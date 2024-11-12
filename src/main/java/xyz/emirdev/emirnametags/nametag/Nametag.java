package xyz.emirdev.emirnametags.nametag;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;
import xyz.emirdev.emirnametags.EmirNametags;
import xyz.emirdev.emirnametags.handlers.ConfigHandler;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class Nametag {
    public static final NamespacedKey NAMETAG_KEY = new NamespacedKey(EmirNametags.get(), "nametag");

    private TextDisplay textDisplay;
    private final Player player;
    private final ConfigHandler config = EmirNametags.get().getPluginConfig();

    public Nametag(Player player) {
        this.player = player;
    }

    private void createDisplayEntity() {
        if (textDisplay != null && !textDisplay.isDead()) return;

        Location location = player.getEyeLocation();
        location.setYaw(0);
        location.setPitch(0);

        textDisplay = (TextDisplay) player.getWorld().spawnEntity(
                location,
                EntityType.TEXT_DISPLAY,
                CreatureSpawnEvent.SpawnReason.CUSTOM,
                entity -> {
                    TextDisplay display = (TextDisplay) entity;
                    display.text(parseText());
                    display.setBillboard(config.getNametagBillboard());
                    display.setBackgroundColor(config.getNametagBackgroundColor());
                    display.setViewRange(config.getNametagViewRange());
                    display.setPersistent(false);

                    display.getPersistentDataContainer().set(NAMETAG_KEY, PersistentDataType.STRING, player.getName());

                    Transformation transformation = display.getTransformation();
                    display.setTransformation(new Transformation(
                            new Vector3f(0, config.getNametagYOffset(), 0),
                            transformation.getLeftRotation(),
                            transformation.getScale(),
                            transformation.getRightRotation()
                    ));
                }
        );

        player.addPassenger(textDisplay);
    }

    public void remove() {
        if (textDisplay == null || textDisplay.isDead()) return;
        textDisplay.remove();
    }

    public void update() {
        if (player.isDead()) {
            remove();
            return;
        }

        if (player.getGameMode() == GameMode.SPECTATOR) {
            remove();
            return;
        }

        if (player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            remove();
            return;
        }

        createDisplayEntity();
        if (textDisplay == null || textDisplay.isDead()) return;

        if (!player.getPassengers().contains(textDisplay)) {
            player.addPassenger(textDisplay);
        }

        textDisplay.text(parseText());

        if (player.isSneaking()) {
            if (textDisplay.getTextOpacity() != 100) {
                textDisplay.setTextOpacity((byte) 100);
            }
        } else if (textDisplay.getTextOpacity() == 100) {
            textDisplay.setTextOpacity((byte) -1);
        }

        if (!EmirNametags.get().getPluginConfig().canSeeSelfNametag()) {
            if (player.canSee(textDisplay)) {
                player.hideEntity(EmirNametags.get(), textDisplay);
            }
        } else if (!player.canSee(textDisplay)) {
            player.showEntity(EmirNametags.get(), textDisplay);
        }

    }

    public TextDisplay getEntity() {
        return textDisplay;
    }
    
    private Component parseText() {
        List<String> strings = new ArrayList<>(config.getNametagText());
        String string = String.join("\n", strings);

        return MiniMessage.builder()
                .tags(TagResolver.builder()
                        .resolvers(
                                StandardTags.defaults(),
                                placeholderTag()
                        )
                        .build()
                )
                .build()
                .deserialize(string);
    }

    private TagResolver placeholderTag() {
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
}
