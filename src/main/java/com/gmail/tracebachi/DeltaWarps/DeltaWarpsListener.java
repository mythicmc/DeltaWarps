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
package com.gmail.tracebachi.DeltaWarps;

import com.gmail.tracebachi.DeltaEssentials.Events.PlayerPostLoadEvent;
import com.gmail.tracebachi.DeltaExecutor.DeltaExecutor;
import com.gmail.tracebachi.DeltaRedis.Shared.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Shared.SplitPatterns;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisMessageEvent;
import com.gmail.tracebachi.DeltaWarps.Runnables.DeleteFactionWarpsOnLeaveRunnable;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.SUCCESS;
import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.input;
import static com.massivecraft.factions.event.EventFactionsMembershipChange.MembershipChangeReason.DISBAND;
import static com.massivecraft.factions.event.EventFactionsMembershipChange.MembershipChangeReason.KICK;
import static com.massivecraft.factions.event.EventFactionsMembershipChange.MembershipChangeReason.LEAVE;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/19/15.
 */
public class DeltaWarpsListener implements Listener, Registerable, Shutdownable
{
    private static final String WARP_CHANNEL = "DW-Warp";

    private HashMap<String, WarpRequest> warpRequests = new HashMap<>();
    private BukkitTask cleanupTask;
    private DeltaWarps plugin;

    public DeltaWarpsListener(DeltaWarps plugin)
    {
        this.plugin = plugin;
        this.cleanupTask = Bukkit.getScheduler().runTaskTimer(
            plugin,
            this::cleanup,
            40,
            40);
    }

    @Override
    public void register()
    {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void unregister()
    {
        HandlerList.unregisterAll(this);
    }

    @Override
    public void shutdown()
    {
        cleanupTask.cancel();
        cleanupTask = null;
        warpRequests.clear();
        warpRequests = null;
        plugin = null;
    }

    @EventHandler
    public void onRedisMessage(DeltaRedisMessageEvent event)
    {
        if(event.getChannel().equals(WARP_CHANNEL))
        {
            String[] splitMessage = SplitPatterns.DELTA.split(event.getMessage(), 2);
            String name = splitMessage[0];
            Warp warp = Warp.fromString(splitMessage[1]);
            Player player = Bukkit.getPlayerExact(name);

            if(player == null)
            {
                warpRequests.put(name, new WarpRequest(warp));
                return;
            }

            World world = Bukkit.getWorld(warp.getWorld());
            Location location = new Location(
                world,
                warp.getX() + 0.5,
                warp.getY() + 0.5,
                warp.getZ() + 0.5,
                warp.getYaw(),
                warp.getPitch());

            warpPlayerWithEvent(player, location, warp);
        }
    }

    @EventHandler
    public void onPlayerPostLoad(PlayerPostLoadEvent event)
    {
        Player player = event.getPlayer();
        WarpRequest request = warpRequests.remove(event.getPlayer().getName());

        if(request != null)
        {
            Warp warp = request.warp;
            World world = Bukkit.getWorld(warp.getWorld());
            Location location = new Location(
                world,
                warp.getX() + 0.5,
                warp.getY() + 0.5,
                warp.getZ() + 0.5,
                warp.getYaw(),
                warp.getPitch());

            warpPlayerWithEvent(player, location, warp);
        }
    }

    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerLeaveFaction(EventFactionsMembershipChange event)
    {
        if(event.getReason() == DISBAND ||
            event.getReason() == KICK ||
            event.getReason() == LEAVE)
        {
            MPlayer mPlayer = event.getMPlayer();

            DeleteFactionWarpsOnLeaveRunnable runnable = new DeleteFactionWarpsOnLeaveRunnable(
                mPlayer.getName(),
                DeltaRedisApi.instance().getServerName(),
                plugin);
            DeltaExecutor.instance().execute(runnable);
        }
    }

    private void cleanup()
    {
        Iterator<Map.Entry<String, WarpRequest>> iterator = warpRequests.entrySet().iterator();
        long oldestTime = System.currentTimeMillis() - 5000;

        while(iterator.hasNext())
        {
            if(iterator.next().getValue().createdAt < oldestTime)
            {
                iterator.remove();
            }
        }
    }

    private void warpPlayerWithEvent(Player player, Location location, Warp warp)
    {
        player.sendMessage(SUCCESS + "Warping to " + input(warp.getName()) + " ...");

        PlayerWarpEvent event = new PlayerWarpEvent(player, location);
        Bukkit.getPluginManager().callEvent(event);

        if(!event.isCancelled())
        {
            player.teleport(location);
        }
    }

    private class WarpRequest
    {
        private final Warp warp;
        private final long createdAt;

        public WarpRequest(Warp warp)
        {
            this.warp = warp;
            this.createdAt = System.currentTimeMillis();
        }
    }
}
