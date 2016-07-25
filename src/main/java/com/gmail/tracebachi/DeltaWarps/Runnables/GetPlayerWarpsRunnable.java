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
import com.gmail.tracebachi.DeltaWarps.Settings;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.google.common.base.Preconditions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.*;
import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessage;
import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessages;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class GetPlayerWarpsRunnable implements Runnable
{
    private static final String SELECT_PLAYER =
        " SELECT normal, faction" +
        " FROM deltawarps_player" +
        " WHERE name = ?;";
    private static final String SELECT_PLAYER_WARPS =
        " SELECT deltawarps_warp.name, server, type" +
        " FROM deltawarps_warp" +
        " INNER JOIN deltawarps_player" +
        " ON deltawarps_player.id = deltawarps_warp.ownerId" +
        " WHERE deltawarps_player.name = ?;";

    private final String sender;
    private final String player;
    private final GroupLimits groupLimits;
    private final boolean canSeePrivateWarps;
    private final DeltaWarps plugin;

    public GetPlayerWarpsRunnable(String sender, String player, GroupLimits groupLimits,
        boolean canSeePrivateWarps, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(player, "Player was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.sender = sender.toLowerCase();
        this.player = player.toLowerCase();
        this.groupLimits = groupLimits;
        this.canSeePrivateWarps = canSeePrivateWarps || sender.equalsIgnoreCase(player);
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            Integer counter = 0;
            List<String> messages = new ArrayList<>(4);

            messages.add(INFO + "Player warp information for " + input(player));

            try(PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER))
            {
                statement.setString(1, player);

                try(ResultSet resultSet = statement.executeQuery())
                {
                    String normalLimit = (groupLimits == null) ? "<group>" : String.valueOf(groupLimits.getNormal());
                    String factionLimit = (groupLimits == null) ? "<group>" : String.valueOf(groupLimits.getFaction());
                    String normalExtra = "0";
                    String factionExtra = "0";

                    if(resultSet.next())
                    {
                        normalExtra = String.valueOf(resultSet.getShort("normal"));
                        factionExtra = String.valueOf(resultSet.getShort("faction"));
                    }

                    messages.add(INFO + "Available Warps:");
                    messages.add(INFO +
                        "Normal: " + input(normalLimit + " + " + normalExtra) + ", " +
                        "Faction: " + input(factionLimit + " + " + factionExtra));
                    messages.add(INFO + "Owned Warps:");
                }
            }

            try(PreparedStatement statement = connection.prepareStatement(SELECT_PLAYER_WARPS))
            {
                statement.setString(1, player);

                try(ResultSet resultSet = statement.executeQuery())
                {
                    while(resultSet.next())
                    {
                        String name = resultSet.getString("deltawarps_warp.name");
                        String server = resultSet.getString("server");
                        WarpType type = WarpType.fromString(resultSet.getString("type"));

                        counter += 1;

                        if(type == WarpType.PRIVATE && canSeePrivateWarps)
                        {
                            messages.add(INFO +
                                input(counter) + ". " +
                                input(name) + " on " +
                                input(server) + ", " +
                                input(type.name().toLowerCase()));
                        }
                        else
                        {
                            messages.add(INFO +
                                input(counter) + ". " +
                                input(name) + " on " +
                                input(server) + ", " +
                                input(type.name().toLowerCase()));
                        }
                    }

                    sendMessages(plugin, sender, messages);
                }
            }
        }
        catch(SQLException ex)
        {
            ex.printStackTrace();

            sendMessage(
                plugin,
                sender,
                FAILURE + "Something went wrong. Please inform the developer.");
        }
    }
}
