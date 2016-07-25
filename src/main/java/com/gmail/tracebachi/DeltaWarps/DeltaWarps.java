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
package com.gmail.tracebachi.DeltaWarps;

import com.gmail.tracebachi.DeltaEssentials.DeltaEssentials;
import com.gmail.tracebachi.DeltaExecutor.DeltaExecutor;
import com.gmail.tracebachi.DeltaWarps.Commands.SWarpCommand;
import com.gmail.tracebachi.DeltaWarps.Commands.WarpCommand;
import com.gmail.tracebachi.DeltaWarps.Runnables.UseWarpRunnable;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.*;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/18/15.
 */
public class DeltaWarps extends JavaPlugin
{
    private static final String CREATE_PLAYER_TABLE =
        " CREATE TABLE IF NOT EXISTS deltawarps_player (" +
        " `id`       INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
        " `name`     VARCHAR(32) NOT NULL UNIQUE KEY," +
        " `normal`   SMALLINT UNSIGNED NOT NULL DEFAULT 0," +
        " `faction`  SMALLINT UNSIGNED NOT NULL DEFAULT 0" +
        " );";
    private static final String CREATE_WARP_TABLE =
        " CREATE TABLE IF NOT EXISTS deltawarps_warp (" +
        " `name`      VARCHAR(32) NOT NULL PRIMARY KEY," +
        " `ownerId`   INT UNSIGNED NOT NULL," +
        " `x`         INT SIGNED NOT NULL," +
        " `y`         INT SIGNED NOT NULL," +
        " `z`         INT SIGNED NOT NULL," +
        " `yaw`       FLOAT NOT NULL," +
        " `pitch`     FLOAT NOT NULL," +
        " `world`     VARCHAR(32) NOT NULL," +
        " `type`      VARCHAR(7) NOT NULL," +
        " `faction`   VARCHAR(36)," +
        " `server`    VARCHAR(32) NOT NULL," +
        " CONSTRAINT `fkOwnerId` FOREIGN KEY (`ownerId`) REFERENCES `deltawarps_player` (`id`) ON DELETE CASCADE," +
        " KEY `faction_and_server` (`faction`, `server`)" +
        " );";
    private static final String SELECT_SERVER_WARP_OWNER =
        " SELECT 1 FROM deltawarps_player WHERE name = '!DeltaWarps!';";
    private static final String INSERT_SERVER_WARP_OWNER =
        " INSERT INTO deltawarps_player" +
        " (id, name, normal, faction)" +
        " VALUES(1, '!DeltaWarps!', 65535, 65535);";

    private DeltaWarpsListener warpsListener;
    private DeltaEssentials deltaEssentialsPlugin;
    private WarpCommand warpCommand;
    private SWarpCommand sWarpCommand;

    @Override
    public void onLoad()
    {
        saveDefaultConfig();
    }

    @Override
    public void onEnable()
    {
        reloadConfig();
        Settings.read(getConfig());

        deltaEssentialsPlugin = (DeltaEssentials) getServer().getPluginManager().getPlugin("DeltaEssentials");

        if(!createTables())
        {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        warpCommand = new WarpCommand(this);
        warpCommand.register();

        sWarpCommand = new SWarpCommand(this);
        sWarpCommand.register();

        warpsListener = new DeltaWarpsListener(this);
        warpsListener.register();
    }

    @Override
    public void onDisable()
    {
        DeltaExecutor.instance().shutdown();

        warpsListener.shutdown();
        warpsListener = null;

        sWarpCommand.shutdown();
        sWarpCommand = null;

        warpCommand.shutdown();
        warpCommand = null;
    }

    public void info(String input)
    {
        getLogger().info(input);
    }

    public void severe(String input)
    {
        getLogger().severe(input);
    }

    public void debug(String input)
    {
        if(Settings.isDebugEnabled())
        {
            getLogger().info("[Debug] " + input);
        }
    }

    public void useWarpSync(String sender, String warper, String warpOwner, Warp warp)
    {
        Bukkit.getScheduler().runTask(
            this,
            new UseWarpRunnable(
                deltaEssentialsPlugin,
                sender,
                warper,
                warpOwner,
                warp));
    }

    private boolean createTables()
    {
        try(Connection connection = Settings.getDataSource().getConnection())
        {
            try(PreparedStatement statement = connection.prepareStatement(CREATE_PLAYER_TABLE))
            {
                info("Creating player table ...");
                statement.execute();
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
                info("Creating warps table ...");
                statement.execute();
            }
            return true;
        }
        catch(SQLException ex)
        {
            severe("Failed to build required tables.");
            ex.printStackTrace();
            return false;
        }
    }
}
