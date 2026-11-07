package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.managers.HomeManager;
import nl.winteressentials.models.Home;
import nl.winteressentials.permissions.LimitManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class HomesCommand extends BaseCommand {

    private final HomeManager homeManager;
    private final LimitManager limitManager;

    public HomesCommand(@NotNull WinterEssentials plugin,
                        @NotNull HomeManager homeManager,
                        @NotNull LimitManager limitManager) {
        super(plugin, "homes", "winteressentials.command.homes", true);
        this.homeManager = homeManager;
        this.limitManager = limitManager;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        homeManager.getHomes(player.getUniqueId()).thenAccept(homes -> {
            if (homes.isEmpty()) {
                messageService.send(player, "homes_empty");
                return;
            }

            int count = homes.size();
            int max = limitManager.getMaxHomes(player);

            String headerFormat = plugin.getConfig().getString("messages.homes_header",
                    "<#80D8FF>Jouw homes (<white>{count}</white>/<white>{limit}</white>):");
            messageService.sendRaw(player, headerFormat,
                    "count", String.valueOf(count),
                    "limit", String.valueOf(max));

            String lineFormat = plugin.getConfig().getString("messages.homes_format",
                    "<gray>- <hover:show_text:'<#80D8FF>Klik om te teleporteren naar {home}\n<gray>X: <white>{x}</white> Y: <white>{y}</white> Z: <white>{z}</white> ({world})'><click:run_command:'/home {home}'><#80D8FF>{home}</#80D8FF></click></hover>");

            for (Home home : homes.values()) {
                messageService.sendRaw(player, lineFormat,
                        "home", home.name(),
                        "world", home.worldName(),
                        "x", String.format(java.util.Locale.ROOT, "%.1f", home.x()),
                        "y", String.format(java.util.Locale.ROOT, "%.1f", home.y()),
                        "z", String.format(java.util.Locale.ROOT, "%.1f", home.z())
                );
            }
        });

        return true;
    }
}
