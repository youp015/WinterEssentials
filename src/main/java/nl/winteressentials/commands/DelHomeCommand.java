package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.managers.HomeManager;
import nl.winteressentials.models.Home;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DelHomeCommand extends BaseCommand {

    private final HomeManager homeManager;

    public DelHomeCommand(@NotNull WinterEssentials plugin, @NotNull HomeManager homeManager) {
        super(plugin, "delhome", "winteressentials.command.delhome", true);
        this.homeManager = homeManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        if (args.length < 1) {
            messageService.sendRaw(player, "<red>Gebruik: /" + label + " <naam>");
            return true;
        }

        String homeName = args[0];
        homeManager.deleteHome(player.getUniqueId(), homeName).thenAccept(deleted -> {
            if (deleted) {
                messageService.send(player, "home_deleted", "home", homeName);
                cooldownManager.applyCooldown(player, commandKey);
            } else {
                messageService.send(player, "home_not_found", "home", homeName);
            }
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
