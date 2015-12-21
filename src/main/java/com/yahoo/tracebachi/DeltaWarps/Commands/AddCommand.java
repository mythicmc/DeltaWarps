package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.AddWarpRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class AddCommand implements IWarpCommand
{
    private final String serverName;
    private HashMap<String, GroupLimits> groupLimits;
    private DeltaWarpsPlugin plugin;

    public AddCommand(String serverName, HashMap<String, GroupLimits> groupLimits, DeltaWarpsPlugin plugin)
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
