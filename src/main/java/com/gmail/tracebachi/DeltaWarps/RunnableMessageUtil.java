package com.gmail.tracebachi.DeltaWarps;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 3/15/16.
 */
public interface RunnableMessageUtil
{
    static void sendMessage(JavaPlugin plugin, String name, String message)
    {
        Bukkit.getScheduler().runTask(plugin, () ->
        {
            if(name.equalsIgnoreCase("console"))
            {
                Bukkit.getConsoleSender().sendMessage(message);
            }
            else
            {
                Player player = Bukkit.getPlayer(name);

                if(player != null && player.isOnline())
                {
                    player.sendMessage(message);
                }
            }
        });
    }

    static void sendMessages(JavaPlugin plugin, String name, List<String> messages)
    {
        Bukkit.getScheduler().runTask(plugin, () ->
        {
            if(name.equalsIgnoreCase("console"))
            {
                messages.forEach(Bukkit.getConsoleSender()::sendMessage);
            }
            else
            {
                Player player = Bukkit.getPlayer(name);
                if(player != null && player.isOnline())
                {
                    messages.forEach(player::sendMessage);
                }
            }
        });
    }
}
