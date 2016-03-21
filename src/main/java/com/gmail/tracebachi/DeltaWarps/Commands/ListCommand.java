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

import com.gmail.tracebachi.DeltaExecutor.DeltaExecutor;
import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import com.gmail.tracebachi.DeltaWarps.Runnables.ListWarpsRunnable;
import com.gmail.tracebachi.DeltaWarps.Settings;
import org.bukkit.command.CommandSender;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/20/15.
 */
public class ListCommand implements IWarpCommand
{
    private DeltaWarps plugin;

    public ListCommand(DeltaWarps plugin)
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
        if(!sender.hasPermission("DeltaWarps.List"))
        {
            sender.sendMessage(Settings.noPermission("DeltaWarps.List"));
            return;
        }

        int page = 0;

        if(args.length >= 2)
        {
            page = parseInt(args[1], 0);
        }

        ListWarpsRunnable runnable = new ListWarpsRunnable(sender.getName(), page, plugin);
        DeltaExecutor.instance().execute(runnable);
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
