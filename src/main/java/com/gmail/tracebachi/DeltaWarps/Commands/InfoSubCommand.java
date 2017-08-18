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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class InfoSubCommand implements WarpSubCommand
{
  private static final String COMMAND_USAGE = "/warp info <warp|player|faction> <name>";
  private static final String COMMAND_PERM = "DeltaWarps.Info";
  private static final String PERM_WARP_SEE_COORDS = "DeltaWarps.Info.Warp.SeeCoords";
  private static final String PERM_PLAYER_SEE_PRIVATE_WARPS = "DeltaWarps.Info.Player.SeePrivateWarps";

  private final DeltaWarpsPlugin plugin;
  private final String serverName;
  private final FactionsHelper factionsHelper;
  private final WarpStorage warpStorage;
  private final WarpOwnerStorage warpOwnerStorage;
  private final MessageFormatMap formatMap;
  private final Executor executor;

  public InfoSubCommand(
    DeltaWarpsPlugin plugin, String serverName, FactionsHelper factionsHelper, WarpStorage warpStorage,
    WarpOwnerStorage warpOwnerStorage, MessageFormatMap formatMap, Executor executor)
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
    this.factionsHelper = factionsHelper;
    this.warpStorage = warpStorage;
    this.warpOwnerStorage = warpOwnerStorage;
    this.formatMap = formatMap;
    this.executor = executor;
  }

  @Override
  public void onCommand(CommandSender sender, String[] args)
  {
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

    String senderName = sender.getName();
    String infoType = args[1].toLowerCase();

    // For warp information by warp name
    if (infoType.startsWith("w"))
    {
      if (args.length < 3)
      {
        sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
        return;
      }

      boolean canSeeCoords = sender.hasPermission(PERM_WARP_SEE_COORDS);
      boolean canSeePrivateWarps = sender.hasPermission(PERM_PLAYER_SEE_PRIVATE_WARPS);
      String warpName = args[2];

      // Get the warp based on name
      executor.execute(() -> getWarpInfo(senderName, warpName, canSeeCoords, canSeePrivateWarps));
      return;
    }

    // For warp information for a player/name
    if (infoType.startsWith("p"))
    {
      String ownerName = (args.length >= 3) ? args[2] : senderName;
      boolean canSeePrivateWarps = sender.hasPermission(PERM_PLAYER_SEE_PRIVATE_WARPS);

      // Get the warps for a warp owner
      executor.execute(() -> getPlayerWarpInfo(senderName, ownerName, canSeePrivateWarps));
      return;
    }

    // For warp information for a faction
    if (infoType.startsWith("f"))
    {
      // Check if Factions is enabled
      if (!factionsHelper.isFactionsEnabled())
      {
        sender.sendMessage(formatMap.format(Formats.FACTIONS_NOT_ENABLED));
        return;
      }

      Faction faction;

      if (args.length >= 3)
      {
        faction = factionsHelper.getFactionByName(args[2]);
      }
      else if (sender instanceof Player)
      {
        faction = factionsHelper.getMPlayer((Player) sender).getFaction();
      }
      else
      {
        sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
        return;
      }

      // Check if the faction exists
      if (faction == null)
      {
        sender.sendMessage(formatMap.format(Formats.FACTION_NOT_FOUND, args[2]));
        return;
      }

      String factionName = faction.getName();
      String factionId = faction.getId();

      // Get warps for a faction
      executor.execute(() -> getFactionWarpInfo(senderName, factionId, factionName));
      return;
    }

    sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
  }

  private void getWarpInfo(
    String senderName, String warpName, boolean canSeeCoords, boolean canSeePrivateWarps)
  {
    String message;
    Warp warp = warpStorage.getByName(warpName);

    // Check if the warp was found
    if (warp == null)
    {
      message = formatMap.format(Formats.WARP_NOT_FOUND, warpName);
      plugin.sendMessageSync(message, senderName);
      return;
    }

    // Get the owner for the warp
    WarpOwner warpOwner = warpOwnerStorage.getById(warp.getOwnerId());

    if (warpOwner == null)
    {
      message = formatMap.format(
        Formats.UNEXPECTED_ERROR, "Expected to find warpOwner when getting warp info");
      plugin.sendMessageSync(message, senderName);
      return;
    }

    boolean senderIsWarpOwner = warpOwner.getName().equalsIgnoreCase(senderName);

    // Check if the warp is private and if the sender can see the warp
    if (warp.getType() == WarpType.PRIVATE)
    {
      if (!canSeePrivateWarps && !senderIsWarpOwner)
      {
        message = formatMap.format(Formats.WARP_NOT_OWNED_BY_NAME, warpName, senderName);
        plugin.sendMessageSync(message, senderName);
        return;
      }
    }

    List<String> messageList = new ArrayList<>();

    messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Name", warp.getName()));
    messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Owner", warpOwner.getName()));
    messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Type", warp.getType().name()));

    // Check if the sender can see the warp coordinates
    if (canSeeCoords || senderIsWarpOwner)
    {
      messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "X", warp.getX()));
      messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Y", warp.getY()));
      messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Z", warp.getZ()));
      messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Yaw", warp.getYaw()));
      messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Pitch", warp.getPitch()));
    }

    messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "World", warp.getWorld()));
    messageList.add(formatMap.format(Formats.WARP_INFO_WARP_LINE, "Server", warp.getServer()));

    // Send the messages
    plugin.sendMessagesSync(messageList, senderName);
  }

  private void getPlayerWarpInfo(String senderName, String playerName, boolean canSeePrivateWarps)
  {
    List<String> messageList = new ArrayList<>();
    WarpOwner warpOwner = warpOwnerStorage.getByName(playerName);

    // Check if the warp owner was not found
    if (warpOwner == null)
    {
      messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Name", playerName));
      messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Warps", ""));
      messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Extra Normal Warps", 0));
      messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Extra Faction Warps", 0));

      plugin.sendMessagesSync(messageList, senderName);
      return;
    }

    boolean senderIsWarpOwner = warpOwner.getName().equalsIgnoreCase(senderName);

    messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Name", warpOwner.getName()));
    messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Warps", ""));

    // Get all warps for the owner
    List<Warp> warpList = warpStorage.getByOwnerId(warpOwner.getId());

    // Add a message for every warp
    for (Warp warp : warpList)
    {
      // Check if the sender can see the warp
      if (senderIsWarpOwner || canSeePrivateWarps || warp.getType() != WarpType.PRIVATE)
      {
        String message = formatMap.format(
          Formats.WARP_INFO_PLAYER_WARP, warp.getName(), warp.getType(), warp.getServer());
        messageList.add(message);
      }
    }

    messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Extra Normal Warps",
      warpOwner.getExtraNormalWarps()));
    messageList.add(formatMap.format(Formats.WARP_INFO_PLAYER_LINE, "Extra Faction Warps",
      warpOwner.getExtraFactionWarps()));

    // Send the messages
    plugin.sendMessagesSync(messageList, senderName);
  }

  private void getFactionWarpInfo(String senderName, String factionId, String factionName)
  {
    List<Warp> warpList = warpStorage.getFactionWarps(factionId, serverName);
    List<String> messageList = new ArrayList<>();

    messageList.add(formatMap.format(Formats.WARP_INFO_FACTION_LINE, "Name", factionName));

    // Add a message for every warp
    for (Warp warp : warpList)
    {
      messageList.add(formatMap.format(Formats.WARP_INFO_FACTION_WARP_NAME, warp.getName()));
    }

    // Send the messages
    plugin.sendMessagesSync(messageList, senderName);
  }
}
