package com.yahoo.tracebachi.DeltaWarps.Runnables;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class MoveWarpRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT type, deltawarps_players.name" +
        " FROM deltawarps_warps" +
        " INNER JOIN deltawarps_players" +
        " ON deltawarps_warps.owner_id = deltawarps_players.id" +
        " WHERE deltawarps_warps.name = ?;";
    private static final String UPDATE_WARP =
        " UPDATE deltawarps_warps" +
        " SET x=?, y=?, z=?, yaw=?, pitch=?, type=?, faction=?, server=?" +
        " WHERE name = ?" +
        " LIMIT 1;";

    private final String sender;
    private final String playerFactionId;
    private final String factionIdAtPos;
    private final Warp warp;
    private final boolean ignoreOwner;
    private final DeltaWarpsPlugin plugin;


    public MoveWarpRunnable(String sender, String playerFactionId, String factionAtPosId,
        Warp warp, boolean ignoreOwner, DeltaWarpsPlugin plugin)
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
        String owner = resultSet.getString("deltawarps_players.name");
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
                        warp.getYaw(), warp.getPitch(),
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
                    warp.getYaw(), warp.getPitch(),
                    WarpType.PRIVATE, null, warp.getServer());

                updateWarp(newWarp, connection);
                sendMessage(sender, Prefixes.SUCCESS + "Moved faction warp to new location and made private.");
            }
        }
        else
        {
            newWarp = new Warp(warp.getName(),
                warp.getX(), warp.getY(), warp.getZ(),
                warp.getYaw(), warp.getPitch(),
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
            statement.setString(6, newWarp.getType().toString());
            statement.setString(7, newWarp.getFaction());
            statement.setString(8, newWarp.getServer());
            statement.setString(9, newWarp.getName());
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
