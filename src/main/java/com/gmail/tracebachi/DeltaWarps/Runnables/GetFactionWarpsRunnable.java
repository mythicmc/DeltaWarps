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
public class GetFactionWarpsRunnable implements Runnable
{
    private static final String SELECT_FACTION_WARPS =
        " SELECT deltawarps_warp.name, deltawarps_player.name" +
        " FROM deltawarps_warp" +
        " INNER JOIN deltawarps_player" +
        " ON deltawarps_warp.ownerId = deltawarps_player.id" +
        " WHERE deltawarps_warp.faction = ? AND server = ?;";

    private final String sender;
    private final String factionName;
    private final String factionId;
    private final String serverName;
    private final DeltaWarps plugin;

    public GetFactionWarpsRunnable(String sender, String factionName, String factionId,
        String serverName, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(factionName, "Faction name was null.");
        Preconditions.checkNotNull(factionId, "Faction ID was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.sender = sender.toLowerCase();
        this.factionName = factionName.toLowerCase();
        this.factionId = factionId;
        this.serverName = serverName.toLowerCase();
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_FACTION_WARPS))
            {
                statement.setString(1, factionId);
                statement.setString(2, serverName);

                try(ResultSet resultSet = statement.executeQuery())
                {
                    List<String> messages = new ArrayList<>(4);
                    messages.add(INFO + "Faction warp information for " +
                        input(factionName) + " on " + input(serverName));

                    while(resultSet.next())
                    {
                        String warpName = resultSet.getString("deltawarps_warp.name");
                        String owner = resultSet.getString("deltawarps_player.name");

                        messages.add(INFO + input(warpName) +
                            " owned by " + input(owner));
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
