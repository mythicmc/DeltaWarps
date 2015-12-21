package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Runnables.GetWarpForUseRunnable;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class UseCommand implements IWarpCommand
{
    private DeltaWarpsPlugin plugin;

    public UseCommand(DeltaWarpsPlugin plugin)
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
        if(!(sender instanceof Player))
        {
            sender.sendMessage(Prefixes.FAILURE + "Only players can use warps.");
            return;
        }

        Player player = (Player) sender;
        if(args[0].length() >= 30)
        {
            player.sendMessage(Prefixes.FAILURE + "Warp name size is restricted to less than 30 characters.");
            return;
        }

        boolean canUse = player.hasPermission("DeltaWarps.Player.Use.Normal");
        boolean canUseFaction = player.hasPermission("DeltaWarps.Player.Use.Faction");

        if(!canUse && !canUseFaction)
        {
            player.sendMessage(Prefixes.FAILURE + "You do not have permission to use any warps.");
            return;
        }

        GetWarpForUseRunnable runnable = new GetWarpForUseRunnable(player.getName(), args[0], plugin);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }
}
