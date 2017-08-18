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
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.Executor;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class MoveSubCommand implements WarpSubCommand
{
  private static final String COMMAND_USAGE = "/warp move <warp name>";
  private static final String COMMAND_PERM = "DeltaWarps.Move";
  private static final String PERM_IGNORE_NOT_ALLOWED = "DeltaWarps.Move.IgnoreNotAllowed";
  private static final String PERM_IGNORE_OWNER = "DeltaWarps.Move.IgnoreOwner";

  private final DeltaWarpsPlugin plugin;
  private final String serverName;
  private final boolean allowedToMoveWarps;
  private final FactionsHelper factionsHelper;
  private final WarpStorage warpStorage;
  private final WarpOwnerStorage warpOwnerStorage;
  private final MessageFormatMap formatMap;
  private final Executor executor;

  public MoveSubCommand(
    DeltaWarpsPlugin plugin, String serverName, boolean allowedToMoveWarps, FactionsHelper factionsHelper,
    WarpStorage warpStorage, WarpOwnerStorage warpOwnerStorage, MessageFormatMap formatMap,
    Executor executor)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");
    Preconditions.checkNotNull(factionsHelper, "factionsHelper");
    Preconditions.checkNotNull(warpOwnerStorage, "warpOwnerStorage");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(executor, "executor");

    this.plugin = plugin;
    this.serverName = serverName;
    this.allowedToMoveWarps = allowedToMoveWarps;
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
      sender.sendMessage(formatMap.format(Formats.PLAYER_ONLY_COMMAND, "move"));
      return;
    }

    if (args.length < 2)
    {
      sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
      return;
    }

    if (!sender.hasPermission(COMMAND_PERM))
    {
      sender.sendMessage(formatMap.format(Formats.NO_PERM, COMMAND_PERM));
      return;
    }

    if (!allowedToMoveWarps && !sender.hasPermission(PERM_IGNORE_NOT_ALLOWED))
    {
      sender.sendMessage(formatMap.format(Formats.NOT_ALLOWED_ON_SERVER, "move"));
      return;
    }

    String warpName = args[1].toLowerCase();

    if (RESERVED_NAMES.contains(warpName))
    {
      sender.sendMessage(formatMap.format(Formats.INVALID_WARP_NAME, warpName, "RESERVED_NAME"));
      return;
    }

    if (warpName.length() > 31)
    {
      sender.sendMessage(formatMap.format(Formats.INVALID_WARP_NAME, warpName, "TOO_LONG"));
      return;
    }

    Player player = (Player) sender;
    Location playerLocation = player.getLocation();
    String factionAtLocId = null;

    if (factionsHelper.isFactionsEnabled())
    {
      Faction factionAtLoc = factionsHelper.getFactionAtLocation(playerLocation);
      if (factionAtLoc != null)
      {
        factionAtLocId = factionAtLoc.getId();
      }
    }

    Warp warp = new Warp();
    warp.setName(warpName);
    warp.setLocation(playerLocation);
    warp.setServer(serverName);

    String senderName = sender.getName();
    boolean ignoreOwner = sender.hasPermission(PERM_IGNORE_OWNER);
    String finalFactionAtLocId = factionAtLocId;

    executor.execute(() -> moveWarp(senderName, warp, ignoreOwner, finalFactionAtLocId));
  }

  private void moveWarp(
    String senderName, Warp updatedWarp, boolean ignoreOwner, String factionAtLocId)
  {
    String message;
    String warpName = updatedWarp.getName();
    Warp foundWarp = warpStorage.getByName(warpName);

    // Check if the warp was found
    if (foundWarp == null)
    {
      message = formatMap.format(Formats.WARP_NOT_FOUND, warpName);
      plugin.sendMessageSync(message, senderName);
      return;
    }

    // Check if the owner is the sender if the owner cannot be ignored
    if (!ignoreOwner)
    {
      WarpOwner warpOwner = warpOwnerStorage.getById(foundWarp.getOwnerId());

      if (warpOwner == null)
      {
        message = formatMap.format(
          Formats.UNEXPECTED_ERROR, "Expected to find warpOwner when moving warp");
        plugin.sendMessageSync(message, senderName);
        return;
      }

      // Check if the warp owner is the sender
      if (!warpOwner.getName().equalsIgnoreCase(senderName))
      {
        message = formatMap.format(Formats.WARP_NOT_OWNED_BY_NAME, warpName, senderName);
        plugin.sendMessageSync(message, senderName);
        return;
      }
    }

    if (foundWarp.getType() == WarpType.FACTION)
    {
      // Check if Factions is enabled
      if (!factionsHelper.isFactionsEnabled())
      {
        plugin.sendMessageSync(formatMap.format(Formats.FACTIONS_NOT_ENABLED), senderName);
        return;
      }

      // Check if the faction in the warp matches the faction at the location
      boolean sameServer = foundWarp.getServer().equalsIgnoreCase(serverName);
      boolean sameFaction = foundWarp.getFaction().equalsIgnoreCase(factionAtLocId);
      if (!sameServer || !sameFaction)
      {
        plugin.sendMessageSync(
          formatMap.format(Formats.FACTION_WARP_ONLY_ON_FACTION_LAND), senderName);
        return;
      }
    }

    // Copy everything but the location of foundWarp to updatedWarp
    updatedWarp.setName(foundWarp.getName());
    updatedWarp.setOwnerId(foundWarp.getOwnerId());
    updatedWarp.setType(foundWarp.getType());
    updatedWarp.setFaction(foundWarp.getFaction());
    updatedWarp.setServer(serverName);

    // Update the warp in storage
    WarpStorage.Result updateResult = warpStorage.update(updatedWarp);
    if (updateResult == WarpStorage.Result.SUCCESS)
    {
      message = formatMap.format(Formats.WARP_MOVED, warpName);
      plugin.sendMessageSync(message, senderName);
    }
    else
    {
      message = formatMap.format(
        Formats.UNEXPECTED_ERROR, "Did not expect " + updateResult.name() + " when moving warp");
      plugin.sendMessageSync(message, senderName);
    }
  }
}
