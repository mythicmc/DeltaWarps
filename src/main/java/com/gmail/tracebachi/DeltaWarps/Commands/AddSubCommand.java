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
package com.gmail.tracebachi.DeltaWarps.Commands;

import com.gmail.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.gmail.tracebachi.DeltaWarps.DeltaWarpsConstants.Formats;
import com.gmail.tracebachi.DeltaWarps.FactionsHelper;
import com.gmail.tracebachi.DeltaWarps.Storage.*;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.google.common.base.Preconditions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class AddSubCommand implements WarpSubCommand
{
  private static final String COMMAND_USAGE = "/warp add <warp name> [public|faction|private]";
  private static final String COMMAND_PERM = "DeltaWarps.Add";
  private static final String PERM_IGNORE_NOT_ALLOWED = "DeltaWarps.Add.IgnoreNotAllowed";

  private final DeltaWarpsPlugin plugin;
  private final String serverName;
  private final boolean allowedToAddWarps;
  private final FactionsHelper factionsHelper;
  private final WarpStorage warpStorage;
  private final WarpOwnerStorage warpOwnerStorage;
  private final MessageFormatMap formatMap;
  private final Executor executor;

  public AddSubCommand(
    DeltaWarpsPlugin plugin, String serverName, boolean allowedToAddWarps, FactionsHelper factionsHelper,
    WarpStorage warpStorage, WarpOwnerStorage warpOwnerStorage, MessageFormatMap formatMap,
    Executor executor)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");
    Preconditions.checkNotNull(factionsHelper, "factionsHelper");
    Preconditions.checkNotNull(warpStorage, "warpStorage");
    Preconditions.checkNotNull(warpOwnerStorage, "warpOwnerStorage");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(executor, "executor");

    this.plugin = plugin;
    this.serverName = serverName;
    this.allowedToAddWarps = allowedToAddWarps;
    this.factionsHelper = factionsHelper;
    this.warpStorage = warpStorage;
    this.warpOwnerStorage = warpOwnerStorage;
    this.formatMap = formatMap;
    this.executor = executor;
  }

  @Override
  public void onCommand(CommandSender sender, String[] args)
  {
    if (!(sender instanceof Player))
    {
      sender.sendMessage(formatMap.format(Formats.PLAYER_ONLY_COMMAND, "add"));
      return;
    }

    if (args.length < 2)
    {
      sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
      return;
    }

    Player player = (Player) sender;

    if (!player.hasPermission(COMMAND_PERM))
    {
      player.sendMessage(formatMap.format(Formats.NO_PERM, COMMAND_PERM));
      return;
    }

    if (!allowedToAddWarps && !player.hasPermission(PERM_IGNORE_NOT_ALLOWED))
    {
      player.sendMessage(formatMap.format(Formats.NOT_ALLOWED_ON_SERVER, "add"));
      return;
    }

    String warpName = args[1].toLowerCase();

    if (RESERVED_NAMES.contains(warpName))
    {
      player.sendMessage(formatMap.format(Formats.INVALID_WARP_NAME, warpName, "RESERVED_NAME"));
      return;
    }

    if (warpName.length() > 31)
    {
      player.sendMessage(formatMap.format(Formats.INVALID_WARP_NAME, warpName, "TOO_LONG"));
      return;
    }

    String warpTypeString = (args.length >= 3) ? args[2] : "PRIVATE";
    WarpType warpType = WarpType.fromString(warpTypeString);

    if (warpType == null)
    {
      sender.sendMessage(formatMap.format(Formats.INVALID_WARP_TYPE, warpTypeString));
      return;
    }

    Location playerLocation = player.getLocation();
    Warp warp = new Warp();
    warp.setName(warpName);
    warp.setType(warpType);
    warp.setServer(serverName);
    warp.setLocation(playerLocation);

    if (warpType == WarpType.FACTION)
    {
      // Check if Factions exists and is enabled
      if (!factionsHelper.isFactionsEnabled())
      {
        sender.sendMessage(formatMap.format(Formats.FACTIONS_NOT_ENABLED));
        return;
      }

      MPlayer mPlayer = factionsHelper.getMPlayer(player);
      Faction facAtPos = factionsHelper.getFactionAtLocation(playerLocation);

      // Check if the player has a faction
      if (!mPlayer.hasFaction())
      {
        sender.sendMessage(formatMap.format(Formats.FACTION_WARP_REQUIRES_A_FACTION));
        return;
      }

      // Check if the player is creating a faction warp on faction land
      if (!mPlayer.getFaction().getId().equals(facAtPos.getId()))
      {
        sender.sendMessage(formatMap.format(Formats.FACTION_WARP_ONLY_ON_FACTION_LAND));
        return;
      }

      // Set faction for warp
      warp.setFaction(facAtPos.getId());
    }

    String senderName = player.getName();
    GroupLimits groupLimits = plugin.getGroupLimitsForSender(player);

    executor.execute(() -> addWarp(senderName, groupLimits, warp));
  }

  private WarpOwner getOrCreateWarpOwner(String ownerName)
  {
    WarpOwner warpOwner = warpOwnerStorage.getByName(ownerName);

    // Return the warpOwner if found
    if (warpOwner != null)
    {
      return warpOwner;
    }

    // Create a new warpOwner
    warpOwner = new WarpOwner();
    warpOwner.setName(ownerName);
    warpOwner.setExtraNormalWarps(0);
    warpOwner.setExtraFactionWarps(0);

    // Add it to the storage
    warpOwnerStorage.add(warpOwner);

    // Get the warp owner (which will include the correct, generated ID)
    return warpOwnerStorage.getByName(ownerName);
  }

  private void addWarp(String senderName, GroupLimits groupLimits, Warp warpToAdd)
  {
    // Get or create a warp owner
    WarpOwner warpOwner = getOrCreateWarpOwner(senderName);

    if (warpOwner == null)
    {
      String message = formatMap.format(
        Formats.UNEXPECTED_ERROR, "Expected to find warpOwner when adding warp");
      plugin.sendMessageSync(message, senderName);
      return;
    }

    // Now we know the ownerId for the senderName, so we can set it for the warpToAdd
    int ownerId = warpOwner.getId();
    warpToAdd.setOwnerId(ownerId);

    Map<WarpType, Integer> warpTypeCountMap = warpStorage.getByOwnerIdGroupByType(ownerId);
    int normalWarpCount = 0;
    int factionWarpCount = 0;

    // Count the number of warps for each type
    for (Map.Entry<WarpType, Integer> entry : warpTypeCountMap.entrySet())
    {
      if (entry.getKey() == WarpType.FACTION)
      {
        factionWarpCount += entry.getValue();
      }
      else
      {
        normalWarpCount += entry.getValue();
      }
    }

    // Check if the player has enough free warps to create another warp
    if (warpToAdd.getType() == WarpType.FACTION)
    {
      int totalLimit = groupLimits.getFactionWarpsLimit() + warpOwner.getExtraFactionWarps();
      if (factionWarpCount >= totalLimit)
      {
        String message = formatMap.format(
          Formats.NOT_ENOUGH_FREE_WARPS_FOR_ADD, factionWarpCount, "FACTION");
        plugin.sendMessageSync(message, senderName);
        return;
      }
    }
    else
    {
      int totalLimit = groupLimits.getNormalWarpsLimit() + warpOwner.getExtraNormalWarps();
      if (normalWarpCount >= totalLimit)
      {
        String message = formatMap.format(
          Formats.NOT_ENOUGH_FREE_WARPS_FOR_ADD, normalWarpCount, "PUBLIC/PRIVATE");
        plugin.sendMessageSync(message, senderName);
        return;
      }
    }

    // Add the warp
    WarpStorage.Result addResult = warpStorage.add(warpToAdd);
    if (addResult == WarpStorage.Result.WARP_NAME_EXISTS)
    {
      String message = formatMap.format(
        Formats.INVALID_WARP_NAME, warpToAdd.getName(), "WARP_NAME_EXISTS");
      plugin.sendMessageSync(message, senderName);
    }
    else if (addResult == WarpStorage.Result.SUCCESS)
    {
      String message = formatMap.format(
        Formats.WARP_ADDED, warpToAdd.getName(), warpToAdd.getType());
      plugin.sendMessageSync(message, senderName);
    }
    else
    {
      String message = formatMap.format(
        Formats.UNEXPECTED_ERROR, "Did not expect " + addResult.name() + " when adding warp");
      plugin.sendMessageSync(message, senderName);
    }
  }
}
