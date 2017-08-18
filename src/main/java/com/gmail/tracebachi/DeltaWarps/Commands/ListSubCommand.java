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
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class ListSubCommand implements WarpSubCommand
{
  private static final String COMMAND_PERM = "DeltaWarps.List";

  private final DeltaWarpsPlugin plugin;
  private final int pageSize;
  private final WarpStorage warpStorage;
  private final MessageFormatMap formatMap;
  private final Executor executor;

  public ListSubCommand(
    DeltaWarpsPlugin plugin, int pageSize, WarpStorage warpStorage, MessageFormatMap formatMap, Executor executor)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    Preconditions.checkNotNull(warpStorage, "warpStorage");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(executor, "executor");

    this.plugin = plugin;
    this.pageSize = Math.max(5, pageSize);
    this.warpStorage = warpStorage;
    this.formatMap = formatMap;
    this.executor = executor;
  }

  @Override
  public void onCommand(CommandSender sender, String[] args)
  {
    if (!sender.hasPermission(COMMAND_PERM))
    {
      sender.sendMessage(formatMap.format(Formats.NO_PERM, COMMAND_PERM));
      return;
    }

    String senderName = sender.getName();
    int page = (args.length >= 2) ? parseNonNegativeInt(args[1]) : 0;

    executor.execute(() -> listPublicWarps(senderName, page));
  }

  private void listPublicWarps(String senderName, int page)
  {
    List<Warp> warpList = warpStorage.getPublicWarps(pageSize, pageSize * page);
    List<String> messageList = new ArrayList<>();

    messageList.add(formatMap.format(Formats.WARP_LIST_PAGE, page));

    // Add a message for each warp
    for (Warp warp : warpList)
    {
      String message = formatMap.format(Formats.WARP_LIST_WARP_NAME, warp.getName());
      messageList.add(message);
    }

    plugin.sendMessagesSync(messageList, senderName);
  }

  private int parseNonNegativeInt(String source)
  {
    try
    {
      return Math.max(0, Integer.parseInt(source));
    }
    catch (NumberFormatException ex)
    {
      return 0;
    }
  }
}
