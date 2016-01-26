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

import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import com.gmail.tracebachi.DeltaWarps.Runnables.AddWarpRunnable;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.gmail.tracebachi.DeltaRedis.Shared.Prefixes;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class AddCommand implements IWarpCommand
{
    private final String serverName;
    private HashMap<String, GroupLimits> groupLimits;
    private DeltaWarps plugin;

    public AddCommand(String serverName, HashMap<String, GroupLimits> groupLimits, DeltaWarps plugin)
    {
        this.serverName = serverName;
        this.groupLimits = groupLimits;
        this.plugin = plugin;
    }

    @Override
    public void shutdown()
    {
        this.groupLimits = null;
        this.plugin = null;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        String warpTypeString = args[1];
        String warpName = args[2].toLowerCase();

        if(!(sender instanceof Player))
        {
            sender.sendMessage(Prefixes.FAILURE + "Only players can add warps.");
            return;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("DeltaWarps.Player.Add"))
        {
            player.sendMessage(Prefixes.FAILURE + "You do not have permission to add warps.");
            return;
        }

        WarpType type = WarpType.fromString(warpTypeString);
        if(type == null)
        {
            player.sendMessage(Prefixes.FAILURE + "Unknown warp type: " + ChatColor.WHITE + warpTypeString);
            return;
        }

        if(warpName.length() >= 30)
        {
            player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to less than 30 characters.");
            return;
        }

        if(reserved.contains(warpName))
        {
            player.sendMessage(Prefixes.FAILURE + "That is a reserved name.");
            return;
        }

        String faction = null;
        Location playerLocation = player.getLocation();
        MPlayer mPlayer = MPlayer.get(player);
        PS locationPS = PS.valueOf(playerLocation);
        Faction facAtPos = BoardColl.get().getFactionAt(locationPS);

        if(type == WarpType.FACTION)
        {
            if(!mPlayer.hasFaction())
            {
                player.sendMessage(Prefixes.FAILURE +
                    "Faction warps cannot be created without a faction.");
                return;
            }

            if(!mPlayer.getFactionId().equals(facAtPos.getId()))
            {
                player.sendMessage(Prefixes.FAILURE +
                    "Faction warps can only be created on land owned by your faction.");
                return;
            }

            faction = facAtPos.getId();
        }
        else
        {
            if(!facAtPos.isNone())
            {
                if(!mPlayer.getFactionId().equals(facAtPos.getId()))
                {
                    player.sendMessage(Prefixes.FAILURE +
                        "Warps can only be created on land owned by your faction or wilderness.");
                    return;
                }
            }
        }

        GroupLimits groupLimit = getGroupLimitsForSender(player);
        Warp warp = new Warp(warpName, player.getLocation(), type, faction, serverName);
        AddWarpRunnable runnable = new AddWarpRunnable(sender.getName(), warp, groupLimit, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private GroupLimits getGroupLimitsForSender(Player player)
    {
        for(Map.Entry<String, GroupLimits> entry : groupLimits.entrySet())
        {
            if(player.hasPermission("DeltaWarps.Group." + entry.getKey()))
            {
                return entry.getValue();
            }
        }
        return new GroupLimits(0, 0);
    }
}
