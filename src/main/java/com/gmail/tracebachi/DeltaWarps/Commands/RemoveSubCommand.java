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
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwner;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwnerStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpStorage;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;

import java.util.concurrent.Executor;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class RemoveSubCommand implements WarpSubCommand
{
  private static final String COMMAND_USAGE = "/warp remove <warp name>";
  private static final String COMMAND_PERM = "DeltaWarps.Remove";
  private static final String COMMAND_PERM_IGNORE_OWNER = "DeltaWarps.Remove.IgnoreOwner";

  private final DeltaWarpsPlugin plugin;
  private final WarpStorage warpStorage;
  private final WarpOwnerStorage warpOwnerStorage;
  private final MessageFormatMap formatMap;
  private final Executor executor;

  public RemoveSubCommand(
    DeltaWarpsPlugin plugin, WarpStorage warpStorage, WarpOwnerStorage warpOwnerStorage,
    MessageFormatMap formatMap, Executor executor)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    Preconditions.checkNotNull(warpOwnerStorage, "warpOwnerStorage");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(executor, "executor");

    this.plugin = plugin;
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

    String senderName = sender.getName();
    boolean ignoreOwner = sender.hasPermission(COMMAND_PERM_IGNORE_OWNER);

    executor.execute(() -> deleteWarp(warpName, senderName, ignoreOwner));
  }

  private void deleteWarp(String warpName, String senderName, boolean ignoreOwner)
  {
    String message;

    // Check if the warp owner is the sender if the owner cannot be ignored
    if (!ignoreOwner)
    {
      Warp warp = warpStorage.getByName(warpName);

      // Check if a warp was found by that name
      if (warp == null)
      {
        message = formatMap.format(Formats.WARP_NOT_FOUND, warpName);
        plugin.sendMessageSync(message, senderName);
        return;
      }

      WarpOwner warpOwner = warpOwnerStorage.getByName(senderName);

      // If the warpOwner was not found, they won't have any warps which means they don't have
      // any warps they can delete. If the ownerId doesn't match, then the sender cannot delete
      // the warp.
      if (warpOwner == null || warpOwner.getId() != warp.getOwnerId())
      {
        message = formatMap.format(Formats.WARP_NOT_OWNED_BY_NAME, warpName, senderName);
        plugin.sendMessageSync(message, senderName);
        return;
      }
    }

    // Remove the warp
    WarpStorage.Result removeResult = warpStorage.removeByName(warpName);

    if (removeResult == WarpStorage.Result.SUCCESS)
    {
      message = formatMap.format(Formats.WARP_REMOVED, warpName);
    }
    else if (removeResult == WarpStorage.Result.WARP_NOT_FOUND)
    {
      message = formatMap.format(Formats.WARP_NOT_FOUND, warpName);
    }
    else
    {
      message = formatMap.format(
        Formats.UNEXPECTED_ERROR,
        "Did not expect to get " + removeResult.name() + " when removing warp");
    }

    plugin.sendMessageSync(message, senderName);
  }
}
