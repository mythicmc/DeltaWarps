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
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
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
public class GetPlayerWarpsRunnable implements Runnable
{
    private static final String SELECT_PLAYER_WARPS =
        " SELECT deltawarps_warp.name, server, type, deltawarps_player.normal, deltawarps_player.faction" +
        " FROM deltawarps_warp" +
        " INNER JOIN deltawarps_player" +
        " ON deltawarps_player.id = deltawarps_warp.ownerId" +
        " WHERE deltawarps_player.name = ?;";

    private final String sender;
    private final String player;
    private final boolean canSeePrivateWarps;
    private final DeltaWarps plugin;

    public GetPlayerWarpsRunnable(String sender, String player, boolean canSeePrivateWarps, DeltaWarps plugin)
    {
        this.sender = sender.toLowerCase();
        this.player = player.toLowerCase();
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
                        String name = resultSet.getString("deltawarps_warp.name");
                        String server = resultSet.getString("server");
                        WarpType type = WarpType.fromString(resultSet.getString("type"));

                        normal = resultSet.getShort("deltawarps_player.normal");
                        faction = resultSet.getShort("deltawarps_player.faction");

                        if(type == WarpType.PRIVATE && canSeePrivateWarps)
                        {
                            messages.add(Prefixes.INFO +
                                Prefixes.input(name) + " on " +
                                Prefixes.input(server) + ", " +
                                Prefixes.input(type.name().toLowerCase()));
                        }
                        else
                        {
                            messages.add(Prefixes.INFO +
                                Prefixes.input(name) + " on " +
                                Prefixes.input(server) + ", " +
                                Prefixes.input(type.name().toLowerCase()));
                        }
                    }

                    messages.add(1, Prefixes.INFO + "Normal (+" +
                        Prefixes.input(normal) + "), Faction (+" +
                        Prefixes.input(faction) + ")");

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
