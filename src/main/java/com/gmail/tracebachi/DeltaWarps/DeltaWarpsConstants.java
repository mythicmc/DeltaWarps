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

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class DeltaWarpsConstants
{
  public static class Channels
  {
    public static final String WARP = "Warp";
  }

  public static class Formats
  {
    public static final String NO_PERM = "NoPerm";
    public static final String USAGE = "Usage";
    public static final String PLAYER_ONLY_COMMAND = "PlayerOnlyCommand";
    public static final String PLAYER_OFFLINE = "PlayerOffline";

    public static final String INVALID_AMOUNT = "InvalidAmount";
    public static final String INVALID_WARP_TYPE = "InvalidWarpType";
    public static final String INVALID_WARP_NAME = "InvalidWarpName";
    public static final String WARP_OWNER_UPDATED = "WarpOwnerUpdated";
    public static final String WARP_ADDED = "WarpAdded";
    public static final String WARP_REMOVED = "WarpRemoved";
    public static final String WARP_MOVED = "WarpMoved";
    public static final String WARPING_TO = "WarpingTo";
    public static final String WARP_NOT_FOUND = "WarpNotFound";
    public static final String FACTION_NOT_FOUND = "FactionNotFound";
    public static final String WARP_NOT_OWNED_BY_NAME = "WarpNotOwnedByName";
    public static final String UNEXPECTED_ERROR = "UnexpectedError";

    public static final String WARP_INFO_WARP_LINE = "WarpInfo/Warp/Line";
    public static final String WARP_INFO_PLAYER_LINE = "WarpInfo/Player/Line";
    public static final String WARP_INFO_PLAYER_WARP = "WarpInfo/Player/Warp";
    public static final String WARP_INFO_FACTION_LINE = "WarpInfo/Faction/Line";
    public static final String WARP_INFO_FACTION_WARP_NAME = "WarpInfo/Faction/WarpName";
    public static final String WARP_LIST_PAGE = "WarpList/Page";
    public static final String WARP_LIST_WARP_NAME = "WarpList/WarpName";

    public static final String NOT_ALLOWED_ON_SERVER = "NotAllowedOnServer";
    public static final String NOT_ENOUGH_FREE_WARPS_FOR_ADD = "NotEnoughFreeWarpsForAdd";
    public static final String FACTIONS_NOT_ENABLED = "FactionsNotEnabled";
    public static final String FACTION_WARP_REQUIRES_A_FACTION = "FactionWarp/RequiresAFaction";
    public static final String FACTION_WARP_ONLY_ON_FACTION_LAND = "FactionWarp/OnlyOnFactionLand";
    public static final String FACTION_WARP_ONLY_USABLE_ON_SAME_SERVER = "FactionWarp/OnlyUsableOnSameServer";
    public static final String FACTION_WARP_NOT_OWNED_BY_YOUR_FACTION = "FactionWarp/NotOwnedByYourFaction";
    public static final String FACTION_WARP_NOT_ON_FACTION_LAND = "FactionWarp/NotOnFactionLand";
  }
}
