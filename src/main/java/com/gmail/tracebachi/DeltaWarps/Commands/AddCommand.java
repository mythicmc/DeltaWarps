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
import com.gmail.tracebachi.DeltaWarps.Runnables.AddWarpRunnable;
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class AddCommand implements IWarpCommand
{
    private final String serverName;
    private DeltaWarps plugin;

    public AddCommand(String serverName, DeltaWarps plugin)
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
        String warpName = args[1].toLowerCase();
        String warpTypeString = (args.length >= 3) ? args[2] : "PRIVATE";

        if(!(sender instanceof Player))
        {
            sender.sendMessage(Prefixes.FAILURE + "Only players can add warps.");
            return;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("DeltaWarps.Add"))
        {
            player.sendMessage(Settings.noPermission("DeltaWarps.Add"));
            return;
        }

        if(!Settings.isWarpEditingEnabled() && !player.hasPermission("DeltaWarps.Staff.Add"))
        {
            player.sendMessage(Prefixes.FAILURE + "Adding warps is not enabled on this server.");
            return;
        }

        if(reserved.contains(warpName))
        {
            player.sendMessage(Prefixes.FAILURE + Prefixes.input(warpName) + " is a reserved name.");
            return;
        }

        if(warpName.length() > 31)
        {
            player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to 32 or less characters.");
            return;
        }

        WarpType type = WarpType.fromString(warpTypeString);

        if(type == WarpType.UNKNOWN)
        {
            player.sendMessage(Prefixes.FAILURE + "Unknown warp type: " + Prefixes.input(warpTypeString));
            return;
        }

        GroupLimits groupLimit = Settings.getGroupLimitsForSender(player);
        Warp warp;

        if(type == WarpType.FACTION)
        {
            if(!Settings.isFactionsEnabled())
            {
                player.sendMessage(Prefixes.FAILURE + "Factions is not enabled on this server.");
                return;
            }

            Location playerLocation = player.getLocation();
            MPlayer mPlayer = MPlayer.get(player);
            PS locationPS = PS.valueOf(playerLocation);
            Faction facAtPos = BoardColl.get().getFactionAt(locationPS);

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

            warp = new Warp(warpName, player.getLocation(), type, facAtPos.getId(), serverName);
        }
        else
        {
            warp = new Warp(warpName, player.getLocation(), type, null, serverName);
        }

        AddWarpRunnable runnable = new AddWarpRunnable(sender.getName(), warp, groupLimit, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
