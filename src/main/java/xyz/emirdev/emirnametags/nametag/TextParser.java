package xyz.emirdev.emirnametags.nametag;

import ch.njol.skript.lang.function.Function;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.variables.Variables;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import xyz.emirdev.emirnametags.EmirNametags;

import java.time.Instant;
import java.util.*;

public class TextParser {
    private static final Map<UUID, Map<String, CachedPlaceholder>> placeholderCache = new HashMap<>();

    private static class CachedPlaceholder {
        private final Component value;
        private final Instant cachedAt;
        private final int cacheTime;

        public CachedPlaceholder(String placeholder, Component value) {
            this.value = value;
            this.cachedAt = Instant.now();
            this.cacheTime = EmirNametags.get().getPluginConfig().getPlaceholderCacheTicks(placeholder);
        }

        public Component getValue() {
            return value;
        }

        public boolean isExpired() {
            return cachedAt.plusSeconds(cacheTime / 20).isBefore(Instant.now());
        }
    }

    private static void cachePlaceholder(Player player, String placeholder, Component value) {
        if (placeholderCache.containsKey(player.getUniqueId())) {
            Map<String, CachedPlaceholder> cachedPlaceholders = placeholderCache.get(player.getUniqueId());
            cachedPlaceholders.put(placeholder, new CachedPlaceholder(placeholder, value));
        } else {
            Map<String, CachedPlaceholder> cachedPlaceholders = new HashMap<>();
            cachedPlaceholders.put(placeholder, new CachedPlaceholder(placeholder, value));
            placeholderCache.put(player.getUniqueId(), cachedPlaceholders);
        }
    }

    public static void clearPlaceholderCache() {
        placeholderCache.clear();
    }

    public static Component parse(String input, Player player) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolvers(
                                StandardTags.defaults(),
                                placeholderTag(player),
                                skriptTag(player),
                                skriptFunctionTag(player)
                        )
                        .build()
                )
                .build()
                .deserialize(input);
    }

    public static TagResolver placeholderTag(Player player) {
        return TagResolver.resolver(Set.of("placeholder", "papi", "p"), (argumentQueue, context) -> {
            List<String> strings = new ArrayList<>();
            while (argumentQueue.hasNext()) {
                strings.add(argumentQueue.pop().value());
            }
            final String placeholder = String.join(":", strings);

            if (placeholderCache.containsKey(player.getUniqueId())) {
                Map<String, CachedPlaceholder> cachedPlaceholders = placeholderCache.get(player.getUniqueId());
                if (cachedPlaceholders.containsKey(placeholder)) {
                    CachedPlaceholder cachedPlaceholder = cachedPlaceholders.get(placeholder);
                    if (!cachedPlaceholder.isExpired()) {
                        return Tag.selfClosingInserting(cachedPlaceholder.getValue());
                    }
                }
            }

            switch (placeholder) {
                case "name" -> {
                    return Tag.selfClosingInserting(player.name());
                }
                case "displayname" -> {
                    if (EmirNametags.get().isLuckPermsEnabled()) {
                        User user = EmirNametags.get().getLuckPerms().getPlayerAdapter(Player.class).getUser(player);
                        String prefix = Objects.requireNonNullElse(user.getCachedData().getMetaData().getPrefix(), "");
                        String suffix = Objects.requireNonNullElse(user.getCachedData().getMetaData().getSuffix(), "");
                        return Tag.selfClosingInserting(LegacyComponentSerializer.legacyAmpersand().deserialize(prefix + player.getName() + suffix));
                    }
                    return Tag.selfClosingInserting(player.displayName());
                }
                default -> {
                    if (!EmirNametags.get().isPapiEnabled()) return Tag.selfClosingInserting(Component.text(placeholder));

                    final String parsedPlaceholder = PlaceholderAPI.setPlaceholders(player, '%' + placeholder + '%');

                    if (parsedPlaceholder.contains(LegacyComponentSerializer.AMPERSAND_CHAR + "")) {
                        Component componentPlaceholder = LegacyComponentSerializer.legacyAmpersand().deserialize(parsedPlaceholder);
                        cachePlaceholder(player, placeholder, componentPlaceholder);
                        return Tag.selfClosingInserting(componentPlaceholder);
                    }

                    Component componentPlaceholder = MiniMessage.miniMessage().deserialize(parsedPlaceholder);
                    cachePlaceholder(player, placeholder, componentPlaceholder);
                    return Tag.selfClosingInserting(componentPlaceholder);
                }
            }
        });
    }

    public static TagResolver skriptTag(Player player) {
        return TagResolver.resolver(Set.of("skript", "sk"), (argumentQueue, context) -> {
            List<String> strings = new ArrayList<>();
            while (argumentQueue.hasNext()) {
                strings.add(argumentQueue.pop().value());
            }
            String variable = String.join(":", strings);
            if (!EmirNametags.get().isSkriptEnabled()) return Tag.selfClosingInserting(Component.text(variable));

            variable = variable
                    .replaceAll("%uuid%", player.getUniqueId().toString());

            String value = String.valueOf(Variables.getVariable(variable, null, false));
            return Tag.selfClosingInserting(Component.text(value));
        });
    }

    public static TagResolver skriptFunctionTag(Player player) {
        return TagResolver.resolver(Set.of("skriptfunction", "skf"), (argumentQueue, context) -> {
            String functionName = argumentQueue.pop().value();
            if (!EmirNametags.get().isSkriptEnabled()) return Tag.selfClosingInserting(Component.text(functionName));

            Function<?> function = Functions.getGlobalFunction(functionName);
            if (function == null) return Tag.selfClosingInserting(Component.text(functionName));

            Object[][] params = {{player}};

            Object[] returnValue = function.execute(params);
            String value = String.valueOf(returnValue[0]);

            return Tag.selfClosingInserting(Component.text(value));
        });
    }
}
