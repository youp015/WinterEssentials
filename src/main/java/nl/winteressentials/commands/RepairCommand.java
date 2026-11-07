package nl.winteressentials.commands;

import nl.winteressentials.WinterEssentials;
import nl.winteressentials.message.MessageService;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RepairCommand extends BaseCommand {

    public RepairCommand(@NotNull WinterEssentials plugin) {

        super(plugin, "repair", null, true);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
        Player player = (Player) sender;

        String sub = args.length > 0 ? args[0].toLowerCase() : "";

        if (sub.equals("all")) {
            handleRepairAll(player);
            return true;
        }

        if (sub.equals("hand") || sub.isEmpty()) {
            handleRepairHand(player);
            return true;
        }

        messageService.sendRaw(player, "<red>Gebruik: /repair [hand|all]");
        return true;
    }

    private void handleRepairHand(Player player) {
        if (!player.hasPermission("winteressentials.command.repair.hand")) {
            messageService.send(player, "no_permission");
            return;
        }

        long remaining = cooldownManager.getRemainingSeconds(player, "repair_hand");
        if (remaining > 0) {
            messageService.send(player, "on_cooldown", "time", MessageService.formatDuration(remaining));
            return;
        }

        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType().isAir()) {
            messageService.send(player, "repair_empty_hand");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable) || damageable.getDamage() <= 0) {
            messageService.send(player, "repair_no_damage");
            return;
        }

        damageable.setDamage(0);
        item.setItemMeta(damageable);

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.4f, 1.4f);
        messageService.send(player, "repaired_hand");
        cooldownManager.applyCooldown(player, "repair_hand");
    }

    private void handleRepairAll(Player player) {
        if (!player.hasPermission("winteressentials.command.repair.all")) {
            messageService.send(player, "no_permission");
            return;
        }

        long remaining = cooldownManager.getRemainingSeconds(player, "repair_all");
        if (remaining > 0) {
            messageService.send(player, "on_cooldown", "time", MessageService.formatDuration(remaining));
            return;
        }

        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof Damageable damageable && damageable.getDamage() > 0) {
                    damageable.setDamage(0);
                    item.setItemMeta(damageable);
                    count++;
                }
            }
        }

        if (count == 0) {
            messageService.send(player, "repair_no_items");
            return;
        }

        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 0.5f, 1.2f);
        messageService.send(player, "repaired_all");
        cooldownManager.applyCooldown(player, "repair_all");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String label,
                                                @NotNull String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (sender.hasPermission("winteressentials.command.repair.hand") && "hand".startsWith(args[0].toLowerCase())) {
                completions.add("hand");
            }
            if (sender.hasPermission("winteressentials.command.repair.all") && "all".startsWith(args[0].toLowerCase())) {
                completions.add("all");
            }
            return completions;
        }
        return Collections.emptyList();
    }
}
