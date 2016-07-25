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
import java.util.Arrays;
import java.util.List;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.*;
import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessage;
import static com.gmail.tracebachi.DeltaWarps.RunnableMessageUtil.sendMessages;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class ListWarpsRunnable implements Runnable
{
    private static final String SELECT_WARP =
        " SELECT name" +
        " FROM deltawarps_warp" +
        " WHERE type = 'PUBLIC'" +
        " LIMIT 75 OFFSET ?;";

    private final String sender;
    private final int pageOffset;
    private final DeltaWarps plugin;

    public ListWarpsRunnable(String sender, int pageOffset, DeltaWarps plugin)
    {
        Preconditions.checkNotNull(sender, "Sender was null.");
        Preconditions.checkNotNull(plugin, "Plugin was null.");

        this.sender = sender.toLowerCase();
        this.pageOffset = pageOffset;
        this.plugin = plugin;
    }

    @Override
    public void run()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(SELECT_WARP))
            {
                statement.setInt(1, pageOffset * 75);

                try(ResultSet resultSet = statement.executeQuery())
                {
                    List<String> messages = new ArrayList<>(75);
                    String header = INFO + "Warp list (Page " + input(pageOffset) + "): ";

                    while(resultSet.next())
                    {
                        String name = resultSet.getString("name");
                        messages.add(name);
                    }

                    sendMessages(
                        plugin,
                        sender,
                        Arrays.asList(header, String.join(", ", messages)));
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
