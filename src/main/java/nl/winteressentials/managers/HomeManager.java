package nl.winteressentials.managers;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.models.Home;
import nl.winteressentials.permissions.LimitManager;
import nl.winteressentials.storage.StorageService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class HomeManager {

    private final WinterEssentials plugin;
    private final StorageService storageService;
    private final LimitManager limitManager;

    private final Map<UUID, Map<String, Home>> homesCache = new ConcurrentHashMap<>();

    public HomeManager(@NotNull WinterEssentials plugin,
                       @NotNull StorageService storageService,
                       @NotNull LimitManager limitManager) {
        this.plugin = plugin;
        this.storageService = storageService;
        this.limitManager = limitManager;
    }

    public CompletableFuture<Map<String, Home>> preloadPlayer(@NotNull UUID uuid) {
        return storageService.loadHomes(uuid).thenApply(homes -> {
            homesCache.put(uuid, homes);
            return homes;
        });
    }

    public void unloadPlayer(@NotNull UUID uuid) {
        homesCache.remove(uuid);
    }

    public CompletableFuture<Map<String, Home>> getHomes(@NotNull UUID uuid) {
        Map<String, Home> cached = homesCache.get(uuid);
        if (cached != null) {
            return CompletableFuture.completedFuture(Collections.unmodifiableMap(cached));
        }
        return preloadPlayer(uuid).thenApply(Collections::unmodifiableMap);
    }

    public CompletableFuture<Optional<Home>> getHome(@NotNull UUID uuid, @NotNull String name) {
        return getHomes(uuid).thenApply(homes -> Optional.ofNullable(homes.get(name.toLowerCase(Locale.ROOT))));
    }

    public CompletableFuture<SetHomeResult> setHome(@NotNull Player player, @NotNull String homeName, @NotNull Location location) {
        UUID uuid = player.getUniqueId();
        String key = homeName.toLowerCase(Locale.ROOT);

        return getHomes(uuid).thenCompose(currentHomes -> {
            Map<String, Home> modifiable = homesCache.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>());
            boolean exists = modifiable.containsKey(key);

            int maxHomes = limitManager.getMaxHomes(player);
            if (!exists && modifiable.size() >= maxHomes) {
                return CompletableFuture.completedFuture(new SetHomeResult(false, exists, maxHomes, null));
            }

            Home newHome = Home.fromLocation(homeName, location);
            modifiable.put(key, newHome);

            return storageService.saveHomes(uuid, modifiable)
                    .thenApply(v -> new SetHomeResult(true, exists, maxHomes, newHome));
        });
    }

    public CompletableFuture<Boolean> deleteHome(@NotNull UUID uuid, @NotNull String homeName) {
        String key = homeName.toLowerCase(Locale.ROOT);
        return getHomes(uuid).thenCompose(currentHomes -> {
            Map<String, Home> modifiable = homesCache.get(uuid);
            if (modifiable == null || !modifiable.containsKey(key)) {
                return CompletableFuture.completedFuture(false);
            }

            modifiable.remove(key);
            return storageService.saveHomes(uuid, modifiable).thenApply(v -> true);
        });
    }

    public int getCachedHomeCount(@NotNull UUID uuid) {
        Map<String, Home> map = homesCache.get(uuid);
        return map != null ? map.size() : 0;
    }

    public record SetHomeResult(boolean success, boolean overwritten, int limit, @Nullable Home home) {}
}
