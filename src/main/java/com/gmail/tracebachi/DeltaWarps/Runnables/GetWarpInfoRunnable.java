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
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessage;
import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessages;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class GetWarpInfoRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT x, y, z, type, server, deltawarps_player.name" +
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
        Preconditions.checkNotNull(sender, "Sender cannot be null.");
        Preconditions.checkNotNull(warpName, "Warp name cannot be null.");
        Preconditions.checkNotNull(plugin, "Plugin cannot be null.");

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
                        String type = resultSet.getString("type");
                        String server = resultSet.getString("server");
                        String owner = resultSet.getString("deltawarps_player.name");

                        if(canSeeCoords)
                        {
                            sendMessages(plugin, sender, Arrays.asList(
                                Prefixes.INFO + "Warp information for " + Prefixes.input(warpName),
                                Prefixes.INFO + "X: " + Prefixes.input(x),
                                Prefixes.INFO + "Y: " + Prefixes.input(y),
                                Prefixes.INFO + "Z: " + Prefixes.input(z),
                                Prefixes.INFO + "Type: " + Prefixes.input(type),
                                Prefixes.INFO + "Owner: " + Prefixes.input(owner),
                                Prefixes.INFO + "Server: " + Prefixes.input(server)
                            ));
                        }
                        else
                        {
                            sendMessages(plugin, sender, Arrays.asList(
                                Prefixes.INFO + "Warp information for " + Prefixes.input(warpName),
                                Prefixes.INFO + "Type: " + Prefixes.input(type),
                                Prefixes.INFO + "Owner: " + Prefixes.input(owner),
                                Prefixes.INFO + "Server: " + Prefixes.input(server)
                            ));
                        }
                    }
                    else
                    {
                        sendMessage(plugin, sender, Prefixes.FAILURE + Prefixes.input(warpName) + " does not exist.");
                    }
                }
            }
        }
        catch(SQLException ex)
        {
            sendMessage(plugin, sender, Prefixes.FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }
}
