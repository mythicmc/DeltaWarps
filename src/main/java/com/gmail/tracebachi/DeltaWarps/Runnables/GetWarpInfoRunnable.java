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
import java.util.Arrays;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.*;
import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessage;
import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessages;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class GetWarpInfoRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT x, y, z, world, type, server, deltawarps_player.name" +
        " FROM deltawarps_warp" +
        " INNER JOIN deltawarps_player" +
        " ON deltawarps_warp.ownerId = deltawarps_player.id" +
        " WHERE deltawarps_warp.name = ?;";

    private final String sender;
    private final String warpName;
    private final boolean canSeeCoords;
    private final DeltaWarps plugin;

    public GetWarpInfoRunnable(String sender, String warpName, boolean canSeeCoords, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(warpName, "Warp name was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.sender = sender.toLowerCase();
        this.warpName = warpName.toLowerCase();
        this.canSeeCoords = canSeeCoords;
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
                        int x = resultSet.getInt("x");
                        int y = resultSet.getShort("y");
                        int z = resultSet.getShort("z");
                        String world = resultSet.getString("world");
                        String type = resultSet.getString("type");
                        String server = resultSet.getString("server");
                        String owner = resultSet.getString("deltawarps_player.name");

                        if(canSeeCoords)
                        {
                            sendMessages(
                                plugin,
                                sender,
                                Arrays.asList(
                                    INFO + "Warp information for " + input(warpName),
                                    INFO + "X: " + input(x),
                                    INFO + "Y: " + input(y),
                                    INFO + "Z: " + input(z),
                                    INFO + "World: " + input(world),
                                    INFO + "Type: " + input(type),
                                    INFO + "Owner: " + input(owner),
                                    INFO + "Server: " + input(server)
                                ));
                        }
                        else
                        {
                            sendMessages(
                                plugin,
                                sender,
                                Arrays.asList(
                                    INFO + "Warp information for " + input(warpName),
                                    INFO + "World: " + input(world),
                                    INFO + "Type: " + input(type),
                                    INFO + "Owner: " + input(owner),
                                    INFO + "Server: " + input(server)
                                ));
                        }
                    }
                    else
                    {
                        sendMessage(
                            plugin,
                            sender,
                            FAILURE + input(warpName) + " does not exist.");
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
                FAILURE + "Something went wrong. Please inform the developer.");
        }
    }
}
