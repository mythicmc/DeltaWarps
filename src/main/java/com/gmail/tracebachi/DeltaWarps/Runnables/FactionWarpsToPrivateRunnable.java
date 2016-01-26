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

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class FactionWarpsToPrivateRunnable implements Runnable
{
    private static final String SELECT_PLAYER =
        " SELECT id" +
        " FROM deltawarps_players" +
        " WHERE name = ?;";
    private static final String UPDATE_WARPS =
        " UPDATE deltawarps_warps" +
        " SET type = 'PRIVATE', faction = NULL" +
        " WHERE owner_id = ? AND server = ?;";

    private final String playerName;
    private final String serverName;
    private final DeltaWarps plugin;

    public FactionWarpsToPrivateRunnable(String playerName, String serverName, DeltaWarps plugin)
    {
        this.playerName = playerName.toLowerCase();
        this.serverName = serverName;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            Integer playerId = selectPlayer(connection);

            if(playerId != null)
            {
                int warpsChanged = updateWarps(playerId, connection);
                sendMessage(playerName, Prefixes.INFO + "Updated " +
                    Prefixes.input(warpsChanged) +
                    " warps to private due to you leaving your faction.");
            }
        }
        catch(SQLException ex)
        {
            sendMessage(playerName, Prefixes.FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }

    private Integer selectPlayer(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER))
        {
            statement.setString(1, playerName);
            try(ResultSet resultSet = statement.executeQuery())
            {
                if(resultSet.next())
                {
                    return resultSet.getInt("id");
                }
                return null;
            }
        }
    }

    private int updateWarps(Integer playerId, Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(UPDATE_WARPS))
        {
            statement.setInt(1, playerId);
            statement.setString(2, serverName);
            return statement.executeUpdate();
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
