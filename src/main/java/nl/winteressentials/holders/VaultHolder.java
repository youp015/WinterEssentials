package nl.winteressentials.holders;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class VaultHolder implements InventoryHolder {

    private final UUID ownerUniqueId;
    private final int vaultNumber;
    private Inventory inventory;

    public VaultHolder(@NotNull UUID ownerUniqueId, int vaultNumber) {
        this.ownerUniqueId = ownerUniqueId;
        this.vaultNumber = vaultNumber;
    }

    public @NotNull UUID getOwnerUniqueId() {
        return ownerUniqueId;
    }

    public int getVaultNumber() {
        return vaultNumber;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
