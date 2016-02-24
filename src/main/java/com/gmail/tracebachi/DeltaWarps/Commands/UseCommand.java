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
import com.gmail.tracebachi.DeltaWarps.Runnables.GetWarpForUseRunnable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class UseCommand implements IWarpCommand
{
    private DeltaWarps plugin;

    public UseCommand(DeltaWarps plugin)
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
        if(args.length == 1)
        {
            if(!(sender instanceof Player))
            {
                sender.sendMessage(Prefixes.FAILURE + "Only players can use warps.");
                return;
            }

            Player player = (Player) sender;
            if(args[0].length() >= 30)
            {
                player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to 32 or less characters.");
                return;
            }

            boolean canUse = player.hasPermission("DeltaWarps.Player.Use.Normal");
            boolean canUseFaction = player.hasPermission("DeltaWarps.Player.Use.Faction");

            if(!canUse && !canUseFaction)
            {
                player.sendMessage(Prefixes.FAILURE + "You do not have permission to use any warps.");
                return;
            }

            GetWarpForUseRunnable runnable = new GetWarpForUseRunnable(
                player.getName(), player.getName(), args[0], plugin);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        }
        else
        {
            if(!sender.hasPermission("DeltaWarps.Staff.ForceUse"))
            {
                sender.sendMessage(Prefixes.FAILURE + "You do not have permission to do that.");
                return;
            }

            if(args[0].length() >= 30)
            {
                sender.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to 32 or less characters.");
                return;
            }

            Player warper = Bukkit.getPlayer(args[1]);
            if(warper == null || !warper.isOnline())
            {
                sender.sendMessage(Prefixes.FAILURE + Prefixes.input(args[1]) + " is not online.");
                return;
            }

            GetWarpForUseRunnable runnable = new GetWarpForUseRunnable(
                sender.getName(), warper.getName(), args[0], plugin);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }
}
