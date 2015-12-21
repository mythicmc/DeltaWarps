package com.yahoo.tracebachi.DeltaWarps.Runnables;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class GetFactionWarpsRunnable implements Runnable
{
    private static final String SELECT_FACTION_WARPS =
        " SELECT deltawarps_warps.name, deltawarps_players.name" +
        " FROM deltawarps_warps" +
        " INNER JOIN deltawarps_players" +
        " ON deltawarps_warps.owner_id = deltawarps_players.id" +
        " WHERE deltawarps_warps.faction = ? AND server = ?;";

    private final String sender;
    private final String factionName;
    private final String factionId;
    private final String serverName;
    private final DeltaWarpsPlugin plugin;

    public GetFactionWarpsRunnable(String sender, String factionName, String factionId,
        String serverName, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.factionName = factionName.toLowerCase();
        this.factionId = factionId;
        this.serverName = serverName.toLowerCase();
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_FACTION_WARPS))
            {
                statement.setString(1, factionId);
                statement.setString(2, serverName);

                try(ResultSet resultSet = statement.executeQuery())
                {
                    List<String> messages = new ArrayList<>(4);
                    messages.add(Prefixes.INFO + "Faction warp information for " +
                        Prefixes.input(factionName) + " on " + Prefixes.input(serverName));

                    while(resultSet.next())
                    {
                        String warpName = resultSet.getString("deltawarps_warps.name");
                        String owner = resultSet.getString("deltawarps_players.name");

                        messages.add(Prefixes.INFO + Prefixes.input(warpName) +
                            " owned by " + Prefixes.input(owner));
                    }

                    sendMessages(sender, messages);
                }
            }
        }
        catch(SQLException ex)
        {
            sendMessage(sender, Prefixes.FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }

    private void sendMessage(String name, String message)
    {
        Bukkit.getScheduler().runTask(plugin, () ->
        {
            Player player = Bukkit.getPlayer(name);
            if(player != null && player.isOnline())
            {
                player.sendMessage(message);
            }
        });
    }

    private void sendMessages(String name, List<String> messages)
    {
        Bukkit.getScheduler().runTask(plugin, () ->
        {
            if(name.equalsIgnoreCase("console"))
            {
                messages.forEach(Bukkit.getConsoleSender()::sendMessage);
            }
            else
            {
                Player player = Bukkit.getPlayer(name);
                if(player != null && player.isOnline())
                {
                    messages.forEach(player::sendMessage);
                }
            }
        });
    }
}
