package nl.winteressentials.storage;

import nl.winteressentials.models.Home;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface StorageService {

    void init();

    CompletableFuture<Map<String, Home>> loadHomes(@NotNull UUID uuid);

    CompletableFuture<Void> saveHomes(@NotNull UUID uuid, @NotNull Map<String, Home> homes);

    CompletableFuture<ItemStack[]> loadVault(@NotNull UUID uuid, int vaultNumber);

    CompletableFuture<Void> saveVault(@NotNull UUID uuid, int vaultNumber, @NotNull ItemStack[] items);

    void flushSync();

    void shutdown();
}
