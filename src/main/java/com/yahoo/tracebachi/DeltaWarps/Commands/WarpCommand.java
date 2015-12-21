package com.yahoo.tracebachi.DeltaWarps.Commands;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Storage.GroupLimits;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class WarpCommand implements CommandExecutor
{
    private HashMap<String, GroupLimits> groupLimits = new HashMap<>();
    private UseCommand useCommand;
    private AddCommand addCommand;
    private RemoveCommand removeCommand;
    private MoveCommand moveCommand;
    private InfoCommand infoCommand;
    private ListCommand listCommand;
    private GiveCommand giveCommand;

    public WarpCommand(String serverName, DeltaWarpsPlugin plugin)
    {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("GroupLimits");
        for(String groupName : section.getKeys(false))
        {
            int normalLimit = section.getInt(groupName + ".Normal", 0);
            int factionLimit = section.getInt(groupName + ".Faction", 0);
            groupLimits.put(groupName, new GroupLimits(normalLimit, factionLimit));
        }

        this.useCommand = new UseCommand(plugin);
        this.addCommand = new AddCommand(serverName, groupLimits, plugin);
        this.removeCommand = new RemoveCommand(plugin);
        this.moveCommand = new MoveCommand(serverName, plugin);
        this.infoCommand = new InfoCommand(serverName, groupLimits, plugin);
        this.listCommand = new ListCommand(plugin);
        this.giveCommand = new GiveCommand(plugin);
    }

    public void shutdown()
    {
        useCommand.shutdown();
        useCommand = null;
        addCommand.shutdown();
        addCommand = null;
        removeCommand.shutdown();
        removeCommand = null;
        moveCommand.shutdown();
        moveCommand = null;
        infoCommand.shutdown();
        infoCommand = null;
        listCommand.shutdown();
        listCommand = null;
        giveCommand.shutdown();
        giveCommand = null;

        groupLimits.clear();
        groupLimits = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(args.length == 0)
        {
            sender.sendMessage(Prefixes.INFO + "/warp [add, remove, move, info, list, give]");
        }
        else if(args[0].equalsIgnoreCase("add"))
        {
            if(args.length < 3)
            {
                sender.sendMessage(Prefixes.INFO + "/warp add <public|faction|private> <warp name>");
            }
            else
            {
                addCommand.onCommand(sender, args);
            }
        }
        else if(args[0].equalsIgnoreCase("remove"))
        {
            if(args.length < 2)
            {
                sender.sendMessage(Prefixes.INFO + "/warp remove <warp name>");
            }
            else
            {
                removeCommand.onCommand(sender, args);
            }
        }
        else if(args[0].equalsIgnoreCase("move"))
        {
            if(args.length < 2)
            {
                sender.sendMessage(Prefixes.INFO + "/warp move <warp name>");
            }
            else
            {
                moveCommand.onCommand(sender, args);
            }
        }
        else if(args[0].equalsIgnoreCase("info"))
        {
            if(args.length < 2)
            {
                sender.sendMessage(Prefixes.INFO + "/warp info w <warp name>");
                sender.sendMessage(Prefixes.INFO + "/warp info p");
                sender.sendMessage(Prefixes.INFO + "/warp info f");
            }
            else
            {
                infoCommand.onCommand(sender, args);
            }
        }
        else if(args[0].equalsIgnoreCase("list"))
        {
            if(args.length < 1)
            {
                sender.sendMessage(Prefixes.INFO + "/warp list [page]");
            }
            else
            {
                listCommand.onCommand(sender, args);
            }
        }
        else if(args[0].equalsIgnoreCase("give"))
        {
            if(args.length < 4)
            {
                sender.sendMessage(Prefixes.INFO + "/warp give <player> <public|faction|private> <amount>");
            }
            else
            {
                giveCommand.onCommand(sender, args);
            }
        }
        else
        {
            useCommand.onCommand(sender, args);
        }
        return true;
    }
}
