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
import com.gmail.tracebachi.DeltaWarps.WarpTeleporter;
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
public class UseSubCommand implements WarpSubCommand
{
  private static final String PERM_USE_PUBLIC = "DeltaWarps.Use.Public";
  private static final String PERM_USE_PRIVATE = "DeltaWarps.Use.Private";
  private static final String PERM_USE_FACTION = "DeltaWarps.Use.Faction";
  private static final String PERM_USE_SPECIAL_PREFIX = "DeltaWarps.Use.Special.";
  private static final String PERM_FORCE_USE = "DeltaWarps.ForceUse";

  private final DeltaWarpsPlugin plugin;
  private final String serverName;
  private final FactionsHelper factionsHelper;
  private final WarpStorage warpStorage;
  private final WarpOwnerStorage warpOwnerStorage;
  private final WarpTeleporter warpTeleporter;
  private final MessageFormatMap formatMap;
  private final Executor executor;

  public UseSubCommand(
    DeltaWarpsPlugin plugin, String serverName, FactionsHelper factionsHelper, WarpStorage warpStorage,
    WarpOwnerStorage warpOwnerStorage, WarpTeleporter warpTeleporter, MessageFormatMap formatMap,
    Executor executor)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");
    Preconditions.checkNotNull(factionsHelper, "factionsHelper");
    Preconditions.checkNotNull(warpStorage, "warpStorage");
    Preconditions.checkNotNull(warpOwnerStorage, "warpOwnerStorage");
    Preconditions.checkNotNull(warpTeleporter, "warpTeleporter");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(executor, "executor");

    this.plugin = plugin;
    this.serverName = serverName;
    this.factionsHelper = factionsHelper;
    this.warpStorage = warpStorage;
    this.warpOwnerStorage = warpOwnerStorage;
    this.warpTeleporter = warpTeleporter;
    this.formatMap = formatMap;
    this.executor = executor;
  }

  @Override
  public void onCommand(CommandSender sender, String[] args)
  {
    String senderName = sender.getName();
    String warpName = args[0].toLowerCase();

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

    String nameToWarp;
    boolean canUseSpecial;

    // Check if the command was "/warp <name>" or "/warp <name> <player>"
    if (args.length == 1)
    {
      if (!(sender instanceof Player))
      {
        sender.sendMessage(formatMap.format(Formats.PLAYER_ONLY_COMMAND, "use"));
        return;
      }

      Player player = (Player) sender;
      boolean canUsePublic = player.hasPermission(PERM_USE_PUBLIC);
      boolean canUsePrivate = player.hasPermission(PERM_USE_PRIVATE);
      boolean canUseFaction = player.hasPermission(PERM_USE_FACTION);

      // Sender is the player to warp
      nameToWarp = player.getName();
      canUseSpecial = player.hasPermission(PERM_USE_SPECIAL_PREFIX + warpName);

      if (!canUsePublic && !canUsePrivate && !canUseFaction && !canUseSpecial)
      {
        player.sendMessage(formatMap.format(Formats.NO_PERM, PERM_USE_PUBLIC));
        return;
      }
    }
    else
    {
      if (!sender.hasPermission(PERM_FORCE_USE))
      {
        sender.sendMessage(formatMap.format(Formats.NO_PERM, PERM_FORCE_USE));
        return;
      }

      Player player = plugin.getServer().getPlayerExact(args[1]);
      if (player == null)
      {
        sender.sendMessage(formatMap.format(Formats.PLAYER_OFFLINE, args[1]));
        return;
      }

      // Player to warp is whoever the sender stated
      nameToWarp = player.getName();
      canUseSpecial = true;
    }

    executor.execute(() -> getWarpForUse(senderName, nameToWarp, warpName, canUseSpecial));
  }

  private void getWarpForUse(
    String senderName, String nameToWarp, String warpName, boolean canUseSpecial)
  {
    String message;
    Warp warp = warpStorage.getByName(warpName);
    WarpOwner warpOwner = null;

    // Check if the warp was found
    if (warp == null)
    {
      message = formatMap.format(Formats.WARP_NOT_FOUND, warpName);
      plugin.sendMessageSync(message, senderName);
      return;
    }

    WarpType warpType = warp.getType();

    // Get the warp owner if the warp is private and nameToWarp does not have
    // the canUseSpecial permission
    if ((warpType == WarpType.PRIVATE) && !canUseSpecial)
    {
      warpOwner = warpOwnerStorage.getById(warp.getOwnerId());

      if (warpOwner == null)
      {
        message = formatMap.format(
          Formats.UNEXPECTED_ERROR, "Expected to find warpOwner when using warp");
        plugin.sendMessageSync(message, senderName);
        return;
      }
    }

    WarpOwner finalWarpOwner = warpOwner;

    plugin.executeSync(() -> useWarp(senderName, nameToWarp, warp, finalWarpOwner, canUseSpecial));
  }

  private void useWarp(String senderName, String nameToWarp, Warp warp, WarpOwner warpOwner,
    boolean canUseSpecial)
  {
    Player playerToWarp = plugin.getServer().getPlayerExact(nameToWarp);

    // There is nothing to do if the player to warp is offline
    if (playerToWarp == null)
    {
      return;
    }

    String message;
    WarpType warpType = warp.getType();
    String warpName = warp.getName();

    if (warpType == WarpType.PUBLIC)
    {
      // Check if the player has perms to go to public warps
      if (!canUseSpecial && !playerToWarp.hasPermission(PERM_USE_PUBLIC))
      {
        message = formatMap.format(Formats.NO_PERM, PERM_USE_PUBLIC);
        playerToWarp.sendMessage(message);
        return;
      }
    }
    else if (warpType == WarpType.PRIVATE)
    {
      // Check if the player has perms to go to private warps
      if (!canUseSpecial && !playerToWarp.hasPermission(PERM_USE_PRIVATE))
      {
        message = formatMap.format(Formats.NO_PERM, PERM_USE_PRIVATE);
        playerToWarp.sendMessage(message);
        return;
      }

      // Check if the player is the warp owner
      if (!canUseSpecial && !warpOwner.getName().equalsIgnoreCase(nameToWarp))
      {
        message = formatMap.format(Formats.WARP_NOT_OWNED_BY_NAME, warpName, nameToWarp);
        playerToWarp.sendMessage(message);
        return;
      }

    }
    else if (warpType == WarpType.FACTION)
    {
      // Check if the player has perms to go to faction warps
      if (!canUseSpecial && !playerToWarp.hasPermission(PERM_USE_FACTION))
      {
        message = formatMap.format(Formats.NO_PERM, PERM_USE_FACTION);
        plugin.sendMessageSync(message, senderName);
        return;
      }

      // Check if the player needs to have their faction checked
      if (!canUseSpecial)
      {
        // Check if Factions is enabled
        if (!factionsHelper.isFactionsEnabled())
        {
          message = formatMap.format(Formats.FACTIONS_NOT_ENABLED);
          playerToWarp.sendMessage(message);
          return;
        }

        String warpServer = warp.getServer();

        // Check if the faction warp is on the current server
        if (!serverName.equalsIgnoreCase(warpServer))
        {
          message = formatMap.format(Formats.FACTION_WARP_ONLY_USABLE_ON_SAME_SERVER, warpServer);
          playerToWarp.sendMessage(message);
          return;
        }

        Faction playerToWarpFaction = factionsHelper.getMPlayer(playerToWarp).getFaction();
        String warpFaction = warp.getFaction();

        // Check if the player's faction is the same as the warp's faction
        if (playerToWarpFaction == null || !playerToWarpFaction.getId().equals(warpFaction))
        {
          message = formatMap.format(Formats.FACTION_WARP_NOT_OWNED_BY_YOUR_FACTION, warpName);
          playerToWarp.sendMessage(message);
          return;
        }

        Location location = plugin.getLocationFromWarp(warp);
        Faction factionAtLocation = factionsHelper.getFactionAtLocation(location);

        // Check if the faction at the warp location is still owned by the same faction
        if (factionAtLocation == null || !factionAtLocation.getId().equals(warpFaction))
        {
          message = formatMap.format(Formats.FACTION_WARP_NOT_ON_FACTION_LAND, warpName);
          playerToWarp.sendMessage(message);
          return;
        }
      }
    }

    // Move to warp
    warpTeleporter.teleportToWarp(playerToWarp, warp);
  }
}
