package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class WorkbenchCommand extends BaseCommand {

    private final Consumer<Player> openAction;

    public WorkbenchCommand(@NotNull WinterEssentials plugin,
                            @NotNull String commandKey,
                            @NotNull String permission,
                            @NotNull Consumer<Player> openAction) {
        super(plugin, commandKey, permission, true);
        this.openAction = openAction;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;
        openAction.accept(player);
        cooldownManager.applyCooldown(player, commandKey);
        return true;
    }
}
