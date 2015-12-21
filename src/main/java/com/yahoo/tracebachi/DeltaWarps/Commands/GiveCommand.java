package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.GiveWarpsRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class GiveCommand implements IWarpCommand
{
    private DeltaWarpsPlugin plugin;

    public GiveCommand(DeltaWarpsPlugin plugin)
    {
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
        String receiver = args[1];
        String warpTypeString = args[2];
        String amountString = args[3];

        if(!sender.hasPermission("DeltaWarps.Staff.Give"))
        {
            sender.sendMessage(Prefixes.FAILURE + "You do not have permission to give warps.");
            return;
        }

        WarpType type = WarpType.fromString(warpTypeString);
        if(type == null)
        {
            sender.sendMessage(Prefixes.FAILURE + "Unknown warp type: " + ChatColor.WHITE + warpTypeString);
            return;
        }

        Integer amount = parseInt(amountString);
        if(amount == null || amount == 0)
        {
            sender.sendMessage(Prefixes.FAILURE + "Invalid number: " + ChatColor.WHITE + amountString);
            return;
        }

        GiveWarpsRunnable runnable = new GiveWarpsRunnable(sender.getName(), receiver, type, amount, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private Integer parseInt(String source)
    {
        try
        {
            return Integer.parseInt(source);
        }
        catch(NumberFormatException ex)
        {
            return null;
        }
    }
}
