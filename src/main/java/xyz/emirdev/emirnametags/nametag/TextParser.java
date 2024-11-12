package xyz.emirdev.emirnametags.nametag;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TextParser {
    public static Component parse(String input, Player player) {
        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolvers(
                                StandardTags.defaults(),
                                placeholderTag(player),
                                skriptTag(player)
                        )
                        .build()
                )
                .build()
                .deserialize(input);
    }

    public static TagResolver placeholderTag(Player player) {
        return TagResolver.resolver(Set.of("placeholder", "papi", "p"), (argumentQueue, context) -> {
            final String placeholder = argumentQueue.popOr("placeholder tag requires an argument").value();
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
                        return Tag.selfClosingInserting(componentPlaceholder);
                    }

                    return Tag.selfClosingInserting(MiniMessage.miniMessage().deserialize(parsedPlaceholder));
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
}
