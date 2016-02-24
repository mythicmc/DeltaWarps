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
package com.gmail.tracebachi.DeltaWarps.Runnables;

import com.gmail.tracebachi.DeltaRedis.Shared.Prefixes;
import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class ListWarpsRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT name" +
        " FROM deltawarps_warp" +
        " WHERE type = 'PUBLIC'" +
        " LIMIT 75 OFFSET ?;";

    private final String sender;
    private final int pageOffset;
    private final DeltaWarps plugin;

    public ListWarpsRunnable(String sender, int pageOffset, DeltaWarps plugin)
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
                    List<String> messages = new ArrayList<>(75);
                    String header = Prefixes.INFO + "Warp list (Page " + Prefixes.input(pageOffset) + "): ";

                    while(resultSet.next())
                    {
                        String name = resultSet.getString("name");
                        messages.add(name);
                    }

                    sendMessages(sender, Arrays.asList(header, String.join(", ", messages)));
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
