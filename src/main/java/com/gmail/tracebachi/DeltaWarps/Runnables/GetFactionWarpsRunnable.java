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
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
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
