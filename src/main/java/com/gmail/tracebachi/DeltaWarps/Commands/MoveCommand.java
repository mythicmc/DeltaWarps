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
import com.gmail.tracebachi.DeltaWarps.Runnables.MoveWarpRunnable;
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class MoveCommand implements IWarpCommand
{
    private final String serverName;
    private DeltaWarps plugin;

    public MoveCommand(String serverName, DeltaWarps plugin)
    {
        this.serverName = serverName;
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
        String warpName = args[1];

        if(!(sender instanceof Player))
        {
            sender.sendMessage(Prefixes.FAILURE + "Only players can move warps.");
            return;
        }

        Player player = (Player) sender;

        if(!sender.hasPermission("DeltaWarps.Move"))
        {
            player.sendMessage(Settings.noPermission("DeltaWarps.Move"));
            return;
        }

        if(!Settings.isWarpEditingEnabled() && !player.hasPermission("DeltaWarps.Staff.Move"))
        {
            player.sendMessage(Prefixes.FAILURE + "Moving warps is not enabled on this server.");
            return;
        }

        if(reserved.contains(warpName))
        {
            player.sendMessage(Prefixes.FAILURE + Prefixes.input(warpName) + " is a reserved name.");
            return;
        }

        if(args[0].length() > 31)
        {
            player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to 32 or less characters.");
            return;
        }

        Warp warp;
        MoveWarpRunnable runnable;

        if(Settings.isFactionsEnabled())
        {
            PS locationPS = PS.valueOf(player.getLocation());
            Faction facAtPos = BoardColl.get().getFactionAt(locationPS);
            String factionAtPosId = facAtPos.getId();

            warp = new Warp(warpName, player.getLocation(), WarpType.FACTION, factionAtPosId, serverName);
            runnable = new MoveWarpRunnable(sender.getName(), warp,
                sender.hasPermission("DeltaWarps.Staff.Move"), plugin);
        }
        else
        {
            warp = new Warp(warpName, player.getLocation(), WarpType.PRIVATE, null, serverName);
            runnable = new MoveWarpRunnable(sender.getName(), warp,
                sender.hasPermission("DeltaWarps.Staff.Move"), plugin);
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
