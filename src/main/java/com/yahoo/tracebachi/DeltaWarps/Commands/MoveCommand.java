package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.MoveWarpRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class MoveCommand implements IWarpCommand
{
    private final String serverName;
    private DeltaWarpsPlugin plugin;

    public MoveCommand(String serverName, DeltaWarpsPlugin plugin)
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
        if(!sender.hasPermission("DeltaWarps.Player.Move"))
        {
            player.sendMessage(Prefixes.FAILURE + "You do not have permission to move warps.");
            return;
        }

        if(args[0].length() >= 30)
        {
            player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to less than 30 characters.");
            return;
        }

        MPlayer mPlayer = MPlayer.get(player);
        String playerFactionId = mPlayer.getFactionId();
        PS locationPS = PS.valueOf(player.getLocation());
        Faction facAtPos = BoardColl.get().getFactionAt(locationPS);
        String factionAtPosId = facAtPos.getId();

        if(!facAtPos.isNone())
        {
            if(!playerFactionId.equals(facAtPos.getId()))
            {
                player.sendMessage(Prefixes.FAILURE +
                    "Warps can only be created on land owned by your faction or wilderness.");
                return;
            }
        }

        Warp warp = new Warp(warpName, player.getLocation(), null, playerFactionId, serverName);
        MoveWarpRunnable runnable = new MoveWarpRunnable(sender.getName(), playerFactionId,
            factionAtPosId, warp, sender.hasPermission("DeltaWarps.Staff.Move"), plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
