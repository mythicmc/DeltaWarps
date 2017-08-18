/*
 * DeltaWarps - Warping plugin for BungeeCord and Spigot servers
 * Copyright (C) 2017 tracebachi@gmail.com (GeeItsZee)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaWarps;

import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class PlayerWarpEvent extends Event implements Cancellable
{
  private final Player player;
  private final String warpName;
  private final WarpType warpType;
  private final Location location;
  private boolean cancelled;

  public PlayerWarpEvent(
    Player player, String warpName, WarpType warpType, Location location)
  {
    this.player = player;
    this.warpName = warpName;
    this.warpType = warpType;
    this.location = location;
  }

  public Player getPlayer()
  {
    return player;
  }

  public String getWarpName()
  {
    return warpName;
  }

  public WarpType getWarpType()
  {
    return warpType;
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

  /* START Required by Spigot *********************************************************************/

  private static final HandlerList handlers = new HandlerList();

  public HandlerList getHandlers()
  {
    return handlers;
  }

  public static HandlerList getHandlerList()
  {
    return handlers;
  }

/* END Required by Spigot ***********************************************************************/
}
