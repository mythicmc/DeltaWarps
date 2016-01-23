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

import com.gmail.tracebachi.DeltaRedis.Spigot.Prefixes;
import com.gmail.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class DeleteWarpRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT deltawarps_players.name" +
        " FROM deltawarps_players" +
        " INNER JOIN deltawarps_warps" +
        " ON deltawarps_players.id = deltawarps_warps.owner_id" +
        " WHERE deltawarps_warps.name = ?;";
    private static final String DELETE_WARP =
        " DELETE FROM deltawarps_warps" +
        " WHERE name=?" +
        " LIMIT 1;";

    private final String sender;
    private final String warpName;
    private final boolean ignoreOwner;
    private final DeltaWarpsPlugin plugin;

    public DeleteWarpRunnable(String sender, String warpName, boolean ignoreOwner, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.warpName = warpName.toLowerCase();
        this.ignoreOwner = ignoreOwner;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            String warpOwner = selectWarp(connection);
            if(warpOwner != null)
            {
                if(ignoreOwner || warpOwner.equals(sender))
                {
                    deleteWarp(connection);
                    sendMessage(sender, Prefixes.SUCCESS + "Deleted warp " +
                        Prefixes.input(warpName));
                }
                else
                {
                    sendMessage(sender, Prefixes.FAILURE + "You do not have permission to delete " +
                        Prefixes.input(warpName));
                }
            }
            else
            {
                sendMessage(sender, Prefixes.FAILURE + Prefixes.input(warpName) + " does not exist.");
            }
        }
        catch(SQLException ex)
        {
            sendMessage(sender, Prefixes.FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }

    private String selectWarp(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
        {
            statement.setString(1, warpName);
            try(ResultSet resultSet = statement.executeQuery())
            {
                if(resultSet.next())
                {
                    return resultSet.getString("deltawarps_players.name");
                }
                return null;
            }
        }
    }

    private void deleteWarp(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(DELETE_WARP))
        {
            statement.setString(1, warpName);
            statement.executeUpdate();
        }
    }

    private void sendMessage(String name, String message)
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
}
