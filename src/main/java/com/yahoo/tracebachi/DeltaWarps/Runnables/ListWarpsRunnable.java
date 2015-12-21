package com.yahoo.tracebachi.DeltaWarps.Runnables;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class ListWarpsRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT name" +
        " FROM deltawarps_warps" +
        " WHERE type = 'PUBLIC'" +
        " LIMIT 50 OFFSET ?;";

    private final String sender;
    private final int pageOffset;
    private final DeltaWarpsPlugin plugin;

    public ListWarpsRunnable(String sender, int pageOffset, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.pageOffset = pageOffset;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
            {
                statement.setInt(1, pageOffset * 50);
                try(ResultSet resultSet = statement.executeQuery())
                {
                    int count = 0;
                    String previous = null;
                    List<String> messages = new ArrayList<>(50);

                    messages.add(Prefixes.INFO + "Warp list (Page " + Prefixes.input(pageOffset) + "): ");

                    while(resultSet.next())
                    {
                        if(count % 2 == 0)
                        {
                            previous = resultSet.getString("name");
                        }
                        else
                        {
                            String name = resultSet.getString("name");
                            messages.add(String.format("%-32s %-32s", previous, name));
                            previous = null;
                        }
                        count++;
                    }

                    if(previous != null)
                    {
                        messages.add(previous);
                    }

                    sendMessages(sender, messages);
                }
            }
        }
        catch(SQLException ex)
        {
            sendMessages(sender, Collections.singletonList(
                Prefixes.FAILURE + "Something went wrong. Please inform the developer."));
            ex.printStackTrace();
        }
    }

    private void sendMessages(String name, List<String> messages)
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
