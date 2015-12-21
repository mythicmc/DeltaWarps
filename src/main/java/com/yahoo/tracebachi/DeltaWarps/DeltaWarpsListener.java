package com.yahoo.tracebachi.DeltaWarps;

import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.factions.event.EventFactionsMembershipChange;
import com.yahoo.tracebachi.DeltaRedis.Spigot.DeltaRedisMessageEvent;
import com.yahoo.tracebachi.DeltaWarps.Runnables.FactionWarpsToPrivateRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/19/15.
 */
public class DeltaWarpsListener implements Listener
{
    private static final String WARP_CHANNEL = "DW-Warp";
    private static final Pattern PATTERN = Pattern.compile("/\\\\");

    private HashMap<String, WarpRequest> warpRequests = new HashMap<>();
    private DeltaWarpsPlugin plugin;

    public DeltaWarpsListener(DeltaWarpsPlugin plugin)
    {
        this.plugin = plugin;
    }

    public void shutdown()
    {
        plugin = null;
    }

    @EventHandler
    public void onRedisMessage(DeltaRedisMessageEvent event)
    {
        if(event.getChannel().equals(WARP_CHANNEL))
        {
            String[] splitMessage = PATTERN.split(event.getMessage(), 2);
            String name = splitMessage[0];
            Warp warp = Warp.fromString(splitMessage[1]);
            Player player = Bukkit.getPlayer(name);

            if(player != null && player.isOnline())
            {
                Location location = new Location(player.getWorld(),
                    warp.getX() + 0.5, warp.getY(), warp.getZ() + 0.5,
                    warp.getYaw(), warp.getPitch());
                warpPlayerWithEvent(player, location);
            }
            else
            {
                warpRequests.put(name, new WarpRequest(warp));
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        WarpRequest request = warpRequests.remove(event.getPlayer().getName());

        if(request != null)
        {
            Location location = new Location(player.getWorld(),
                request.warp.getX() + 0.5, request.warp.getY(), request.warp.getZ() + 0.5,
                request.warp.getYaw(), request.warp.getPitch());
            warpPlayerWithEvent(player, location);
        }
    }

    @EventHandler(priority= EventPriority.NORMAL)
    public void onPlayerLeaveFaction(EventFactionsMembershipChange event)
    {
        if(event.getReason() == EventFactionsMembershipChange.MembershipChangeReason.DISBAND ||
            event.getReason() == EventFactionsMembershipChange.MembershipChangeReason.KICK ||
            event.getReason() == EventFactionsMembershipChange.MembershipChangeReason.LEAVE)
        {
            MPlayer mPlayer = event.getMPlayer();

            FactionWarpsToPrivateRunnable runnable = new FactionWarpsToPrivateRunnable(
                mPlayer.getName(), plugin);
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    public void cleanup()
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
