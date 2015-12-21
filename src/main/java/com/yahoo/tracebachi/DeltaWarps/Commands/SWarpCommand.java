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

import com.massivecraft.factions.entity.FactionColl;
import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.AddServerWarpRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class SWarpCommand implements CommandExecutor
{
    private final String serverName;
    private DeltaWarpsPlugin plugin;

    public SWarpCommand(String serverName, DeltaWarpsPlugin plugin)
    {
        this.serverName = serverName;
        this.plugin = plugin;
    }

    public void shutdown()
    {
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(Prefixes.FAILURE + "Only players can add warps.");
            return true;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("DeltaWarps.Staff.ServerWarp"))
        {
            player.sendMessage(Prefixes.FAILURE + "You do not have permission to add server warps.");
            return true;
        }

        if(args.length == 0)
        {
            player.sendMessage(Prefixes.INFO + "/swarp <warp name>");
            return true;
        }

        String warpName = args[0].toLowerCase();
        if(warpName.length() >= 30)
        {
            player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to less than 30 characters.");
            return true;
        }

        if(IWarpCommand.reserved.contains(warpName))
        {
            player.sendMessage(Prefixes.FAILURE + "That is a reserved name.");
            return true;
        }

        Warp warp = new Warp(warpName, player.getLocation(), WarpType.PUBLIC,
            FactionColl.get().getSafezone().getId(), serverName);
        AddServerWarpRunnable runnable = new AddServerWarpRunnable(sender.getName(), warp, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        return true;
    }
}
