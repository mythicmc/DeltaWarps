package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.ListWarpsRunnable;
import org.bukkit.command.CommandSender;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/20/15.
 */
public class ListCommand implements IWarpCommand
{
    private DeltaWarpsPlugin plugin;

    public ListCommand(DeltaWarpsPlugin plugin)
    {
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
        if(!sender.hasPermission("DeltaWarps.Player.List"))
        {
            sender.sendMessage(Prefixes.FAILURE + "You do not have permission to list warps.");
            return;
        }

        int page = 0;
        if(args.length >= 2)
        {
            page = parseInt(args[1], 0);
        }

        ListWarpsRunnable runnable = new ListWarpsRunnable(sender.getName(), page, plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    private int parseInt(String source, int def)
    {
        try
        {
            return Integer.parseInt(source);
        }
        catch(NumberFormatException ex)
        {
            return def;
        }
    }
}
