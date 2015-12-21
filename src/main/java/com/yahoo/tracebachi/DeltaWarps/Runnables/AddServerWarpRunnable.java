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

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.*;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class AddServerWarpRunnable implements Runnable
{
    private static final int WARP_NAME_EXISTS = 1062;

    private static final String INSERT_WARP =
        " INSERT INTO deltawarps_warps" +
        " (name, owner_id, x, y, z, yaw, pitch, type, faction, server)" +
        " VALUES (?, 1, ?, ?, ?, ?, ?, 'PUBLIC', 'safezone', ?);";

    private final String sender;
    private final Warp warp;
    private final DeltaWarpsPlugin plugin;

    public AddServerWarpRunnable(String sender, Warp warp, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.warp = warp;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(INSERT_WARP))
            {
                statement.setString(1, warp.getName().toLowerCase());
                statement.setInt(2, warp.getX());
                statement.setInt(3, warp.getY());
                statement.setInt(4, warp.getZ());
                statement.setFloat(5, warp.getYaw());
                statement.setFloat(6, warp.getPitch());
                statement.setString(7, warp.getServer());
                statement.execute();

                sendMessage(sender, Prefixes.SUCCESS + "Created server warp " +
                    Prefixes.input(warp.getName()));
            }
        }
        catch(SQLException ex)
        {
            if(ex.getErrorCode() == WARP_NAME_EXISTS)
            {
                sendMessage(sender, Prefixes.FAILURE +
                    "Failed to create warp. Name is already in use.");
            }
            else
            {
                ex.printStackTrace();
            }
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
