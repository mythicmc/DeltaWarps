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

import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.gmail.tracebachi.DeltaEssentials.DeltaEssentials;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.gmail.tracebachi.DeltaRedis.Spigot.Prefixes;
import com.gmail.tracebachi.DeltaWarps.PlayerWarpEvent;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/19/15.
 */
public class UseWarpRunnable implements Runnable
{
    private static final String WARP_CHANNEL = "DW-Warp";

    private final DeltaRedisApi deltaRedisApi;
    private final DeltaEssentials deltaEssentialsPlugin;
    private final String sender;
    private final String warper;
    private final String warpOwner;
    private final Warp warp;

    public UseWarpRunnable(DeltaRedisApi deltaRedisApi, DeltaEssentials deltaEssentialsPlugin,
        String sender, String warper, String warpOwner, Warp warp)
    {
        this.deltaRedisApi = deltaRedisApi;
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

        Player player = Bukkit.getPlayer(warper);
        if(player != null && player.isOnline())
        {
            String currentServerName = deltaRedisApi.getServerName();
            boolean isOwner = player.getName().equalsIgnoreCase(warpOwner);
            boolean canUseOthers = player.hasPermission("DeltaWarps.Staff.Use");
            boolean canUseNormal = player.hasPermission("DeltaWarps.Player.Use.Normal") ||
                canUseOthers || isForceWarpUse;
            boolean canUseFaction = player.hasPermission("DeltaWarps.Player.Use.Faction") ||
                canUseOthers || isForceWarpUse;

            if(warp.getServer().equals(currentServerName))
            {
                onSameServerWarp(player, isOwner, canUseNormal, canUseFaction, canUseOthers);
            }
            else
            {
                onDifferentServerWarp(player, isOwner, canUseNormal, canUseOthers);
            }
        }
        else
        {
            if(isForceWarpUse)
            {
                if(sender.equals("CONSOLE"))
                {
                    Bukkit.getConsoleSender().sendMessage(Prefixes.FAILURE + Prefixes.input(warper) +
                        " is no longer online.");
                }
                else
                {
                    Player senderPlayer = Bukkit.getPlayer(sender);
                    if(senderPlayer != null && senderPlayer.isOnline())
                    {
                        senderPlayer.sendMessage(Prefixes.FAILURE + Prefixes.input(warper) +
                            " is no longer online.");
                    }
                }
            }
        }
    }

    private void onSameServerWarp(Player player, boolean isOwner, boolean canUseNormal,
        boolean canUseFaction, boolean canUseOthers)
    {
        Location warpLocation = getWarpLocation(player, warp);

        if(warp.getType() == WarpType.PUBLIC)
        {
            if(canUseNormal)
            {
                warpPlayerWithEvent(player, warpLocation);
            }
            else
            {
                player.sendMessage(Prefixes.FAILURE + "You do not have permission to use public warps.");
            }
        }
        else if(warp.getType() == WarpType.FACTION)
        {
            if(canUseFaction)
            {
                Faction faction = MPlayer.get(player).getFaction();
                PS locationPS = PS.valueOf(warpLocation);
                Faction factionAtPos = BoardColl.get().getFactionAt(locationPS);

                if(!factionAtPos.getId().equals(warp.getFaction()))
                {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "warp remove " + warp.getName());
                    player.sendMessage(Prefixes.FAILURE + "Whoops! That faction warp shouldn't exist!");
                    return;
                }

                if(!faction.isNone())
                {
                    if(faction.getId().equals(warp.getFaction()))
                    {
                        warpPlayerWithEvent(player, warpLocation);
                        player.sendMessage(Prefixes.SUCCESS + "Warping to " +
                            Prefixes.input(warp.getName()) + " ...");
                    }
                    else
                    {
                        player.sendMessage(Prefixes.FAILURE + "You are not in the same faction as the warp.");
                    }
                }
                else if(canUseOthers)
                {
                    warpPlayerWithEvent(player, warpLocation);
                    player.sendMessage(Prefixes.SUCCESS + "Warping to " +
                        Prefixes.input(warp.getName()) + " ...");
                }
                else
                {
                    player.sendMessage(Prefixes.FAILURE + "Faction warps cannot be used without a faction.");
                }
            }
            else
            {
                player.sendMessage(Prefixes.FAILURE + "You do not have permission to use faction warps.");
            }
        }
        else if(warp.getType() == WarpType.PRIVATE)
        {
            if(canUseNormal)
            {
                if(isOwner || canUseOthers)
                {
                    warpPlayerWithEvent(player, warpLocation);
                    player.sendMessage(Prefixes.SUCCESS + "Warping to " +
                        Prefixes.input(warp.getName()) + " ...");
                }
                else
                {
                    player.sendMessage(Prefixes.FAILURE + "You do not have access to that private warp.");
                }
            }
            else
            {
                player.sendMessage(Prefixes.FAILURE + "You do not have permission to use private warps.");
            }
        }
    }

    private void onDifferentServerWarp(Player player, boolean isOwner, boolean canUseNormal, boolean canUseOthers)
    {
        if(!deltaRedisApi.getCachedServers().contains(warp.getServer()))
        {
            player.sendMessage(Prefixes.FAILURE + "The server with that warp (" +
                Prefixes.input(warp.getServer()) + ") is offline.");
            return;
        }

        if(warp.getType() == WarpType.FACTION)
        {
            player.sendMessage(Prefixes.FAILURE + "That warp is a faction warp in " +
                Prefixes.input(warp.getServer()) + " and can only be used in that server.");
            return;
        }

        if(warp.getType() == WarpType.PUBLIC)
        {
            if(canUseNormal)
            {
                deltaRedisApi.publish(warp.getServer(), WARP_CHANNEL,
                    player.getName() + "/\\" + warp);
                deltaEssentialsPlugin.sendToServer(player, warp.getServer());
                player.sendMessage(Prefixes.SUCCESS + "Warping to " +
                    Prefixes.input(warp.getName()) + " ...");
            }
            else
            {
                player.sendMessage(Prefixes.FAILURE + "You do not have permission to use public warps.");
            }
        }
        else if(warp.getType() == WarpType.PRIVATE)
        {
            if(canUseNormal)
            {
                if(isOwner || canUseOthers)
                {
                    deltaRedisApi.publish(warp.getServer(), WARP_CHANNEL,
                        player.getName() + "/\\" + warp);
                    deltaEssentialsPlugin.sendToServer(player, warp.getServer());
                    player.sendMessage(Prefixes.SUCCESS + "Warping ...");
                }
                else
                {
                    player.sendMessage(Prefixes.FAILURE + "You do not have access to that private warp.");
                }
            }
            else
            {
                player.sendMessage(Prefixes.FAILURE + "You do not have permission to use private warps.");
            }
        }
    }

    private Location getWarpLocation(Player player, Warp warp)
    {
        return new Location(player.getWorld(),
            warp.getX() + 0.5, warp.getY(), warp.getZ() + 0.5,
            warp.getYaw(), warp.getPitch());
    }

    private void warpPlayerWithEvent(Player player, Location location)
    {
        PlayerWarpEvent event = new PlayerWarpEvent(player, location);
        Bukkit.getPluginManager().callEvent(event);

        if(!event.isCancelled())
        {
            player.teleport(location);
        }
    }
}
