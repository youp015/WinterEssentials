package nl.winteressentials;

import nl.winteressentials.commands.*;
import nl.winteressentials.cooldown.CooldownManager;
import nl.winteressentials.listeners.PlayerConnectionListener;
import nl.winteressentials.listeners.TeleportListener;
import nl.winteressentials.listeners.TrashListener;
import nl.winteressentials.listeners.VaultListener;
import nl.winteressentials.managers.HomeManager;
import nl.winteressentials.managers.VaultManager;
import nl.winteressentials.message.MessageService;
import nl.winteressentials.permissions.LimitManager;
import nl.winteressentials.storage.StorageService;
import nl.winteressentials.storage.YamlStorageService;
import nl.winteressentials.teleport.TeleportManager;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class WinterEssentials extends JavaPlugin {

    private static WinterEssentials instance;

    private MessageService messageService;
    private CooldownManager cooldownManager;
    private LimitManager limitManager;
    private StorageService storageService;
    private HomeManager homeManager;
    private VaultManager vaultManager;
    private TeleportManager teleportManager;

    public static WinterEssentials getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.messageService = new MessageService(this);
        this.cooldownManager = new CooldownManager(this);
        this.limitManager = new LimitManager(this);

        this.storageService = new YamlStorageService(this);
        this.storageService.init();

        this.homeManager = new HomeManager(this, storageService, limitManager);
        this.vaultManager = new VaultManager(this, storageService, limitManager, messageService);
        this.teleportManager = new TeleportManager(this, messageService, cooldownManager);

        registerListeners();

        registerCommands();

        getLogger().info("========================================");
        getLogger().info(" WinterEssentials v" + getPluginMeta().getVersion() + " is succesvol opgestart!");
        getLogger().info(" Clean, lightweight Paper essentials");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        if (teleportManager != null) {
            teleportManager.cancelAll();
        }

        if (storageService != null) {
            storageService.shutdown();
        }

        getLogger().info("WinterEssentials is veilig uitgeschakeld en data is opgeslagen.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        if (limitManager != null) {
            limitManager.checkLuckPerms();
        }
    }

    private void registerListeners() {
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new TrashListener(), this);
        pm.registerEvents(new VaultListener(vaultManager), this);
        pm.registerEvents(new TeleportListener(teleportManager), this);
        pm.registerEvents(new PlayerConnectionListener(homeManager, cooldownManager), this);
    }

    private void registerCommands() {
        registerCmd("trash", new TrashCommand(this));
        registerCmd("repair", new RepairCommand(this));
        registerCmd("feed", new FeedCommand(this));

        registerCmd("craft", new WorkbenchCommand(this, "craft", "winteressentials.command.craft",
                player -> player.openWorkbench(null, true)));
        registerCmd("anvil", new WorkbenchCommand(this, "anvil", "winteressentials.command.anvil",
                player -> player.openAnvil(null, true)));
        registerCmd("grindstone", new WorkbenchCommand(this, "grindstone", "winteressentials.command.grindstone",
                player -> player.openGrindstone(null, true)));
        registerCmd("stonecutter", new WorkbenchCommand(this, "stonecutter", "winteressentials.command.stonecutter",
                player -> player.openStonecutter(null, true)));
        registerCmd("cartography", new WorkbenchCommand(this, "cartography", "winteressentials.command.cartography",
                player -> player.openCartographyTable(null, true)));
        registerCmd("smithing", new WorkbenchCommand(this, "smithing", "winteressentials.command.smithing",
                player -> player.openSmithingTable(null, true)));
        registerCmd("loom", new WorkbenchCommand(this, "loom", "winteressentials.command.loom",
                player -> player.openLoom(null, true)));
        registerCmd("enderchest", new WorkbenchCommand(this, "enderchest", "winteressentials.command.enderchest",
                player -> player.openInventory(player.getEnderChest())));

        registerCmd("personalvaults", new VaultCommand(this, vaultManager, limitManager));
        registerCmd("sethome", new SetHomeCommand(this, homeManager));
        registerCmd("home", new HomeCommand(this, homeManager, teleportManager));
        registerCmd("delhome", new DelHomeCommand(this, homeManager));
        registerCmd("homes", new HomesCommand(this, homeManager, limitManager));

        registerCmd("winteressentials", new WinterEssentialsCommand(this));
    }

    private void registerCmd(@NotNull String name, @NotNull BaseCommand commandHandler) {
        PluginCommand cmd = getCommand(name);
        if (cmd != null) {
            cmd.setExecutor(commandHandler);
            cmd.setTabCompleter(commandHandler);
        } else {
            getLogger().warning("Commando niet gedefinieerd in plugin.yml: " + name);
        }
    }

    public @NotNull MessageService getMessageService() {
        return messageService;
    }

    public @NotNull CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public @NotNull LimitManager getLimitManager() {
        return limitManager;
    }

    public @NotNull StorageService getStorageService() {
        return storageService;
    }

    public @NotNull HomeManager getHomeManager() {
        return homeManager;
    }

    public @NotNull VaultManager getVaultManager() {
        return vaultManager;
    }

    public @NotNull TeleportManager getTeleportManager() {
        return teleportManager;
    }
}
