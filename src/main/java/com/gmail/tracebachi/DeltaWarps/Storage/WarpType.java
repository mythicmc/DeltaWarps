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
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public enum WarpType
{
  PUBLIC,
  FACTION,
  PRIVATE;

  public static WarpType fromString(String type)
  {
    switch (type.toLowerCase())
    {
      case "faction":
        return FACTION;
      case "private":
        return PRIVATE;
      case "public":
        return PUBLIC;
      default:
        return null;
    }
  }
}
