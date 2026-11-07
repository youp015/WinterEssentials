package nl.winteressentials.listeners;

import nl.winteressentials.holders.VaultHolder;
import nl.winteressentials.managers.VaultManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

public class VaultListener implements Listener {

    private final VaultManager vaultManager;

    public VaultListener(@NotNull VaultManager vaultManager) {
        this.vaultManager = vaultManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(@NotNull InventoryCloseEvent event) {
        if (event.getInventory().getHolder() instanceof VaultHolder holder) {
            vaultManager.handleVaultClose(holder, event.getInventory());
        }
    }
}
