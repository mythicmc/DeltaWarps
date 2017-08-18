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
package com.gmail.tracebachi.DeltaWarps.Storage;

import org.bukkit.Location;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class Warp
{
  private String name;
  private int ownerId;
  private int x;
  private int y;
  private int z;
  private float yaw;
  private float pitch;
  private String world;
  private WarpType type;
  private String faction;
  private String server;

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getOwnerId()
  {
    return ownerId;
  }

  public void setOwnerId(int ownerId)
  {
    this.ownerId = ownerId;
  }

  public int getX()
  {
    return x;
  }

  public void setX(int x)
  {
    this.x = x;
  }

  public int getY()
  {
    return y;
  }

  public void setY(int y)
  {
    this.y = y;
  }

  public int getZ()
  {
    return z;
  }

  public void setZ(int z)
  {
    this.z = z;
  }

  public float getYaw()
  {
    return yaw;
  }

  public void setYaw(float yaw)
  {
    this.yaw = yaw;
  }

  public float getPitch()
  {
    return pitch;
  }

  public void setPitch(float pitch)
  {
    this.pitch = pitch;
  }

  public String getWorld()
  {
    return world;
  }

  public void setWorld(String world)
  {
    this.world = world;
  }

  public void setLocation(Location location)
  {
    this.x = location.getBlockX();
    this.y = location.getBlockY();
    this.z = location.getBlockZ();
    this.yaw = Math.round(location.getYaw());
    this.pitch = Math.round(location.getPitch());
    this.world = location.getWorld().getName();
  }

  public WarpType getType()
  {
    return type;
  }

  public void setType(WarpType type)
  {
    this.type = type;
  }

  public String getFaction()
  {
    return faction;
  }

  public void setFaction(String faction)
  {
    this.faction = faction;
  }

  public String getServer()
  {
    return server;
  }

  public void setServer(String server)
  {
    this.server = server;
  }

  //  public Warp(String name, Location location, WarpType type, String faction, String server)
//  {
//    this(name, location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getYaw(),
//      location.getPitch(), location.getWorld().getName(), type, faction, server);
//  }
//
//  public Warp(
//    String name, int x, int y, int z, float yaw, float pitch, String world, WarpType type,
//    String faction, String server)
//  {
//    Preconditions.checkNotNull(name, "name");
//    Preconditions.checkNotNull(world, "world");
//    Preconditions.checkNotNull(type, "type");
//    Preconditions.checkNotNull(server, "server");
//
//    this.name = name.toLowerCase();
//    this.x = x;
//    this.y = y;
//    this.z = z;
//    this.yaw = Math.round(yaw);
//    this.pitch = Math.round(pitch);
//    this.world = world;
//    this.type = type;
//    this.faction = faction;
//    this.server = server;
//  }
//
//  public String getName()
//  {
//    return name;
//  }
//
//  public int getX()
//  {
//    return x;
//  }
//
//  public int getY()
//  {
//    return y;
//  }
//
//  public int getZ()
//  {
//    return z;
//  }
//
//  public float getYaw()
//  {
//    return yaw;
//  }
//
//  public float getPitch()
//  {
//    return pitch;
//  }
//
//  public String getWorld()
//  {
//    return world;
//  }
//
//  public WarpType getType()
//  {
//    return type;
//  }
//
//  public String getFaction()
//  {
//    return faction;
//  }
//
//  public String getServer()
//  {
//    return server;
//  }
}
