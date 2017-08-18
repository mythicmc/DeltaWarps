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

import com.gmail.tracebachi.DeltaWarps.DeltaWarpsConstants.Formats;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwner;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwnerStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class GiveSubCommand implements WarpSubCommand
{
  private static final String COMMAND_USAGE = "/warp give <name> <public|private|faction> <amount>";
  private static final String COMMAND_PERM = "DeltaWarps.Give";

  private final WarpOwnerStorage warpOwnerStorage;
  private final MessageFormatMap formatMap;

  public GiveSubCommand(WarpOwnerStorage warpOwnerStorage, MessageFormatMap formatMap)
  {
    Preconditions.checkNotNull(warpOwnerStorage, "warpOwnerStorage");
    Preconditions.checkNotNull(formatMap, "formatMap");

    this.warpOwnerStorage = warpOwnerStorage;
    this.formatMap = formatMap;
  }

  @Override
  public void onCommand(CommandSender sender, String[] args)
  {
    if (args.length < 4)
    {
      sender.sendMessage(formatMap.format(Formats.USAGE, COMMAND_USAGE));
      return;
    }

    if (!sender.hasPermission(COMMAND_PERM))
    {
      sender.sendMessage(formatMap.format(Formats.NO_PERM, COMMAND_PERM));
      return;
    }

    String warpTypeString = args[2];
    WarpType warpType = WarpType.fromString(warpTypeString);

    if (warpType == null)
    {
      sender.sendMessage(formatMap.format(Formats.INVALID_WARP_TYPE, warpTypeString));
      return;
    }

    String amountStr = args[3];
    Integer amount = parseInt(amountStr);

    if (amount == null || amount == 0)
    {
      sender.sendMessage(formatMap.format(Formats.INVALID_AMOUNT, amountStr));
      return;
    }

    int newExtraNormalWarps = 0;
    int newExtraFactionWarps = 0;

    switch (warpType)
    {
      case PUBLIC:
      case PRIVATE:
        newExtraNormalWarps += amount;
        break;
      case FACTION:
        newExtraFactionWarps += amount;
        break;
    }

    String warpOwnerName = args[1];
    WarpOwnerStorage.Result result = addOrUpdateWarpOwner(
      warpOwnerName, newExtraNormalWarps, newExtraFactionWarps);

    if (result == WarpOwnerStorage.Result.SUCCESS)
    {
      sender.sendMessage(formatMap.format(Formats.WARP_OWNER_UPDATED, warpOwnerName));
    }
    else
    {
      sender.sendMessage(formatMap.format(Formats.UNEXPECTED_ERROR,
        "Did not expect " + result.name() + " when updating warp owner"));
    }
  }

  private Integer parseInt(String source)
  {
    try
    {
      return Integer.parseInt(source);
    }
    catch (NumberFormatException ex)
    {
      return null;
    }
  }

  private WarpOwnerStorage.Result addOrUpdateWarpOwner(
    String warpOwnerName, int newExtraNormalWarps, int newExtraFactionWarps)
  {
    WarpOwner warpOwner = warpOwnerStorage.getByName(warpOwnerName);

    // Add a new warp owner if one does not exist
    if (warpOwner == null)
    {
      warpOwner = new WarpOwner();
      warpOwner.setName(warpOwnerName);
      warpOwner.setExtraNormalWarps(newExtraNormalWarps);
      warpOwner.setExtraFactionWarps(newExtraFactionWarps);

      return warpOwnerStorage.add(warpOwner);
    }
    else
    {
      newExtraNormalWarps += warpOwner.getExtraNormalWarps();
      newExtraFactionWarps += warpOwner.getExtraFactionWarps();

      warpOwner.setExtraNormalWarps(newExtraNormalWarps);
      warpOwner.setExtraFactionWarps(newExtraFactionWarps);

      return warpOwnerStorage.updateByName(warpOwner);
    }
  }
}
