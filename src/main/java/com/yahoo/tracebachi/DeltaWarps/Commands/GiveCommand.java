/*
 * This file is part of DeltaWarps.
 *
 * DeltaWarps is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DeltaWarps is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DeltaWarps.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.GiveWarpsRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class GiveCommand implements IWarpCommand
{
    private DeltaWarpsPlugin plugin;

    public GiveCommand(DeltaWarpsPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void shutdown()
    {
        this.plugin = null;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        String receiver = args[1];
        String warpTypeString = args[2];
        String amountString = args[3];

        if(!sender.hasPermission("DeltaWarps.Staff.Give"))
        {
            sender.sendMessage(Prefixes.FAILURE + "You do not have permission to give warps.");
            return;
        }

        WarpType type = WarpType.fromString(warpTypeString);
        if(type == null)
        {
            sender.sendMessage(Prefixes.FAILURE + "Unknown warp type: " + ChatColor.WHITE + warpTypeString);
            return;
        }

        Integer amount = parseInt(amountString);
        if(amount == null || amount == 0)
        {
            sender.sendMessage(Prefixes.FAILURE + "Invalid number: " + ChatColor.WHITE + amountString);
            return;
        }

        GiveWarpsRunnable runnable = new GiveWarpsRunnable(sender.getName(), receiver, type, amount, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private Integer parseInt(String source)
    {
        try
        {
            return Integer.parseInt(source);
        }
        catch(NumberFormatException ex)
        {
            return null;
        }
    }
}
