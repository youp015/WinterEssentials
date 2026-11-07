package nl.winteressentials.managers;

import net.kyori.adventure.text.Component;
import nl.winteressentials.WinterEssentials;
import nl.winteressentials.holders.VaultHolder;
import nl.winteressentials.message.MessageService;
import nl.winteressentials.permissions.LimitManager;
import nl.winteressentials.storage.StorageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class VaultManager {

    private final WinterEssentials plugin;
    private final StorageService storageService;
    private final LimitManager limitManager;
    private final MessageService messageService;

    private final Set<String> openVaultKeys = ConcurrentHashMap.newKeySet();

    private final Map<String, Inventory> activeInventories = new ConcurrentHashMap<>();

    public VaultManager(@NotNull WinterEssentials plugin,
                        @NotNull StorageService storageService,
                        @NotNull LimitManager limitManager,
                        @NotNull MessageService messageService) {
        this.plugin = plugin;
        this.storageService = storageService;
        this.limitManager = limitManager;
        this.messageService = messageService;
    }

    private String getVaultKey(UUID uuid, int vaultNumber) {
        return uuid.toString() + ":" + vaultNumber;
    }

    public boolean canAccessVault(@NotNull Player player, int vaultNumber) {
        if (vaultNumber < 1 || vaultNumber > 99) {
            return false;
        }
        int max = limitManager.getMaxVaults(player);
        return vaultNumber <= max;
    }

    public CompletableFuture<Boolean> openVault(@NotNull Player player, int vaultNumber) {
        if (!canAccessVault(player, vaultNumber)) {
            int max = limitManager.getMaxVaults(player);
            messageService.send(player, "vault_limit_reached",
                    "number", String.valueOf(vaultNumber),
                    "limit", String.valueOf(max));
            return CompletableFuture.completedFuture(false);
        }

        UUID uuid = player.getUniqueId();
        String key = getVaultKey(uuid, vaultNumber);

        Inventory active = activeInventories.get(key);
        if (active != null) {
            Bukkit.getScheduler().runTask(plugin, () -> player.openInventory(active));
            return CompletableFuture.completedFuture(true);
        }

        return storageService.loadVault(uuid, vaultNumber).thenApply(items -> {

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }

                VaultHolder holder = new VaultHolder(uuid, vaultNumber);
                String titleFormat = plugin.getConfig().getString("messages.vault_title", "<#80D8FF>Vault #{number}");
                Component title = messageService.parse(titleFormat, "number", String.valueOf(vaultNumber));

                Inventory inv = Bukkit.createInventory(holder, 54, title);
                holder.setInventory(inv);

                if (items != null) {
                    for (int i = 0; i < items.length && i < inv.getSize(); i++) {
                        if (items[i] != null) {
                            inv.setItem(i, items[i]);
                        }
                    }
                }

                activeInventories.put(key, inv);
                openVaultKeys.add(key);
                player.openInventory(inv);
            });
            return true;
        });
    }

    public CompletableFuture<Void> handleVaultClose(@NotNull VaultHolder holder, @NotNull Inventory inventory) {
        UUID uuid = holder.getOwnerUniqueId();
        int vaultNumber = holder.getVaultNumber();
        String key = getVaultKey(uuid, vaultNumber);

        openVaultKeys.remove(key);
        activeInventories.remove(key);

        ItemStack[] contents = inventory.getContents();
        return storageService.saveVault(uuid, vaultNumber, contents);
    }
}
