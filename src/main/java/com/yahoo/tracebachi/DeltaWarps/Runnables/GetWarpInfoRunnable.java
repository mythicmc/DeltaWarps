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
package com.yahoo.tracebachi.DeltaWarps.Runnables;

import com.yahoo.tracebachi.DeltaRedis.Spigot.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class GetWarpInfoRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT x, y, z, type, server, deltawarps_players.name" +
        " FROM deltawarps_warps" +
        " INNER JOIN deltawarps_players" +
        " ON deltawarps_warps.owner_id = deltawarps_players.id" +
        " WHERE deltawarps_warps.name = ?;";

    private final String sender;
    private final String warpName;
    private final boolean canSeeCoords;
    private final DeltaWarpsPlugin plugin;

    public GetWarpInfoRunnable(String sender, String warpName, boolean canSeeCoords, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.warpName = warpName.toLowerCase();
        this.canSeeCoords = canSeeCoords;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
            {
                statement.setString(1, warpName);
                try(ResultSet resultSet = statement.executeQuery())
                {
                    if(resultSet.next())
                    {
                        int x = resultSet.getInt("x");
                        int y = resultSet.getShort("y");
                        int z = resultSet.getShort("z");
                        String type = resultSet.getString("type");
                        String server = resultSet.getString("server");
                        String owner = resultSet.getString("deltawarps_players.name");

                        if(canSeeCoords)
                        {
                            sendMessages(sender, new String[]
                            {
                                Prefixes.INFO + "Warp information for " + Prefixes.input(warpName),
                                Prefixes.INFO + "X: " + Prefixes.input(x),
                                Prefixes.INFO + "Y: " + Prefixes.input(y),
                                Prefixes.INFO + "Z: " + Prefixes.input(z),
                                Prefixes.INFO + "Type: " + Prefixes.input(type),
                                Prefixes.INFO + "Owner: " + Prefixes.input(owner),
                                Prefixes.INFO + "Server: " + Prefixes.input(server)
                            });
                        }
                        else
                        {
                            sendMessages(sender, new String[]
                            {
                                Prefixes.INFO + "Warp information for " + Prefixes.input(warpName),
                                Prefixes.INFO + "Type: " + Prefixes.input(type),
                                Prefixes.INFO + "Owner: " + Prefixes.input(owner),
                                Prefixes.INFO + "Server: " + Prefixes.input(server)
                            });
                        }
                    }
                    else
                    {
                        sendMessage(sender, Prefixes.FAILURE + Prefixes.input(warpName) + " does not exist.");
                    }
                }
            }
        }
        catch(SQLException ex)
        {
            sendMessage(sender, Prefixes.FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }

    private void sendMessage(String name, String message)
    {
        Bukkit.getScheduler().runTask(plugin, () ->
        {
            Player player = Bukkit.getPlayer(name);
            if(player != null && player.isOnline())
            {
                player.sendMessage(message);
            }
        });
    }

    private void sendMessages(String name, String[] messages)
    {
        Bukkit.getScheduler().runTask(plugin, () ->
        {
            if(name.equalsIgnoreCase("console"))
            {
                for(String message : messages)
                {
                    Bukkit.getConsoleSender().sendMessage(message);
                }
            }
            else
            {
                Player player = Bukkit.getPlayer(name);
                if(player != null && player.isOnline())
                {
                    for(String message : messages)
                    {
                        player.sendMessage(message);
                    }
                }
            }
        });
    }
}
