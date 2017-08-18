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

import com.google.common.base.Preconditions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class FactionsHelper
{
  private final boolean factionsEnabled;

  public FactionsHelper(boolean factionsEnabled)
  {
    this.factionsEnabled = factionsEnabled;
  }

  public boolean isFactionsEnabled()
  {
    return factionsEnabled;
  }

  public MPlayer getMPlayer(Player player)
  {
    Preconditions.checkNotNull(player, "player");

    return MPlayer.get(player);
  }

  public Faction getFactionAtLocation(Location location)
  {
    Preconditions.checkNotNull(location, "location");

    return BoardColl.get().getFactionAt(PS.valueOf(location));
  }

  public Faction getFactionByName(String factionName)
  {
    Preconditions.checkNotNull(factionName, "factionName");

    return FactionColl.get().getByName(factionName);
  }
}
