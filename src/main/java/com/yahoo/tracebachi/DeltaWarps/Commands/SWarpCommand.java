package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.AddServerWarpRunnable;
import com.yahoo.tracebachi.DeltaWarps.Runnables.AddWarpRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class SWarpCommand implements CommandExecutor
{
    private final String serverName;
    private DeltaWarpsPlugin plugin;

    public SWarpCommand(String serverName, DeltaWarpsPlugin plugin)
    {
        this.serverName = serverName;
        this.plugin = plugin;
    }

    public void shutdown()
    {
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        String warpName = args[0].toLowerCase();

        if(!(sender instanceof Player))
        {
            sender.sendMessage(Prefixes.FAILURE + "Only players can add warps.");
            return true;
        }

        Player player = (Player) sender;
        if(!player.hasPermission("DeltaWarps.Staff.ServerWarp"))
        {
            player.sendMessage(Prefixes.FAILURE + "You do not have permission to add server warps.");
            return true;
        }

        if(args.length == 0)
        {
            player.sendMessage(Prefixes.INFO + "/swarp <warp name>");
            return true;
        }

        if(warpName.length() >= 30)
        {
            player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to less than 30 characters.");
            return true;
        }

        if(IWarpCommand.reserved.contains(warpName))
        {
            player.sendMessage(Prefixes.FAILURE + "That is a reserved name.");
            return true;
        }

        Warp warp = new Warp(warpName, player.getLocation(), WarpType.PUBLIC,
            FactionColl.get().getSafezone().getId(), serverName);
        AddServerWarpRunnable runnable = new AddServerWarpRunnable(sender.getName(), warp, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        return true;
    }
}
