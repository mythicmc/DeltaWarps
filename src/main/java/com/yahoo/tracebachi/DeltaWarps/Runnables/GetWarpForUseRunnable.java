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
public class GetWarpForUseRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT x, y, z, yaw, pitch, type, deltawarps_warps.faction, server, deltawarps_players.name" +
        " FROM deltawarps_warps" +
        " INNER JOIN deltawarps_players" +
        " ON deltawarps_players.id = deltawarps_warps.owner_id" +
        " WHERE deltawarps_warps.name = ?;";

    private final String sender;
    private final String warpName;
    private final DeltaWarpsPlugin plugin;

    public GetWarpForUseRunnable(String sender, String warpName, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.warpName = warpName.toLowerCase();
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
            {
                statement.setString(1, warpName);
                try(ResultSet resultSet = statement.executeQuery())
                {
                    if(resultSet.next())
                    {
                        String warpOwner = resultSet.getString("deltawarps_players.name");
                        Warp warp = getWarpFromResultSet(resultSet);

                        plugin.useWarpSync(sender, warpOwner, warp);
                    }
                    else
                    {
                        sendMessage(sender, Prefixes.FAILURE + "There is no warp named " +
                            Prefixes.input(warpName));
                    }
                }
            }
        }
        catch(SQLException ex)
        {
            sendMessage(sender, Prefixes.FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }

    private Warp getWarpFromResultSet(ResultSet resultSet) throws SQLException
    {
        int x = resultSet.getInt("x");
        int y = resultSet.getInt("y");
        int z = resultSet.getInt("z");
        float yaw = resultSet.getFloat("yaw");
        float pitch = resultSet.getFloat("pitch");
        WarpType type = WarpType.valueOf(resultSet.getString("type"));
        String faction = resultSet.getString("deltawarps_warps.faction");
        String server = resultSet.getString("server");
        return new Warp(warpName, x, y, z, yaw, pitch, type, faction, server);
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
