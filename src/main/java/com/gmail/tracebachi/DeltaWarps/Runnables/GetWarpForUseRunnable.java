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
import static com.gmail.tracebachi.DeltaWarps.Settings.input;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class GetWarpForUseRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT x, y, z, yaw, pitch, world, type, deltawarps_warp.faction, server, deltawarps_player.name" +
        " FROM deltawarps_warp" +
        " INNER JOIN deltawarps_player" +
        " ON deltawarps_player.id = deltawarps_warp.ownerId" +
        " WHERE deltawarps_warp.name = ?;";

    private final String sender;
    private final String warper;
    private final String warpName;
    private final DeltaWarps plugin;

    public GetWarpForUseRunnable(String sender, String warper, String warpName, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(warper, "Warper was null.");
        Preconditions.checkNotNull(warpName, "Warp name was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.sender = sender;
        this.warper = warper;
        this.warpName = warpName.toLowerCase();
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
            {
                statement.setString(1, warpName);

                try(ResultSet resultSet = statement.executeQuery())
                {
                    if(resultSet.next())
                    {
                        String warpOwner = resultSet.getString("deltawarps_player.name");
                        Warp warp = getWarpFromResultSet(resultSet);

                        plugin.useWarpSync(sender, warper, warpOwner, warp);
                    }
                    else
                    {
                        sendMessage(
                            plugin,
                            sender,
                            failure("There is no warp named " + input(warpName)));
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
                failure("Something went wrong. Please inform the developer."));
        }
    }

    private Warp getWarpFromResultSet(ResultSet resultSet) throws SQLException
    {
        int x = resultSet.getInt("x");
        int y = resultSet.getInt("y");
        int z = resultSet.getInt("z");
        float yaw = resultSet.getFloat("yaw");
        float pitch = resultSet.getFloat("pitch");
        String world = resultSet.getString("world");
        WarpType type = WarpType.valueOf(resultSet.getString("type"));
        String faction = resultSet.getString("deltawarps_warp.faction");
        String server = resultSet.getString("server");

        return new Warp(warpName, x, y, z, yaw, pitch, world, type, faction, server);
    }
}
