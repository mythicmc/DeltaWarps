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

import com.gmail.tracebachi.DeltaRedis.Shared.Prefixes;
import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import com.gmail.tracebachi.DeltaWarps.Runnables.DeleteWarpRunnable;
import com.gmail.tracebachi.DeltaWarps.Settings;
import org.bukkit.command.CommandSender;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class RemoveCommand implements IWarpCommand
{
    private DeltaWarps plugin;

    public RemoveCommand(DeltaWarps plugin)
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
        String warpName = args[1].toLowerCase();

        if(!sender.hasPermission("DeltaWarps.Remove"))
        {
            sender.sendMessage(Settings.noPermission("DeltaWarps.Remove"));
            return;
        }

        if(!Settings.isWarpEditingEnabled() && !sender.hasPermission("DeltaWarps.Staff.Remove"))
        {
            sender.sendMessage(Prefixes.FAILURE + "Removing warps is not enabled on this server.");
            return;
        }

        if(reserved.contains(warpName))
        {
            sender.sendMessage(Prefixes.FAILURE + Prefixes.input(warpName) + " is a reserved name.");
            return;
        }

        if(warpName.length() > 31)
        {
            sender.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to 32 or less characters.");
            return;
        }

        DeleteWarpRunnable runnable = new DeleteWarpRunnable(sender.getName(), warpName,
            sender.hasPermission("DeltaWarps.Staff.Remove"), plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
