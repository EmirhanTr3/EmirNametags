package xyz.emirdev.emirnametags.nametag;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class NametagUpdateEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean isCancelled;
    private final Nametag nametag;

    public NametagUpdateEvent(Player player, Nametag nametag) {
        super(player);
        isCancelled = false;
        this.nametag = nametag;
    }

    public Nametag getNametag() {
        return nametag;
    }

    @Override
    public boolean isCancelled() {
        return isCancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        isCancelled = b;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
