package nl.winteressentials.models;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record Home(
        @NotNull String name,
        @NotNull String worldName,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        long createdAt
) {
    public Home {
        Objects.requireNonNull(name, "Home name cannot be null");
        Objects.requireNonNull(worldName, "World name cannot be null");
    }

    public static Home fromLocation(@NotNull String name, @NotNull Location location) {
        World world = location.getWorld();
        String worldName = world != null ? world.getName() : "world";
        return new Home(
                name,
                worldName,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch(),
                System.currentTimeMillis()
        );
    }

    @Nullable
    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

    @Nullable
    public Location toLocation() {
        World world = getWorld();
        if (world == null) {
            return null;
        }
        return new Location(world, x, y, z, yaw, pitch);
    }
}
