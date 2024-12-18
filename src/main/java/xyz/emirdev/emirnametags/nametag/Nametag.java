package xyz.emirdev.emirnametags.nametag;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
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

        NametagUpdateEvent event = new NametagUpdateEvent(player, this);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
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

        double scale = player.getAttribute(Attribute.GENERIC_SCALE).getValue();
        Transformation transformation = textDisplay.getTransformation();
        Vector3f scaleVector = new Vector3f((float) scale, (float) scale, (float) scale);

        if (!transformation.getScale().equals(scaleVector)) {
            textDisplay.setTransformation(new Transformation(
                    transformation.getTranslation(),
                    transformation.getLeftRotation(),
                    scaleVector,
                    transformation.getRightRotation()
            ));
        }

    }

    public TextDisplay getEntity() {
        return textDisplay;
    }
    
    private Component parseText() {
        List<String> strings = new ArrayList<>(config.getNametagText());
        String string = String.join("\n", strings);

        return TextParser.parse(string, player);
    }
}
