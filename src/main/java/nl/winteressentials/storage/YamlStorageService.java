package nl.winteressentials.storage;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.models.Home;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class YamlStorageService implements StorageService {

    private final WinterEssentials plugin;
    private final File homesFolder;
    private final File vaultsFolder;
    private final ExecutorService executor;
    private final Map<UUID, ReentrantLock> locks = new ConcurrentHashMap<>();

    public YamlStorageService(@NotNull WinterEssentials plugin) {
        this.plugin = plugin;
        this.homesFolder = new File(plugin.getDataFolder(), "data/homes");
        this.vaultsFolder = new File(plugin.getDataFolder(), "data/vaults");
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public void init() {
        if (!homesFolder.exists() && !homesFolder.mkdirs()) {
            plugin.getLogger().warning("Kon de datamap voor homes niet aanmaken: " + homesFolder.getPath());
        }
        if (!vaultsFolder.exists() && !vaultsFolder.mkdirs()) {
            plugin.getLogger().warning("Kon de datamap voor vaults niet aanmaken: " + vaultsFolder.getPath());
        }
    }

    private ReentrantLock getLock(@NotNull UUID uuid) {
        return locks.computeIfAbsent(uuid, k -> new ReentrantLock());
    }

    @Override
    public CompletableFuture<Map<String, Home>> loadHomes(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            ReentrantLock lock = getLock(uuid);
            lock.lock();
            try {
                File file = new File(homesFolder, uuid + ".yml");
                if (!file.exists()) {
                    return new ConcurrentHashMap<>();
                }

                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection section = config.getConfigurationSection("homes");
                if (section == null) {
                    return new ConcurrentHashMap<>();
                }

                Map<String, Home> map = new ConcurrentHashMap<>();
                for (String key : section.getKeys(false)) {
                    ConfigurationSection homeSec = section.getConfigurationSection(key);
                    if (homeSec == null) continue;

                    String name = homeSec.getString("name", key);
                    String worldName = homeSec.getString("world", "world");
                    double x = homeSec.getDouble("x", 0.0);
                    double y = homeSec.getDouble("y", 64.0);
                    double z = homeSec.getDouble("z", 0.0);
                    float yaw = (float) homeSec.getDouble("yaw", 0.0);
                    float pitch = (float) homeSec.getDouble("pitch", 0.0);
                    long created = homeSec.getLong("created", System.currentTimeMillis());

                    Home home = new Home(name, worldName, x, y, z, yaw, pitch, created);
                    map.put(key.toLowerCase(Locale.ROOT), home);
                }
                return map;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Fout bij het laden van homes voor UUID " + uuid, e);
                return new ConcurrentHashMap<>();
            } finally {
                lock.unlock();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveHomes(@NotNull UUID uuid, @NotNull Map<String, Home> homes) {
        return CompletableFuture.runAsync(() -> {
            ReentrantLock lock = getLock(uuid);
            lock.lock();
            try {
                File file = new File(homesFolder, uuid + ".yml");
                FileConfiguration config = new YamlConfiguration();

                ConfigurationSection section = config.createSection("homes");
                for (Map.Entry<String, Home> entry : homes.entrySet()) {
                    Home home = entry.getValue();
                    ConfigurationSection homeSec = section.createSection(entry.getKey());
                    homeSec.set("name", home.name());
                    homeSec.set("world", home.worldName());
                    homeSec.set("x", home.x());
                    homeSec.set("y", home.y());
                    homeSec.set("z", home.z());
                    homeSec.set("yaw", home.yaw());
                    homeSec.set("pitch", home.pitch());
                    homeSec.set("created", home.createdAt());
                }

                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Fout bij het opslaan van homes voor UUID " + uuid, e);
            } finally {
                lock.unlock();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<ItemStack[]> loadVault(@NotNull UUID uuid, int vaultNumber) {
        return CompletableFuture.supplyAsync(() -> {
            ReentrantLock lock = getLock(uuid);
            lock.lock();
            try {
                File file = new File(vaultsFolder, uuid + ".yml");
                ItemStack[] items = new ItemStack[54];
                if (!file.exists()) {
                    return items;
                }

                FileConfiguration config = YamlConfiguration.loadConfiguration(file);
                ConfigurationSection vaultSec = config.getConfigurationSection("vaults." + vaultNumber + ".items");
                if (vaultSec == null) {
                    return items;
                }

                for (String slotKey : vaultSec.getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(slotKey);
                        if (slot >= 0 && slot < items.length) {
                            items[slot] = vaultSec.getItemStack(slotKey);
                        }
                    } catch (NumberFormatException ignored) {}
                }

                return items;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Fout bij het laden van vault #" + vaultNumber + " voor UUID " + uuid, e);
                return new ItemStack[54];
            } finally {
                lock.unlock();
            }
        }, executor);
    }

    @Override
    public CompletableFuture<Void> saveVault(@NotNull UUID uuid, int vaultNumber, @NotNull ItemStack[] items) {

        ItemStack[] itemsToSave = new ItemStack[items.length];
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && !items[i].getType().isAir()) {
                itemsToSave[i] = items[i].clone();
            }
        }

        return CompletableFuture.runAsync(() -> {
            ReentrantLock lock = getLock(uuid);
            lock.lock();
            try {
                File file = new File(vaultsFolder, uuid + ".yml");
                FileConfiguration config = file.exists() ? YamlConfiguration.loadConfiguration(file) : new YamlConfiguration();

                String basePath = "vaults." + vaultNumber;
                config.set(basePath + ".items", null);

                for (int slot = 0; slot < itemsToSave.length; slot++) {
                    ItemStack item = itemsToSave[slot];
                    if (item != null && !item.getType().isAir()) {
                        config.set(basePath + ".items." + slot, item);
                    }
                }
                config.set(basePath + ".updated", System.currentTimeMillis());

                config.save(file);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Fout bij het opslaan van vault #" + vaultNumber + " voor UUID " + uuid, e);
            } finally {
                lock.unlock();
            }
        }, executor);
    }

    @Override
    public void flushSync() {

        try {
            executor.submit(() -> {}).get(3, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }

    @Override
    public void shutdown() {
        flushSync();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
