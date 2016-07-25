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
package com.gmail.tracebachi.DeltaWarps.Commands;

import com.gmail.tracebachi.DeltaRedis.Shared.Registerable;
import com.gmail.tracebachi.DeltaRedis.Shared.Shutdownable;
import com.gmail.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.INFO;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class WarpCommand implements CommandExecutor, Registerable, Shutdownable
{
    private UseCommand useCommand;
    private AddCommand addCommand;
    private RemoveCommand removeCommand;
    private MoveCommand moveCommand;
    private InfoCommand infoCommand;
    private ListCommand listCommand;
    private GiveCommand giveCommand;
    private DeltaWarps plugin;

    public WarpCommand(DeltaWarps plugin)
    {
        String serverName = DeltaRedisApi.instance().getServerName();

        this.plugin = plugin;
        this.useCommand = new UseCommand(plugin);
        this.addCommand = new AddCommand(serverName, plugin);
        this.removeCommand = new RemoveCommand(plugin);
        this.moveCommand = new MoveCommand(serverName, plugin);
        this.infoCommand = new InfoCommand(serverName, plugin);
        this.listCommand = new ListCommand(plugin);
        this.giveCommand = new GiveCommand();
    }

    @Override
    public void register()
    {
        plugin.getCommand("warp").setExecutor(this);
    }

    @Override
    public void unregister()
    {
        plugin.getCommand("warp").setExecutor(null);
    }

    public void shutdown()
    {
        unregister();

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
        plugin = null;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args)
    {
        if(args.length == 0)
        {
            sender.sendMessage(INFO + "/warp [add, remove, move, info, list, give]");
        }
        else if(args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("set"))
        {
            if(args.length < 2)
            {
                sender.sendMessage(INFO + "/warp add <warp name> [public|faction|private]");
            }
            else
            {
                addCommand.onCommand(sender, args);
            }
        }
        else if(args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("delete"))
        {
            if(args.length < 2)
            {
                sender.sendMessage(INFO + "/warp remove <warp name>");
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
                sender.sendMessage(INFO + "/warp move <warp name>");
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
                sender.sendMessage(INFO + "/warp info w <warp name>");
                sender.sendMessage(INFO + "/warp info p");
                sender.sendMessage(INFO + "/warp info f");
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
                sender.sendMessage(INFO + "/warp list [page]");
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
                sender.sendMessage(INFO + "/warp give <player> <public|faction|private> <amount>");
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
