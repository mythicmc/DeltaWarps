package com.yahoo.tracebachi.DeltaWarps.Runnables;

import com.yahoo.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.yahoo.tracebachi.DeltaWarps.Prefixes;
import com.yahoo.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.yahoo.tracebachi.DeltaWarps.Storage.WarpType;
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
public class GetPlayerWarpsRunnable implements Runnable
{
    private static final String SELECT_PLAYER_WARPS =
        " SELECT deltawarps_warps.name, server, type, deltawarps_players.normal, deltawarps_players.faction" +
        " FROM deltawarps_warps" +
        " INNER JOIN deltawarps_players" +
        " ON deltawarps_players.id = deltawarps_warps.owner_id" +
        " WHERE deltawarps_players.name = ?;";

    private final String sender;
    private final String player;
    private final GroupLimits groupLimits;
    private final boolean canSeePrivateWarps;
    private final DeltaWarpsPlugin plugin;

    public GetPlayerWarpsRunnable(String sender, String player, GroupLimits groupLimits,
        boolean canSeePrivateWarps, DeltaWarpsPlugin plugin)
    {
        this.sender = sender.toLowerCase();
        this.player = player.toLowerCase();
        this.groupLimits = groupLimits;
        this.canSeePrivateWarps = canSeePrivateWarps || sender.equalsIgnoreCase(player);
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_WARPS))
            {
                statement.setString(1, player);
                try(ResultSet resultSet = statement.executeQuery())
                {
                    short normal = 0;
                    short faction = 0;
                    List<String> messages = new ArrayList<>(4);
                    messages.add(Prefixes.INFO + "Player warp information for " + Prefixes.input(player));

                    while(resultSet.next())
                    {
                        String name = resultSet.getString("deltawarps_warps.name");
                        String server = resultSet.getString("server");
                        WarpType type = WarpType.fromString(resultSet.getString("type"));

                        normal = resultSet.getShort("deltawarps_players.normal");
                        faction = resultSet.getShort("deltawarps_players.faction");

                        if(type == WarpType.PRIVATE && canSeePrivateWarps)
                        {
                            messages.add(Prefixes.INFO +
                                Prefixes.input(name) + " on " +
                                Prefixes.input(server) + ", " +
                                Prefixes.input(type));
                        }
                        else
                        {
                            messages.add(Prefixes.INFO +
                                Prefixes.input(name) + " on " +
                                Prefixes.input(server) + ", " +
                                Prefixes.input(type));
                        }
                    }

                    messages.add(1, Prefixes.INFO + "Normal: " +
                        Prefixes.input(groupLimits.getNormal()) + " + " +
                        Prefixes.input(normal));
                    messages.add(2, Prefixes.INFO + "Faction: " +
                        Prefixes.input(groupLimits.getFaction()) + " + " +
                        Prefixes.input(faction));

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
