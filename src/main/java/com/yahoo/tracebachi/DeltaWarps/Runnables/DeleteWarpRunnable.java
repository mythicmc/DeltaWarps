package com.yahoo.tracebachi.DeltaWarps.Runnables;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class DeleteWarpRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT deltawarps_players.name" +
        " FROM deltawarps_players" +
        " INNER JOIN deltawarps_warps" +
        " ON deltawarps_players.id = deltawarps_warps.owner_id" +
        " WHERE deltawarps_warps.name = ?;";
    private static final String DELETE_WARP =
        " DELETE FROM deltawarps_warps" +
        " WHERE name=?" +
        " LIMIT 1;";

    private final String sender;
    private final String warpName;
    private final boolean ignoreOwner;
    private final DeltaWarpsPlugin plugin;

    public DeleteWarpRunnable(String sender, String warpName, boolean ignoreOwner, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.warpName = warpName.toLowerCase();
        this.ignoreOwner = ignoreOwner;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            String warpOwner = selectWarp(connection);
            if(warpOwner != null)
            {
                if(ignoreOwner || warpOwner.equals(sender))
                {
                    deleteWarp(connection);
                    sendMessage(sender, Prefixes.SUCCESS + "Deleted warp " +
                        Prefixes.input(warpName));
                }
                else
                {
                    sendMessage(sender, Prefixes.FAILURE + "You do not have permission to delete " +
                        Prefixes.input(warpName));
                }
            }
            else
            {
                sendMessage(sender, Prefixes.FAILURE + Prefixes.input(warpName) + " does not exist.");
            }
        }
        catch(SQLException ex)
        {
            sendMessage(sender, Prefixes.FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }

    private String selectWarp(Connection connection) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
        {
            statement.setString(1, warpName);
            try(ResultSet resultSet = statement.executeQuery())
            {
                if(resultSet.next())
                {
                    return resultSet.getString("deltawarps_players.name");
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
