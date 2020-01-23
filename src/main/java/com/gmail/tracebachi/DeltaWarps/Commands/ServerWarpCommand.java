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
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.concurrent.Executor;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ServerWarpCommand implements CommandExecutor, Registerable
{
  private static final String COMMAND_NAME = "serverwarp";
  private static final String COMMAND_USAGE = "/serverwarp <name>";
  private static final String COMMAND_PERM = "DeltaWarps.ServerWarp";

  private final DeltaWarpsPlugin plugin;
  private final String serverName;
  private final WarpStorage warpStorage;
  private final MessageFormatMap formatMap;
  private final Executor executor;

  public ServerWarpCommand(
    DeltaWarpsPlugin plugin, String serverName, WarpStorage warpStorage, MessageFormatMap formatMap,
    Executor executor)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");
    Preconditions.checkNotNull(warpStorage, "warpStorage");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(executor, "executor");

    this.plugin = plugin;
    this.serverName = serverName;
    this.warpStorage = warpStorage;
    this.formatMap = formatMap;
    this.executor = executor;
  }

  @Override
  public void register()
  {
    plugin.getCommand(COMMAND_NAME).setExecutor(this);
  }

  @Override
  public void unregister()
  {
    plugin.getCommand(COMMAND_NAME).setExecutor(null);
  }

  @Override
  public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
  {
    if (!(sender instanceof Player))
    {
      sender.sendMessage(formatMap.format(Formats.PLAYER_ONLY_COMMAND, COMMAND_NAME));
      return true;
    }

    if (args.length != 1)
    {
      sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
      return true;
    }

    Player player = (Player) sender;

    if (!player.hasPermission(COMMAND_PERM))
    {
      player.sendMessage(formatMap.format(Formats.NO_PERM, COMMAND_PERM));
      return true;
    }

    String warpName = args[0].toLowerCase();

    if (WarpSubCommand.RESERVED_NAMES.contains(warpName))
    {
      player.sendMessage(formatMap.format(Formats.INVALID_WARP_NAME, warpName, "RESERVED_NAME"));
      return true;
    }

    if (warpName.length() > 31)
    {
      player.sendMessage(formatMap.format(Formats.INVALID_WARP_NAME, warpName, "TOO_LONG"));
      return true;
    }

    String senderName = player.getName();
    Location playerLocation = player.getLocation();
    Warp warp = new Warp();
    warp.setName(warpName);
    warp.setOwnerId(1); // Server warp owner is always ownerId = 1
    warp.setLocation(playerLocation);
    warp.setType(WarpType.PUBLIC);
    warp.setFaction(null);
    warp.setServer(serverName);

    executor.execute(() -> addServerWarp(senderName, warp));
    return true;
  }

  private void addServerWarp(String senderName, Warp warpToAdd)
  {
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
