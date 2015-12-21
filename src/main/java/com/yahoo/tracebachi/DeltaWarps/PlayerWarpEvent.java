package com.yahoo.tracebachi.DeltaWarps;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/20/15.
 */
public class PlayerWarpEvent extends Event implements Cancellable
{
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;

    private final Player player;
    private final Location location;

    public PlayerWarpEvent(Player player, Location location)
    {
        this.player = player;
        this.location = location;
    }

    public Player getPlayer()
    {
        return player;
    }

    public Location getLocation()
    {
        return location;
    }

    public boolean isCancelled()
    {
        return cancelled;
    }

    public void setCancelled(boolean cancel)
    {
        cancelled = cancel;
    }

    public HandlerList getHandlers()
    {
        return handlers;
    }

    public static HandlerList getHandlerList()
    {
        return handlers;
    }
}
