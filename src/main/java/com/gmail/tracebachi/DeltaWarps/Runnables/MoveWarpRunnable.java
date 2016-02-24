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
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class MoveWarpRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT type, deltawarps_player.name" +
        " FROM deltawarps_warp" +
        " INNER JOIN deltawarps_player" +
        " ON deltawarps_warp.owner_id = deltawarps_player.id" +
        " WHERE deltawarps_warp.name = ?;";
    private static final String UPDATE_WARP =
        " UPDATE deltawarps_warp" +
        " SET x=?, y=?, z=?, yaw=?, pitch=?, world=?, type=?, faction=?, server=?" +
        " WHERE name = ?" +
        " LIMIT 1;";

    private final String sender;
    private final String playerFactionId;
    private final String factionIdAtPos;
    private final Warp warp;
    private final boolean ignoreOwner;
    private final DeltaWarps plugin;

    public MoveWarpRunnable(String sender, String playerFactionId, String factionAtPosId,
        Warp warp, boolean ignoreOwner, DeltaWarps plugin)
    {
        this.sender = sender.toLowerCase();
        this.playerFactionId = playerFactionId;
        this.factionIdAtPos = factionAtPosId;
        this.warp = warp;
        this.ignoreOwner = ignoreOwner;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
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
                        sendMessage(sender, Prefixes.FAILURE + "That warp does not exist.");
                    }
                }
            }
        }
        catch(SQLException ex)
        {
            sendMessage(sender, Prefixes.FAILURE + "Something went wrong. " +
                "Please report this to the developer.");
            ex.printStackTrace();
        }
    }

    private void onWarpFound(ResultSet resultSet, Connection connection) throws SQLException
    {
        WarpType originalType = WarpType.fromString(resultSet.getString("type"));
        String owner = resultSet.getString("deltawarps_player.name");
        Warp newWarp;

        if(!ignoreOwner && !owner.equals(sender))
        {
            sendMessage(sender, Prefixes.FAILURE + "You do not have access to move that warp.");
            return;
        }

        if(originalType == WarpType.FACTION)
        {
            if(owner.equals(sender))
            {
                if(playerFactionId.equals(factionIdAtPos))
                {
                    newWarp = new Warp(warp.getName(),
                        warp.getX(), warp.getY(), warp.getZ(),
                        warp.getYaw(), warp.getPitch(), warp.getWorld(),
                        WarpType.FACTION, playerFactionId, warp.getServer());

                    updateWarp(newWarp, connection);
                    sendMessage(sender, Prefixes.SUCCESS + "Moved faction warp to new location.");
                }
                else
                {
                    sendMessage(sender, Prefixes.FAILURE + "You cannot move a faction warp " +
                        "to land that does not belong to your faction.");
                }
            }
            else
            {
                newWarp = new Warp(warp.getName(),
                    warp.getX(), warp.getY(), warp.getZ(),
                    warp.getYaw(), warp.getPitch(), warp.getWorld(),
                    WarpType.PRIVATE, null, warp.getServer());

                updateWarp(newWarp, connection);
                sendMessage(sender, Prefixes.SUCCESS + "Moved faction warp to new location and made private.");
            }
        }
        else
        {
            newWarp = new Warp(warp.getName(),
                warp.getX(), warp.getY(), warp.getZ(),
                warp.getYaw(), warp.getPitch(), warp.getWorld(),
                originalType, null, warp.getServer());

            updateWarp(newWarp, connection);
            sendMessage(sender, Prefixes.SUCCESS + "Moved normal warp to new location.");
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
