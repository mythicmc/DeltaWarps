package com.gmail.tracebachi.DeltaWarps;

import com.gmail.tracebachi.DbShare.DbShare;
import com.gmail.tracebachi.DeltaRedis.Shared.Prefixes;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

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
        return Prefixes.FAILURE + "You do not have the " + Prefixes.input(permission) + " permission.";
    }
}
