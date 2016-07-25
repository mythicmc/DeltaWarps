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
package com.gmail.tracebachi.DeltaWarps.Runnables;

import com.gmail.tracebachi.DeltaEssentials.DeltaEssentials;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.gmail.tracebachi.DeltaWarps.PlayerWarpEvent;
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.google.common.base.Preconditions;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/19/15.
 */
public class UseWarpRunnable implements Runnable
{
    private static final String WARP_CHANNEL = "DW-Warp";

    private final DeltaEssentials deltaEssentialsPlugin;
    private final String sender;
    private final String warper;
    private final String warpOwner;
    private final Warp warp;

    public UseWarpRunnable(DeltaEssentials deltaEssentialsPlugin, String sender,
        String warper, String warpOwner, Warp warp)
    {
        Preconditions.checkNotNull(deltaEssentialsPlugin, "DeltaEssentials was null.");
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(warper, "Warper was null.");
        Preconditions.checkNotNull(warpOwner, "Warp owner was null.");
        Preconditions.checkNotNull(warp, "Warp was null.");

        this.deltaEssentialsPlugin = deltaEssentialsPlugin;
        this.sender = sender;
        this.warper = warper;
        this.warpOwner = warpOwner;
        this.warp = warp;
    }

    @Override
    public void run()
    {
        boolean isForceWarpUse = !sender.equals(warper);

        Player player = Bukkit.getPlayerExact(warper);

        if(player == null)
        {
            if(isForceWarpUse)
            {
                sendMessage(FAILURE + input(warper) + " is no longer online.");
            }
            return;
        }

        String currentServerName = DeltaRedisApi.instance().getServerName();
        boolean isOwner = player.getName().equalsIgnoreCase(warpOwner);
        boolean canUseOthers =
            player.hasPermission("DeltaWarps.Staff.Use") ||
            player.hasPermission("DeltaWarps.Use.Special." + warp.getName().toLowerCase());
        boolean canUseNormal =
            player.hasPermission("DeltaWarps.Use.Normal") ||
            canUseOthers ||
            isForceWarpUse;
        boolean canUseFaction =
            player.hasPermission("DeltaWarps.Use.Faction") ||
            canUseOthers ||
            isForceWarpUse;

        if(warp.getServer().equals(currentServerName))
        {
            onSameServerWarp(
                player,
                isOwner,
                canUseNormal,
                canUseFaction,
                canUseOthers);
        }
        else
        {
            onDifferentServerWarp(
                player,
                isOwner,
                canUseNormal,
                canUseOthers);
        }
    }

    private void onSameServerWarp(Player player, boolean isOwner, boolean canUseNormal,
        boolean canUseFaction, boolean canUseOthers)
    {
        Location warpLocation = getWarpLocation(warp);

        if(warp.getType() == WarpType.PUBLIC)
        {
            if(!canUseNormal)
            {
                player.sendMessage(FAILURE + "You do not have permission to use public warps.");
                return;
            }

            warpPlayerWithEvent(player, warpLocation);
        }
        else if(warp.getType() == WarpType.PRIVATE)
        {
            if(!canUseNormal)
            {
                player.sendMessage(FAILURE + "You do not have permission to use private warps.");
                return;
            }

            if(!isOwner && !canUseOthers)
            {
                player.sendMessage(FAILURE + "You do not have access to that private warp.");
                return;
            }

            warpPlayerWithEvent(player, warpLocation);
        }
        else if(warp.getType() == WarpType.FACTION)
        {
            if(!Settings.isFactionsEnabled())
            {
                player.sendMessage(FAILURE + "Factions is not enabled on this server.");
                return;
            }

            if(!canUseFaction)
            {
                player.sendMessage(FAILURE + "You do not have permission to use faction warps.");
                return;
            }

            Faction faction = MPlayer.get(player).getFaction();
            PS locationPS = PS.valueOf(warpLocation);
            Faction factionAtPos = BoardColl.get().getFactionAt(locationPS);

            if(!factionAtPos.getId().equals(warp.getFaction()))
            {
                player.sendMessage(FAILURE + "Whoops! That faction warp shouldn't exist! Deleting ...");

                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp remove " + warp.getName());
                return;
            }

            if(canUseOthers || (!faction.isNone() && faction.getId().equals(warp.getFaction())))
            {
                warpPlayerWithEvent(player, warpLocation);
            }
            else
            {
                player.sendMessage(FAILURE + "You are not in the same faction as the warp.");
            }
        }
    }

    private void onDifferentServerWarp(Player player, boolean isOwner, boolean canUseNormal,
        boolean canUseOthers)
    {
        DeltaRedisApi api = DeltaRedisApi.instance();

        if(!api.getCachedServers().contains(warp.getServer()))
        {
            player.sendMessage(FAILURE + "The server with that warp (" +
                input(warp.getServer()) + ") is offline.");
            return;
        }

        if(warp.getType() == WarpType.FACTION)
        {
            player.sendMessage(FAILURE + "That warp is a faction warp in " +
                input(warp.getServer()) + " and can only be used in that server.");
            return;
        }

        if(warp.getType() == WarpType.PUBLIC)
        {
            if(!canUseNormal)
            {
                player.sendMessage(FAILURE + "You do not have permission to use public warps.");
                return;
            }
        }
        else if(warp.getType() == WarpType.PRIVATE)
        {
            if(!canUseNormal)
            {
                player.sendMessage(FAILURE + "You do not have permission to use private warps.");
                return;
            }

            if(!isOwner && !canUseOthers)
            {
                player.sendMessage(FAILURE + "You do not have access to that private warp.");
                return;
            }
        }

        player.sendMessage(SUCCESS + "Warping to " + input(warp.getName()) + " ...");

        api.publish(
            warp.getServer(),
            WARP_CHANNEL,
            player.getName(),
            warp.toString());

        deltaEssentialsPlugin.sendToServer(player, warp.getServer());
    }

    private Location getWarpLocation(Warp warp)
    {
        World world = Bukkit.getWorld(warp.getWorld());

        return new Location(world,
            warp.getX() + 0.5, warp.getY() + 0.5, warp.getZ() + 0.5,
            warp.getYaw(), warp.getPitch());
    }

    private void warpPlayerWithEvent(Player player, Location location)
    {
        player.sendMessage(SUCCESS + "Warping to " + input(warp.getName()) + " ...");

        PlayerWarpEvent event = new PlayerWarpEvent(player, location);
        Bukkit.getPluginManager().callEvent(event);

        if(!event.isCancelled())
        {
            player.teleport(location);
        }
    }

    private void sendMessage(String message)
    {
        if(sender.equals("CONSOLE"))
        {
            Bukkit.getConsoleSender().sendMessage(message);
        }
        else
        {
            Player senderPlayer = Bukkit.getPlayerExact(sender);
            if(senderPlayer != null)
            {
                senderPlayer.sendMessage(message);
            }
        }
    }
}
