package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.DeleteWarpRunnable;
import org.bukkit.command.CommandSender;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class RemoveCommand implements IWarpCommand
{
    private DeltaWarpsPlugin plugin;

    public RemoveCommand(DeltaWarpsPlugin plugin)
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
        String warpName = args[1].toLowerCase();

        if(!sender.hasPermission("DeltaWarps.Player.Remove"))
        {
            sender.sendMessage(Prefixes.FAILURE + "You do not have permission to remove warps.");
            return;
        }

        if(warpName.length() >= 30)
        {
            sender.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to less than 30 characters.");
            return;
        }

        DeleteWarpRunnable runnable = new DeleteWarpRunnable(sender.getName(), warpName,
            sender.hasPermission("DeltaWarps.Staff.Remove"), plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
