package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.managers.HomeManager;
import nl.winteressentials.models.Home;
import nl.winteressentials.teleport.TeleportManager;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HomeCommand extends BaseCommand {

    private final HomeManager homeManager;
    private final TeleportManager teleportManager;

    public HomeCommand(@NotNull WinterEssentials plugin,
                       @NotNull HomeManager homeManager,
                       @NotNull TeleportManager teleportManager) {
        super(plugin, "home", "winteressentials.command.home", true);
        this.homeManager = homeManager;
        this.teleportManager = teleportManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        homeManager.getHomes(player.getUniqueId()).thenAccept(homes -> {
            if (homes.isEmpty()) {
                messageService.send(player, "homes_empty");
                return;
            }

            String targetName;
            if (args.length >= 1) {
                targetName = args[0];
            } else if (homes.size() == 1) {
                targetName = homes.keySet().iterator().next();
            } else if (homes.containsKey("home")) {
                targetName = "home";
            } else {
                messageService.sendRaw(player, "<gray>Kies een home: <#80D8FF>/home <naam></#80D8FF> of typ <#80D8FF>/homes</#80D8FF>.");
                return;
            }

            Home home = homes.get(targetName.toLowerCase());
            if (home == null) {
                messageService.send(player, "home_not_found", "home", targetName);
                return;
            }

            Location targetLocation = home.toLocation();
            if (targetLocation == null) {
                messageService.send(player, "home_world_not_found");
                return;
            }

            teleportManager.startTeleport(player, home.name(), targetLocation);
        });

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1 && sender instanceof Player player) {
            Map<String, Home> homes = homeManager.getHomes(player.getUniqueId()).join();
            if (homes == null || homes.isEmpty()) {
                return Collections.emptyList();
            }

            List<String> list = new ArrayList<>();
            for (Home h : homes.values()) {
                if (h.name().toLowerCase().startsWith(args[0].toLowerCase())) {
                    list.add(h.name());
                }
            }
            return list;
        }
        return Collections.emptyList();
    }
}
