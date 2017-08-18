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

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class WarpOwner
{
  private int id;
  private String name;
  private int extraNormalWarps;
  private int extraFactionWarps;

  public int getId()
  {
    return id;
  }

  public void setId(int id)
  {
    this.id = id;
  }

  public String getName()
  {
    return name;
  }

  public void setName(String name)
  {
    this.name = name;
  }

  public int getExtraNormalWarps()
  {
    return Math.max(0, extraNormalWarps);
  }

  public void setExtraNormalWarps(int extraNormalWarps)
  {
    this.extraNormalWarps = extraNormalWarps;
  }

  public int getExtraFactionWarps()
  {
    return Math.max(0, extraFactionWarps);
  }

  public void setExtraFactionWarps(int extraFactionWarps)
  {
    this.extraFactionWarps = extraFactionWarps;
  }
}
