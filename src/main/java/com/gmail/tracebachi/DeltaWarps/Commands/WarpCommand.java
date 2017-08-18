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
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwnerStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpStorage;
import com.gmail.tracebachi.DeltaWarps.WarpTeleporter;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.concurrent.Executor;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class WarpCommand implements CommandExecutor, Registerable
{
  private static final String COMMAND_NAME = "warp";
  private static final String COMMAND_USAGE = "/warp [add, remove, move, info, list, give]";

  private final DeltaWarpsPlugin plugin;
  private final MessageFormatMap formatMap;
  private final AddSubCommand addSubCommand;
  private final GiveSubCommand giveSubCommand;
  private final InfoSubCommand infoSubCommand;
  private final ListSubCommand listSubCommand;
  private final MoveSubCommand moveSubCommand;
  private final RemoveSubCommand removeSubCommand;
  private final UseSubCommand useSubCommand;

  public WarpCommand(
    DeltaWarpsPlugin plugin, String serverName, boolean allowedToAddWarps, boolean allowedToMoveWarps,
    int pageSize, FactionsHelper factionsHelper, WarpStorage warpStorage,
    WarpOwnerStorage warpOwnerStorage, WarpTeleporter warpTeleporter, MessageFormatMap formatMap,
    Executor executor)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    Preconditions.checkNotNull(formatMap, "formatMap");

    this.plugin = plugin;
    this.formatMap = formatMap;
    this.addSubCommand = new AddSubCommand(
      plugin, serverName, allowedToAddWarps, factionsHelper, warpStorage, warpOwnerStorage,
      formatMap, executor);
    this.giveSubCommand = new GiveSubCommand(warpOwnerStorage, formatMap);
    this.infoSubCommand = new InfoSubCommand(
      plugin, serverName, factionsHelper, warpStorage, warpOwnerStorage, formatMap, executor);
    this.listSubCommand = new ListSubCommand(plugin, pageSize, warpStorage, formatMap, executor);
    this.moveSubCommand = new MoveSubCommand(
      plugin, serverName, allowedToMoveWarps, factionsHelper, warpStorage, warpOwnerStorage,
      formatMap, executor);
    this.removeSubCommand = new RemoveSubCommand(
      plugin, warpStorage, warpOwnerStorage, formatMap, executor);
    this.useSubCommand = new UseSubCommand(
      plugin, serverName, factionsHelper, warpStorage, warpOwnerStorage, warpTeleporter, formatMap,
      executor);
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
    if (args.length == 0)
    {
      sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
      return true;
    }

    String subCommand = args[0];

    if (subCommand.equalsIgnoreCase("add") || subCommand.equalsIgnoreCase("set"))
    {
      addSubCommand.onCommand(sender, args);
      return true;
    }

    if (subCommand.equalsIgnoreCase("give"))
    {
      giveSubCommand.onCommand(sender, args);
      return true;
    }

    if (subCommand.equalsIgnoreCase("info"))
    {
      infoSubCommand.onCommand(sender, args);
      return true;
    }

    if (subCommand.equalsIgnoreCase("list"))
    {
      listSubCommand.onCommand(sender, args);
      return true;
    }

    if (subCommand.equalsIgnoreCase("move"))
    {
      moveSubCommand.onCommand(sender, args);
      return true;
    }

    if (subCommand.equalsIgnoreCase("remove") || subCommand.equalsIgnoreCase("delete"))
    {
      removeSubCommand.onCommand(sender, args);
      return true;
    }

    useSubCommand.onCommand(sender, args);
    return true;
  }
}
