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

import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessage;
import static com.gmail.tracebachi.DeltaWarps.Settings.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class DeleteFactionWarpsOnLeaveRunnable implements Runnable
{
    private static final String SELECT_PLAYER =
        " SELECT id" +
        " FROM deltawarps_player" +
        " WHERE name = ?;";
    private static final String DELETE_WARPS =
        " DELETE FROM deltawarps_warp" +
        " WHERE ownerId = ? AND server = ? AND type = 'FACTION';";

    private final String playerName;
    private final String serverName;
    private final DeltaWarps plugin;

    public DeleteFactionWarpsOnLeaveRunnable(String playerName, String serverName, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(playerName, "Player name was null.");
        Preconditions.checkNotNull(serverName, "Server name was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.playerName = playerName.toLowerCase();
        this.serverName = serverName;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            Integer playerId = selectPlayer(connection);

            if(playerId != null)
            {
                int warpsChanged = updateWarps(playerId, connection);

                sendMessage(
                    plugin,
                    playerName,
                    info("Deleted " + input(warpsChanged) +
                        " warps due to you leaving your faction."));
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();

            sendMessage(
                plugin,
                playerName,
                failure("Something went wrong. Please inform the developer."));
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
        try(PreparedStatement statement = connection.prepareStatement(DELETE_WARPS))
        {
            statement.setInt(1, playerId);
            statement.setString(2, serverName);
            return statement.executeUpdate();
        }
    }
}
