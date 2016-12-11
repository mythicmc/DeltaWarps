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
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessage;
import static com.gmail.tracebachi.DeltaWarps.Settings.failure;
import static com.gmail.tracebachi.DeltaWarps.Settings.success;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class MoveWarpRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT type, deltawarps_warp.faction, deltawarps_player.name" +
        " FROM deltawarps_warp" +
        " INNER JOIN deltawarps_player" +
        " ON deltawarps_warp.ownerId = deltawarps_player.id" +
        " WHERE deltawarps_warp.name = ?;";
    private static final String UPDATE_WARP =
        " UPDATE deltawarps_warp" +
        " SET x=?, y=?, z=?, yaw=?, pitch=?, world=?, type=?, faction=?, server=?" +
        " WHERE name = ?" +
        " LIMIT 1;";

    private final String sender;
    private final Warp warp;
    private final boolean ignoreOwner;
    private final DeltaWarps plugin;

    public MoveWarpRunnable(String sender, Warp warp, boolean ignoreOwner, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(warp, "Warp was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.sender = sender.toLowerCase();
        this.warp = warp;
        this.ignoreOwner = ignoreOwner;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
            {
                statement.setString(1, warp.getName());

                try(ResultSet resultSet = statement.executeQuery())
                {
                    if(resultSet.next())
                    {
                        onWarpFound(resultSet, connection);
                    }
                    else
                    {
                        sendMessage(
                            plugin,
                            sender,
                            failure("That warp does not exist."));
                    }
                }
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();

            sendMessage(
                plugin,
                sender,
                failure("Something went wrong. " + "Please report this to the developer."));
        }
    }

    private void onWarpFound(ResultSet resultSet, Connection connection) throws SQLException
    {
        WarpType originalType = WarpType.fromString(resultSet.getString("type"));
        String originalFaction = resultSet.getString("deltawarps_warp.faction");
        String owner = resultSet.getString("deltawarps_player.name");

        if(!ignoreOwner && !owner.equals(sender))
        {
            sendMessage(
                plugin,
                sender,
                failure("You do not have access to move that warp."));
            return;
        }

        if(originalType == WarpType.FACTION)
        {
            if(!Settings.isFactionsEnabled())
            {
                sendMessage(
                    plugin,
                    sender,
                    failure("Factions is not enabled on this server."));
                return;
            }

            if(warp.getFaction() != null && warp.getFaction().equals(originalFaction))
            {
                updateWarp(warp, connection);

                sendMessage(
                    plugin,
                    sender,
                    success("Moved faction warp to new location."));
            }
            else
            {
                sendMessage(
                    plugin,
                    sender,
                    failure("You cannot move a faction warp " +
                        "to land that does not belong to your faction."));
            }
        }
        else
        {
            Warp newWarp = new Warp(
                warp.getName(),
                warp.getX(),
                warp.getY(),
                warp.getZ(),
                warp.getYaw(),
                warp.getPitch(),
                warp.getWorld(),
                originalType,
                null,
                warp.getServer());

            updateWarp(newWarp, connection);

            sendMessage(
                plugin,
                sender,
                success("Moved normal warp to new location."));
        }
    }

    private int updateWarp(Warp newWarp, Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(UPDATE_WARP))
        {
            statement.setInt(1, newWarp.getX());
            statement.setInt(2, newWarp.getY());
            statement.setInt(3, newWarp.getZ());
            statement.setFloat(4, newWarp.getYaw());
            statement.setFloat(5, newWarp.getPitch());
            statement.setString(6, newWarp.getWorld());
            statement.setString(7, newWarp.getType().toString());
            statement.setString(8, newWarp.getFaction());
            statement.setString(9, newWarp.getServer());
            statement.setString(10, newWarp.getName());
            return statement.executeUpdate();
        }
    }
}
