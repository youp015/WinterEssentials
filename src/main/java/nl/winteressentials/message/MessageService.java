package nl.winteressentials.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import nl.winteressentials.WinterEssentials;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageService {

    private final WinterEssentials plugin;
    private final MiniMessage miniMessage;

    public MessageService(@NotNull WinterEssentials plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public @NotNull MiniMessage getMiniMessage() {
        return miniMessage;
    }

    public static @NotNull Map<String, String> toMap(@Nullable String... pairs) {
        Map<String, String> map = new LinkedHashMap<>();
        if (pairs == null) {
            return map;
        }
        for (int i = 0; i < pairs.length - 1; i += 2) {
            if (pairs[i] != null && pairs[i + 1] != null) {
                map.put(pairs[i], pairs[i + 1]);
            }
        }
        return map;
    }

    public static @NotNull Map<String, String> expandPlaceholders(@NotNull Map<String, String> placeholders) {
        Map<String, String> expanded = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue() != null ? entry.getValue() : "";
            expanded.put(k.toLowerCase(Locale.ROOT), v);

            switch (k.toLowerCase(Locale.ROOT)) {
                case "seconds", "seconden" -> {
                    expanded.put("seconds", v);
                    expanded.put("seconden", v);
                    expanded.put("sec", v);
                    expanded.put("time", v);
                    expanded.put("tijd", v);
                }
                case "number", "nummer" -> {
                    expanded.put("number", v);
                    expanded.put("nummer", v);
                    expanded.put("vault", v);
                    expanded.put("kluis", v);
                }
                case "home" -> {
                    expanded.put("home", v);
                    expanded.put("homenaam", v);
                    expanded.put("naam", v);
                }
                case "limit", "limiet" -> {
                    expanded.put("limit", v);
                    expanded.put("limiet", v);
                    expanded.put("max", v);
                }
                case "count", "aantal" -> {
                    expanded.put("count", v);
                    expanded.put("aantal", v);
                }
                case "target" -> {
                    expanded.put("target", v);
                    expanded.put("speler", v);
                    expanded.put("player", v);
                }
                case "sender" -> {
                    expanded.put("sender", v);
                    expanded.put("afzender", v);
                }
                case "time", "tijd" -> {
                    expanded.put("time", v);
                    expanded.put("tijd", v);
                }
            }
        }
        return expanded;
    }

    public static @NotNull String replacePlaceholders(@NotNull String text, @NotNull Map<String, String> expanded) {
        if (text.isEmpty() || expanded.isEmpty()) {
            return text;
        }
        String result = text;
        for (Map.Entry<String, String> entry : expanded.entrySet()) {
            String pattern = "(?i)\\{" + Pattern.quote(entry.getKey()) + "\\}";
            result = result.replaceAll(pattern, Matcher.quoteReplacement(entry.getValue()));
        }
        return result;
    }

    public static @NotNull List<TagResolver> createResolvers(@NotNull Map<String, String> expanded) {
        List<TagResolver> list = new ArrayList<>();
        for (Map.Entry<String, String> entry : expanded.entrySet()) {
            list.add(Placeholder.unparsed(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public @NotNull Component parse(@Nullable String text) {
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        return miniMessage.deserialize(text);
    }

    public @NotNull Component parse(@Nullable String text, @NotNull String... placeholders) {
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        Map<String, String> map = toMap(placeholders);
        Map<String, String> expanded = expandPlaceholders(map);
        String preprocessed = replacePlaceholders(text, expanded);
        List<TagResolver> resolvers = createResolvers(expanded);
        return miniMessage.deserialize(preprocessed, TagResolver.resolver(resolvers));
    }

    public @NotNull Component parseWithResolvers(@Nullable String text, @NotNull TagResolver... resolvers) {
        if (text == null || text.isBlank()) {
            return Component.empty();
        }
        return miniMessage.deserialize(text, resolvers);
    }

    public @NotNull String getPrefix() {
        return plugin.getConfig().getString("prefix", "<gradient:#80D8FF:#ECEFF1>Winter</gradient> <dark_gray>»</dark_gray> <gray>");
    }

    public @NotNull String getRawMessage(@NotNull String key) {
        return plugin.getConfig().getString("messages." + key, "<red>Missing message: " + key);
    }

    public @NotNull Component getPrefixedComponent(@NotNull String key, @NotNull String... placeholders) {
        String raw = getRawMessage(key);
        String prefix = getPrefix();
        return parse(prefix + raw, placeholders);
    }

    public @NotNull Component getComponent(@NotNull String key, @NotNull String... placeholders) {
        return parse(getRawMessage(key), placeholders);
    }

    public void send(@NotNull CommandSender sender, @NotNull String key) {
        sender.sendMessage(getPrefixedComponent(key));
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull String... placeholders) {
        sender.sendMessage(getPrefixedComponent(key, placeholders));
    }

    public void sendRaw(@NotNull CommandSender sender, @NotNull String rawMiniMessage, @NotNull String... placeholders) {
        sender.sendMessage(parse(rawMiniMessage, placeholders));
    }

    public void sendWithoutPrefix(@NotNull CommandSender sender, @NotNull String key, @NotNull String... placeholders) {
        sender.sendMessage(getComponent(key, placeholders));
    }

    public void sendActionBar(@NotNull Player player, @NotNull String key, @NotNull String... placeholders) {
        Component component = parse(getRawMessage(key), placeholders);
        player.sendActionBar(component);
    }

    public void sendTitle(@NotNull Player player,
                          @NotNull String titleKey,
                          @NotNull String subtitleKey,
                          @NotNull Duration fadeIn,
                          @NotNull Duration stay,
                          @NotNull Duration fadeOut,
                          @NotNull String... placeholders) {
        Component titleComponent = parse(getRawMessage(titleKey), placeholders);
        Component subtitleComponent = parse(getRawMessage(subtitleKey), placeholders);
        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);
        Title title = Title.title(titleComponent, subtitleComponent, times);
        player.showTitle(title);
    }

    public static @NotNull String formatDuration(long totalSeconds) {
        if (totalSeconds <= 0) {
            return "0s";
        }
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (hours > 0) {
            sb.append(hours).append("u ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (seconds > 0 || sb.isEmpty()) {
            sb.append(seconds).append("s");
        }
        return sb.toString().trim();
    }
}
