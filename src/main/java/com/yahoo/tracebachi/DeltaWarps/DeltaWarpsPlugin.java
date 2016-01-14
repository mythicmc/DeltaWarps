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
package com.yahoo.tracebachi.DeltaWarps;

import com.yahoo.tracebachi.DeltaEssentials.DeltaEssentialsPlugin;
import com.yahoo.tracebachi.DeltaRedis.Spigot.DeltaRedisApi;
import com.yahoo.tracebachi.DeltaRedis.Spigot.DeltaRedisPlugin;
import com.yahoo.tracebachi.DeltaWarps.Commands.SWarpCommand;
import com.yahoo.tracebachi.DeltaWarps.Commands.WarpCommand;
import com.yahoo.tracebachi.DeltaWarps.Runnables.UseWarpRunnable;
import com.yahoo.tracebachi.DeltaWarps.Storage.Warp;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class DeltaWarpsPlugin extends JavaPlugin
{
    private static HikariDataSource dataSource;

    private static final String CREATE_PLAYER_TABLE =
        " CREATE TABLE IF NOT EXISTS deltawarps_players (" +
        " `id`       INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
        " `name`     VARCHAR(32) NOT NULL UNIQUE KEY," +
        " `normal`   SMALLINT UNSIGNED NOT NULL DEFAULT 0," +
        " `faction`  SMALLINT UNSIGNED NOT NULL DEFAULT 0" +
        " );";
    private static final String CREATE_WARP_TABLE =
        " CREATE TABLE IF NOT EXISTS deltawarps_warps (" +
        " `name`      VARCHAR(32) NOT NULL PRIMARY KEY," +
        " `owner_id`  INT UNSIGNED NOT NULL," +
        " `x`         INT SIGNED NOT NULL," +
        " `y`         INT SIGNED NOT NULL," +
        " `z`         INT SIGNED NOT NULL," +
        " `yaw`       FLOAT NOT NULL," +
        " `pitch`     FLOAT NOT NULL," +
        " `type`      VARCHAR(7) NOT NULL," +
        " `faction`   VARCHAR(36)," +
        " `server`    VARCHAR(32) NOT NULL," +
        " KEY `faction_and_server` (`faction`, `server`)" +
        " );";
    private static final String SELECT_SERVER_WARP_OWNER =
        " SELECT 1 FROM deltawarps_players WHERE name = '!DeltaWarps!';";
    private static final String INSERT_SERVER_WARP_OWNER =
        " INSERT INTO deltawarps_players" +
        " (id, name, normal, faction)" +
        " VALUES(1, '!DeltaWarps!', 65535, 65535);";

    private DeltaWarpsListener warpsListener;
    private DeltaRedisApi deltaRedisApi;
    private DeltaEssentialsPlugin deltaEssentialsPlugin;
    private WarpCommand warpCommand;
    private SWarpCommand sWarpCommand;

    @Override
    public void onLoad()
    {
        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists())
        {
            saveDefaultConfig();
        }
    }

    @Override
    public void onEnable()
    {
        DeltaRedisPlugin deltaRedisPlugin = (DeltaRedisPlugin) getServer()
            .getPluginManager().getPlugin("DeltaRedis");

        deltaRedisApi = deltaRedisPlugin.getDeltaRedisApi();
        deltaEssentialsPlugin = (DeltaEssentialsPlugin) getServer()
            .getPluginManager().getPlugin("DeltaEssentials");

        if(checkDatabase() && createTables())
        {
            warpCommand = new WarpCommand(deltaRedisApi.getServerName(), this);
            getCommand("warp").setExecutor(warpCommand);
            sWarpCommand = new SWarpCommand(deltaRedisApi.getServerName(), this);
            getCommand("swarp").setExecutor(sWarpCommand);

            warpsListener = new DeltaWarpsListener(deltaRedisApi.getServerName(), this);
            getServer().getPluginManager().registerEvents(warpsListener, this);

            getServer().getScheduler().runTaskTimer(this, () -> warpsListener.cleanup(), 40, 40);
        }
        else
        {
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable()
    {
        getServer().getScheduler().cancelTasks(this);

        if(warpsListener != null)
        {
            warpsListener.shutdown();
            warpsListener = null;
        }

        if(sWarpCommand != null)
        {
            getCommand("swarp").setExecutor(null);
            sWarpCommand.shutdown();
            sWarpCommand = null;
        }

        if(warpCommand != null)
        {
            getCommand("warp").setExecutor(null);
            warpCommand.shutdown();
            warpCommand = null;
        }

        dataSource = null;
    }

    public Connection getDatabaseConnection() throws SQLException
    {
        return dataSource.getConnection();
    }

    public void useWarpSync(String sender, String warper, String warpOwner, Warp warp)
    {
        Bukkit.getScheduler().runTask(this, new UseWarpRunnable(
            deltaRedisApi, deltaEssentialsPlugin, sender, warper, warpOwner, warp));
    }

    private boolean checkDatabase()
    {
        String databaseName = getConfig().getString("Database");

        dataSource = deltaEssentialsPlugin.getDataSource(databaseName);
        if(dataSource == null)
        {
            getLogger().severe("The specified database does not exist. Shutting down ...");
            return false;
        }
        return true;
    }

    private boolean createTables()
    {
        try(Connection connection = dataSource.getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(CREATE_PLAYER_TABLE))
            {
                getLogger().info("Creating player table ...");
                statement.execute();
                getLogger().info("......................... Done");
            }
            try(Statement statement = connection.createStatement())
            {
                try(ResultSet resultSet = statement.executeQuery(SELECT_SERVER_WARP_OWNER))
                {
                    if(!resultSet.next())
                    {
                        statement.execute(INSERT_SERVER_WARP_OWNER);
                    }
                }
            }
            try(PreparedStatement statement = connection.prepareStatement(CREATE_WARP_TABLE))
            {
                getLogger().info("Creating warps table ...");
                statement.execute();
                getLogger().info("........................ Done");
            }
            return true;
        }
        catch(SQLException ex)
        {
            getLogger().severe("Failed to build required tables.");
            ex.printStackTrace();
            return false;
        }
    }
}
