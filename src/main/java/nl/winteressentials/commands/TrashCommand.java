package nl.winteressentials.commands;

import net.kyori.adventure.text.Component;
import nl.winteressentials.WinterEssentials;
import nl.winteressentials.holders.TrashHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class TrashCommand extends BaseCommand {

    public TrashCommand(@NotNull WinterEssentials plugin) {
        super(plugin, "trash", "winteressentials.command.trash", true);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        TrashHolder holder = new TrashHolder();
        String titleRaw = plugin.getConfig().getString("messages.trash_title", "<#80D8FF>Prullenbak");
        Component title = messageService.parse(titleRaw);

        Inventory inventory = Bukkit.createInventory(holder, 27, title);
        holder.setInventory(inventory);

        player.openInventory(inventory);
        cooldownManager.applyCooldown(player, commandKey);
        return true;
    }
}
