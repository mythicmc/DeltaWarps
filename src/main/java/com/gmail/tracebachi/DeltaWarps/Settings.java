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

import com.gmail.tracebachi.DbShare.Spigot.DbShare;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.FAILURE;
import static com.gmail.tracebachi.DeltaRedis.Shared.Prefixes.input;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 3/15/16.
 */
public class Settings
{
    private static boolean debugEnabled;
    private static boolean warpEditingEnabled;
    private static boolean factionsEnabled;
    private static String databaseName;
    private static Map<String, GroupLimits> groupLimitsMap = new HashMap<>();

    public static void read(FileConfiguration config)
    {
        ConfigurationSection section;

        debugEnabled = config.getBoolean("Debug");
        warpEditingEnabled = config.getBoolean("WarpEditing");
        factionsEnabled = Bukkit.getPluginManager().getPlugin("Factions") != null;
        databaseName = config.getString("Database");

        section = config.getConfigurationSection("GroupLimits");

        groupLimitsMap.clear();

        for(String groupName : section.getKeys(false))
        {
            int normalLimit = section.getInt(groupName + ".Normal", 0);
            int factionLimit = section.getInt(groupName + ".Faction", 0);

            groupLimitsMap.put(groupName, new GroupLimits(normalLimit, factionLimit));
        }
    }

    public static boolean isDebugEnabled()
    {
        return debugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled)
    {
        Settings.debugEnabled = debugEnabled;
    }

    public static boolean isWarpEditingEnabled()
    {
        return warpEditingEnabled;
    }

    public static boolean isFactionsEnabled()
    {
        return factionsEnabled;
    }

    public static HikariDataSource getDataSource()
    {
        return DbShare.getDataSource(databaseName);
    }

    public static GroupLimits getGroupLimitsForSender(Player player)
    {
        int normalLimit = 0;
        int factionLimit = 0;

        for(Map.Entry<String, GroupLimits> entry : groupLimitsMap.entrySet())
        {
            GroupLimits limits = entry.getValue();

            if(player.hasPermission("DeltaWarps.Group." + entry.getKey()))
            {
                normalLimit = Math.max(normalLimit, limits.getNormal());
                factionLimit = Math.max(factionLimit, limits.getFaction());
            }
        }

        return new GroupLimits(normalLimit, factionLimit);
    }

    public static String noPermission(String permission)
    {
        return FAILURE + "You do not have the " + input(permission) + " permission.";
    }
}
