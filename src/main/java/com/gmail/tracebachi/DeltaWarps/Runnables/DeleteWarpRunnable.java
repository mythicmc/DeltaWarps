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
public class DeleteWarpRunnable implements Runnable
{
    private static final String SELECT_WARP_OWNER =
        " SELECT deltawarps_player.name" +
        " FROM deltawarps_player" +
        " INNER JOIN deltawarps_warp" +
        " ON deltawarps_player.id = deltawarps_warp.ownerId" +
        " WHERE deltawarps_warp.name = ?;";
    private static final String DELETE_WARP =
        " DELETE FROM deltawarps_warp" +
        " WHERE name = ?" +
        " LIMIT 1;";

    private final String sender;
    private final String warpName;
    private final boolean ignoreOwner;
    private final DeltaWarps plugin;

    public DeleteWarpRunnable(String sender, String warpName, boolean ignoreOwner, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(warpName, "Warp name was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.sender = sender.toLowerCase();
        this.warpName = warpName.toLowerCase();
        this.ignoreOwner = ignoreOwner;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            String warpOwner = selectWarpOwner(connection);

            if(warpOwner != null)
            {
                if(ignoreOwner || warpOwner.equals(sender))
                {
                    deleteWarp(connection);

                    sendMessage(
                        plugin,
                        sender,
                        success("Deleted warp " + input(warpName)));
                }
                else
                {
                    sendMessage(
                        plugin,
                        sender,
                        failure("You do not have permission to delete " +
                            input(warpName)));
                }
            }
            else
            {
                sendMessage(
                    plugin,
                    sender,
                    failure(input(warpName) + " does not exist."));
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();

            sendMessage(
                plugin,
                sender,
                failure("Something went wrong. Please inform the developer."));
        }
    }

    private String selectWarpOwner(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP_OWNER))
        {
            statement.setString(1, warpName);

            try(ResultSet resultSet = statement.executeQuery())
            {
                if(resultSet.next())
                {
                    return resultSet.getString("deltawarps_player.name");
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
}
