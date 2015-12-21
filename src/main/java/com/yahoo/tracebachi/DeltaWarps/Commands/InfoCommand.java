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

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.GetFactionWarpsRunnable;
import com.yahoo.tracebachi.DeltaWarps.Runnables.GetPlayerWarpsRunnable;
import com.yahoo.tracebachi.DeltaWarps.Runnables.GetWarpInfoRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.GroupLimits;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/19/15.
 */
public class InfoCommand implements IWarpCommand
{
    private final String serverName;
    private HashMap<String, GroupLimits> groupLimits;
    private DeltaWarpsPlugin plugin;

    public InfoCommand(String serverName, HashMap<String, GroupLimits> groupLimits, DeltaWarpsPlugin plugin)
    {
        this.serverName = serverName;
        this.groupLimits = groupLimits;
        this.plugin = plugin;
    }

    @Override
    public void shutdown()
    {
        groupLimits = null;
        plugin = null;
    }

    @Override
    public void onCommand(CommandSender sender, String[] args)
    {
        String type = args[1].toLowerCase();

        if(type.startsWith("w"))
        {
            if(args.length < 3)
            {
                sender.sendMessage(Prefixes.FAILURE + "Warp name not specified.");
            }
            else
            {
                getWarp(sender, args[2]);
            }
        }
        else if(type.startsWith("p"))
        {
            String playerName = (args.length >= 3) ? args[2] : sender.getName();
            getPlayerWarps(sender, playerName);
        }
        else if(type.startsWith("f"))
        {
            if(!(sender instanceof Player))
            {
                if(args.length >= 3)
                {
                    String factionName = args[2];
                    getFactionWarps(sender, factionName);
                }
                else
                {
                    sender.sendMessage(Prefixes.FAILURE + "No faction specified.");
                }
            }
            else
            {
                MPlayer mPlayer = MPlayer.get(sender);
                String factionName = (args.length >= 3) ? args[2] : mPlayer.getFactionId();
                getFactionWarps(sender, factionName);
            }
        }
        else
        {
            sender.sendMessage(Prefixes.FAILURE + "Unknown info type. Only W, P, and F are valid.");
        }
    }

    private void getWarp(CommandSender sender, String warpName)
    {
        GetWarpInfoRunnable runnable = new GetWarpInfoRunnable(sender.getName(), warpName,
            sender.hasPermission("DeltaWarps.Staff.Info"), plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private void getPlayerWarps(CommandSender sender, String playerName)
    {
        GetPlayerWarpsRunnable runnable = new GetPlayerWarpsRunnable(sender.getName(), playerName,
            getGroupLimitsForSender(sender), sender.hasPermission("DeltaWarps.Staff.Info"), plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private void getFactionWarps(CommandSender sender, String factionName)
    {
        Faction faction = FactionColl.get().getByName(factionName);

        if(faction == null)
        {
            sender.sendMessage(Prefixes.FAILURE + "Faction " +
                ChatColor.WHITE + factionName +
                ChatColor.GRAY + " does not exist on this server.");
        }
        else
        {
            GetFactionWarpsRunnable runnable = new GetFactionWarpsRunnable(
                sender.getName(), factionName, faction.getId(), serverName, plugin);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    private GroupLimits getGroupLimitsForSender(CommandSender sender)
    {
        if(!(sender instanceof Player))
        {
            return new GroupLimits(0, 0);
        }

        Player player = (Player) sender;
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
