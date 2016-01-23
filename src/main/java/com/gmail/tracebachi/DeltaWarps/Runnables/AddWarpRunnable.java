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

import com.gmail.tracebachi.DeltaRedis.Spigot.Prefixes;
import com.gmail.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class AddWarpRunnable implements Runnable
{
    private static final int WARP_NAME_EXISTS = 1062;

    private static final String INSERT_PLAYER =
        " INSERT INTO deltawarps_players" +
        " (name)" +
        " VALUES(?)" +
        " ON DUPLICATE KEY UPDATE" +
        " id = id;";
    private static final String SELECT_PLAYER =
        " SELECT id, normal, faction" +
        " FROM deltawarps_players" +
        " WHERE name = ?;";
    private static final String INSERT_WARP =
        " INSERT INTO deltawarps_warps" +
        " (name, owner_id, x, y, z, yaw, pitch, type, faction, server)" +
        " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String SELECT_PLAYER_WARPS =
        " SELECT type " +
        " FROM deltawarps_warps" +
        " INNER JOIN deltawarps_players" +
        " ON deltawarps_players.id = deltawarps_warps.owner_id" +
        " WHERE deltawarps_players.id = ?;";

    private final String sender;
    private final Warp warp;
    private final GroupLimits groupLimits;
    private final DeltaWarpsPlugin plugin;

    private Integer ownerId;
    private short extraNormal = 0;
    private short normalCount = 0;
    private short extraFaction = 0;
    private short factionCount = 0;

    public AddWarpRunnable(String sender, Warp warp, GroupLimits groupLimits, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.warp = warp;
        this.groupLimits = groupLimits;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            boolean foundPlayer = selectPlayer(connection);
            boolean insertedPlayer = false;

            // If player is not found, insert the record
            if(!foundPlayer)
            {
                insertedPlayer = insertPlayer(connection);
            }

            // If player was found or a record was inserted
            if(foundPlayer || insertedPlayer)
            {
                selectPlayerWarps(connection);

                try
                {
                    if(warp.getType() == WarpType.FACTION &&
                        factionCount >= (groupLimits.getFaction() + extraFaction))
                    {
                        sendMessage(sender, Prefixes.FAILURE +
                            "You do not have enough free warps to make a faction warp.");
                    }
                    else if(warp.getType() != WarpType.FACTION &&
                        normalCount >= (groupLimits.getNormal() + extraNormal))
                    {
                        sendMessage(sender, Prefixes.FAILURE +
                            "You do not have enough free warps to make a normal warp.");
                    }
                    else
                    {
                        insertWarp(connection);
                        sendMessage(sender, Prefixes.SUCCESS + "Created a new warp named " +
                            ChatColor.WHITE + warp.getName());
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
            else
            {
                sendMessage(sender, Prefixes.FAILURE + "Failed to find or insert player. " +
                    "Please report this to the developer.");
            }
        }
        catch(SQLException ex)
        {
            sendMessage(sender, Prefixes.FAILURE + "Something went wrong. " +
                "Please report this to the developer.");
            ex.printStackTrace();
        }
    }

    private boolean selectPlayer(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER))
        {
            statement.setString(1, sender);
            try(ResultSet resultSet = statement.executeQuery())
            {
                if(resultSet.next())
                {
                    ownerId = resultSet.getInt("id");
                    extraNormal = resultSet.getShort("normal");
                    extraFaction = resultSet.getShort("faction");
                    return true;
                }
                return false;
            }
        }
    }

    private boolean insertPlayer(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(INSERT_PLAYER, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, sender);
            statement.executeUpdate();

            try(ResultSet resultSet = statement.getGeneratedKeys())
            {
                if(resultSet.next())
                {
                    ownerId = resultSet.getInt(1);
                    extraNormal = 0;
                    extraFaction = 0;
                    return true;
                }
                return false;
            }
        }
    }

    private void selectPlayerWarps(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_WARPS))
        {
            statement.setInt(1, ownerId);

            try(ResultSet resultSet = statement.executeQuery())
            {
                while(resultSet.next())
                {
                    if(resultSet.getString("type").equals(WarpType.FACTION.toString()))
                    {
                        factionCount++;
                    }
                    else
                    {
                        normalCount++;
                    }
                }
            }
        }
    }

    private void insertWarp(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(INSERT_WARP))
        {
            statement.setString(1, warp.getName().toLowerCase());
            statement.setInt(2, ownerId);
            statement.setInt(3, warp.getX());
            statement.setInt(4, warp.getY());
            statement.setInt(5, warp.getZ());
            statement.setFloat(6, warp.getYaw());
            statement.setFloat(7, warp.getPitch());
            statement.setString(8, warp.getType().toString());
            statement.setString(9, warp.getFaction());
            statement.setString(10, warp.getServer());
            statement.execute();
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
