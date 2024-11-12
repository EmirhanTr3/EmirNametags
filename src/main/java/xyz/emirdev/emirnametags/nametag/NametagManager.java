package xyz.emirdev.emirnametags.nametag;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.purpurmc.purpur.event.entity.EntityTeleportHinderedEvent;
import xyz.emirdev.emirnametags.EmirNametags;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NametagManager implements Listener {
    private final Map<UUID, Nametag> nametags = new HashMap<>();

    public NametagManager() {
        Bukkit.getScheduler().runTaskTimer(EmirNametags.get(), () -> nametags.values().forEach(Nametag::update), 0, EmirNametags.get().getPluginConfig().getNametagInterval());

        // Remove all orphan nameplates
        Bukkit.getScheduler().runTaskTimer(EmirNametags.get(), () -> Bukkit.getWorlds().forEach(world -> world.getEntities().forEach(entity -> {
            if (!(entity instanceof TextDisplay textDisplay)) return;
            if (!textDisplay.getPersistentDataContainer().has(Nametag.NAMETAG_KEY)) return;
            for (Nametag nametag : nametags.values()) {
                if (nametag.getEntity() == textDisplay) return;
            }
            textDisplay.remove();
        })), 100, 100);
    }

    public void reloadNametags() {
        this.nametags.values().forEach(Nametag::remove);
        this.nametags.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            nametags.put(player.getUniqueId(), new Nametag(player));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        nametags.put(event.getPlayer().getUniqueId(), new Nametag(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        nametags.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        nametags.get(event.getPlayer().getUniqueId()).remove();
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerTeleportHindered(EntityTeleportHinderedEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getReason() != EntityTeleportHinderedEvent.Reason.IS_VEHICLE) return;
        nametags.get(player.getUniqueId()).remove();
        event.setShouldRetry(true);
    }
}
