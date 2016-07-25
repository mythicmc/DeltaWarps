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
import com.gmail.tracebachi.DeltaWarps.Runnables.GetFactionWarpsRunnable;
import com.gmail.tracebachi.DeltaWarps.Runnables.GetPlayerWarpsRunnable;
import com.gmail.tracebachi.DeltaWarps.Runnables.GetWarpInfoRunnable;
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.google.common.base.Preconditions;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.FAILURE;
import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.input;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/19/15.
 */
public class InfoCommand implements IWarpCommand
{
    private final String serverName;
    private DeltaWarps plugin;

    public InfoCommand(String serverName, DeltaWarps plugin)
    {
        this.serverName = serverName;
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
        String type = args[1].toLowerCase();

        if(type.startsWith("w"))
        {
            if(args.length < 3)
            {
                sender.sendMessage(FAILURE + "Warp name not specified.");
            }
            else
            {
                getWarp(sender, args[2]);
            }
        }
        else if(type.startsWith("p"))
        {
            if(args.length >= 3)
            {
                getPlayerWarps(sender, args[2]);
            }
            else
            {
                getPlayerWarps(sender, sender.getName());
            }
        }
        else if(type.startsWith("f"))
        {
            if(!Settings.isFactionsEnabled())
            {
                sender.sendMessage(FAILURE + "Factions is not enabled on this server.");
                return;
            }

            if(!(sender instanceof Player))
            {
                if(args.length < 3)
                {
                    sender.sendMessage(FAILURE + "No faction specified.");
                }
                else
                {
                    String factionName = args[2];
                    Faction faction = FactionColl.get().getByName(factionName);

                    if(faction == null)
                    {
                        sender.sendMessage(FAILURE + input(factionName) +
                            " does not exist on this server.");
                    }
                    else
                    {
                        getFactionWarps(sender, faction);
                    }
                }
            }
            else
            {
                if(args.length >= 3)
                {
                    String factionName = args[2];
                    Faction faction = FactionColl.get().getByName(factionName);

                    if(faction == null)
                    {
                        sender.sendMessage(FAILURE + input(factionName) +
                            " does not exist on this server.");
                    }
                    else
                    {
                        getFactionWarps(sender, faction);
                    }
                }
                else
                {
                    MPlayer mPlayer = MPlayer.get(sender);
                    getFactionWarps(sender, mPlayer.getFaction());
                }
            }
        }
        else
        {
            sender.sendMessage(FAILURE + "Unknown info type. Only W, P, and F are valid.");
        }
    }

    private void getWarp(CommandSender sender, String warpName)
    {
        GetWarpInfoRunnable runnable = new GetWarpInfoRunnable(
            sender.getName(),
            warpName,
            sender.hasPermission("DeltaWarps.Staff.Info"),
            plugin);
        DeltaExecutor.instance().execute(runnable);
    }

    private void getPlayerWarps(CommandSender sender, String playerName)
    {
        GroupLimits groupLimits = null;

        if(sender instanceof Player && sender.getName().equals(playerName))
        {
            groupLimits = Settings.getGroupLimitsForSender((Player) sender);
        }

        GetPlayerWarpsRunnable runnable = new GetPlayerWarpsRunnable(
            sender.getName(),
            playerName,
            groupLimits,
            sender.hasPermission("DeltaWarps.Staff.Info"),
            plugin);
        DeltaExecutor.instance().execute(runnable);
    }

    private void getFactionWarps(CommandSender sender, Faction faction)
    {
        Preconditions.checkNotNull(faction, "Faction was null.");

        GetFactionWarpsRunnable runnable = new GetFactionWarpsRunnable(
            sender.getName(),
            faction.getName(),
            faction.getId(),
            serverName,
            plugin);
        DeltaExecutor.instance().execute(runnable);
    }
}
