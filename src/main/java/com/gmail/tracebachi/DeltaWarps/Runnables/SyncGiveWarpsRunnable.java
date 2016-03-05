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

import com.gmail.tracebachi.DeltaWarps.DeltaWarps;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import org.bukkit.command.CommandSender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class SyncGiveWarpsRunnable implements Runnable
{
    private static final String SELECT_PLAYER =
        " SELECT normal, faction" +
        " FROM deltawarps_player" +
        " WHERE name = ?;";
    private static final String UPDATE_PLAYER =
        " INSERT INTO deltawarps_player" +
        " (name, normal, faction)" +
        " VALUES(?, ?, ?)" +
        " ON DUPLICATE KEY UPDATE" +
        " normal = VALUES(normal)," +
        " faction = VALUES(faction);";

    private final CommandSender sender;
    private final String receiver;
    private final WarpType type;
    private final int amount;
    private final DeltaWarps plugin;

    public SyncGiveWarpsRunnable(CommandSender sender, String receiver, WarpType type, int amount, DeltaWarps plugin)
    {
        this.sender = sender;
        this.receiver = receiver.toLowerCase();
        this.type = type;
        this.amount = amount;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = plugin.getDatabaseConnection())
        {
            short currentNormal = 0;
            short currentFaction = 0;

            try(PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER))
            {
                statement.setString(1, receiver);

                try(ResultSet resultSet = statement.executeQuery())
                {
                    if(resultSet.next())
                    {
                        currentNormal = resultSet.getShort("normal");
                        currentFaction = resultSet.getShort("faction");
                    }
                }
            }

            if(type == WarpType.FACTION)
            {
                if(currentFaction + amount < 0)
                {
                    updatePlayer(connection, currentNormal, (short) 0);
                    sender.sendMessage(SUCCESS + "Updated faction warps for " +
                        input(receiver) + " from " +
                        input(currentFaction) + " to " +
                        input(0));
                }
                else
                {
                    updatePlayer(connection, currentNormal, (short) (currentFaction + amount));
                    sender.sendMessage(SUCCESS + "Updated faction warps for " +
                        input(receiver) + " from " +
                        input(currentFaction) + " to " +
                        input(currentFaction + amount));
                }
            }
            else
            {
                if(currentNormal + amount < 0)
                {
                    updatePlayer(connection, (short) 0, currentFaction);
                    sender.sendMessage(SUCCESS + "Updated normal warps for " +
                        input(receiver) + " from " +
                        input(currentNormal) + " to " +
                        input(0));
                }
                else
                {
                    updatePlayer(connection, (short) (currentNormal + amount), currentFaction);
                    sender.sendMessage(SUCCESS + "Updated normal warps for " +
                        input(receiver) + " from " +
                        input(currentNormal) + " to " +
                        input(currentNormal + amount));
                }
            }
        }
        catch(SQLException ex)
        {
            sender.sendMessage(FAILURE + "Something went wrong. Please inform the developer.");
            ex.printStackTrace();
        }
    }

    private void updatePlayer(Connection connection, short newNormal, short newFaction) throws SQLException
    {
        try(PreparedStatement statement = connection.prepareStatement(UPDATE_PLAYER))
        {
            statement.setString(1, receiver);
            statement.setShort(2, newNormal);
            statement.setShort(3, newFaction);
            statement.execute();
        }
    }
}
