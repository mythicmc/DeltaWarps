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
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessage;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class AddServerWarpRunnable implements Runnable
{
    private static final int WARP_NAME_EXISTS = 1062;

    private static final String INSERT_WARP =
        " INSERT INTO deltawarps_warp" +
        " (name, ownerId, x, y, z, yaw, pitch, world, type, faction, server)" +
        " VALUES (?, 1, ?, ?, ?, ?, ?, ?, 'PUBLIC', NULL, ?);";

    private final String sender;
    private final Warp warp;
    private final DeltaWarps plugin;

    public AddServerWarpRunnable(String sender, Warp warp, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender cannot be null.");
        Preconditions.checkNotNull(warp, "Warp cannot be null.");
        Preconditions.checkNotNull(plugin, "Plugin cannot be null.");

        this.sender = sender.toLowerCase();
        this.warp = warp;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(INSERT_WARP))
            {
                statement.setString(1, warp.getName().toLowerCase());
                statement.setInt(2, warp.getX());
                statement.setInt(3, warp.getY());
                statement.setInt(4, warp.getZ());
                statement.setFloat(5, warp.getYaw());
                statement.setFloat(6, warp.getPitch());
                statement.setString(7, warp.getWorld());
                statement.setString(8, warp.getServer());
                statement.execute();

                sendMessage(plugin, sender, Prefixes.SUCCESS + "Created server warp " +
                    Prefixes.input(warp.getName()));
            }
        }
        catch(SQLException ex)
        {
            if(ex.getErrorCode() == WARP_NAME_EXISTS)
            {
                sendMessage(plugin, sender, Prefixes.FAILURE +
                    "Failed to create warp. Name is already in use.");
            }
            else
            {
                ex.printStackTrace();
            }
        }
    }
}
