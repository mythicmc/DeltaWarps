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
package com.gmail.tracebachi.DeltaWarps.Commands;

import com.gmail.tracebachi.DeltaRedis.Spigot.Prefixes;
import com.gmail.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.gmail.tracebachi.DeltaWarps.Runnables.ListWarpsRunnable;
import org.bukkit.command.CommandSender;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/20/15.
 */
public class ListCommand implements IWarpCommand
{
    private DeltaWarpsPlugin plugin;

    public ListCommand(DeltaWarpsPlugin plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void shutdown()
    {
        plugin = null;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        if(!sender.hasPermission("DeltaWarps.Player.List"))
        {
            sender.sendMessage(Prefixes.FAILURE + "You do not have permission to list warps.");
            return;
        }

        int page = 0;
        if(args.length >= 2)
        {
            page = parseInt(args[1], 0);
        }

        ListWarpsRunnable runnable = new ListWarpsRunnable(sender.getName(), page, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private int parseInt(String source, int def)
    {
        try
        {
            return Integer.parseInt(source);
        }
        catch(NumberFormatException ex)
        {
            return def;
        }
    }
}
