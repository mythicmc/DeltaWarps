/*
 * DeltaWarps - Warping plugin for BungeeCord and Spigot servers
 * Copyright (C) 2017 tracebachi@gmail.com (GeeItsZee)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.gmail.tracebachi.DeltaWarps;

import com.gmail.tracebachi.DbShare.DbShare;
import com.gmail.tracebachi.DeltaWarps.Commands.ServerWarpCommand;
import com.gmail.tracebachi.DeltaWarps.Commands.WarpCommand;
import com.gmail.tracebachi.DeltaWarps.Storage.GroupLimits;
import com.gmail.tracebachi.DeltaWarps.Storage.MySql.MySqlWarpOwnerStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.MySql.MySqlWarpStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwnerStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpStorage;
import com.gmail.tracebachi.SockExchange.Scheduler.SchedulingExecutor;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * * @author GeeItsZee (tracebachi@gmail.com)
 */
public class DeltaWarpsPlugin extends JavaPlugin
{
  private final GroupLimits ZERO_GROUP_LIMITS = new GroupLimits(0, 0);

  private String dbShareDataSourceName;
  private String serverWarpOwnerName;
  private boolean tryToSupportFactions;
  private boolean allowedToAddWarps;
  private boolean allowedToMoveWarps;
  private int warpListPageSize;
  private Map<String, GroupLimits> groupLimitsMap;
  private MessageFormatMap formatMap;

  private WarpStorage warpStorage;
  private WarpOwnerStorage warpOwnerStorage;
  private FactionsHelper factionsHelper;

  private WarpCommand warpCommand;
  private ServerWarpCommand serverWarpCommand;
  private WarpTeleporter warpTeleporter;

  @Override
  public void onEnable()
  {
    saveDefaultConfig();
    reloadConfig();
    readConfiguration(getConfig());

    try
    {
      warpStorage = new MySqlWarpStorage(this);
      warpOwnerStorage = new MySqlWarpOwnerStorage(this, serverWarpOwnerName);

      warpStorage.createTable();
      warpOwnerStorage.createTable();
    }
    catch (Exception e)
    {
      e.printStackTrace();

      getServer().getPluginManager().disablePlugin(this);
      return;
    }

    SockExchangeApi api = SockExchangeApi.instance();
    SchedulingExecutor executor = api.getSchedulingExecutor();
    String serverName = api.getServerName();
    boolean foundFactions = getServer().getPluginManager().getPlugin("Factions") != null;

    factionsHelper = new FactionsHelper(tryToSupportFactions && foundFactions);

    warpTeleporter = new WarpTeleporter(this, formatMap, api);
    warpTeleporter.register();

    warpCommand = new WarpCommand(this, serverName, allowedToAddWarps, allowedToMoveWarps,
      warpListPageSize, factionsHelper, warpStorage, warpOwnerStorage, warpTeleporter, formatMap,
      executor);
    warpCommand.register();

    serverWarpCommand = new ServerWarpCommand(this, serverName, warpStorage, formatMap, executor);
    serverWarpCommand.register();
  }

  @Override
  public void onDisable()
  {
    if (serverWarpCommand != null)
    {
      serverWarpCommand.unregister();
      serverWarpCommand = null;
    }

    if (warpCommand != null)
    {
      warpCommand.unregister();
      warpCommand = null;
    }

    if (warpTeleporter != null)
    {
      warpTeleporter.unregister();
      warpTeleporter = null;
    }

    factionsHelper = null;
    warpOwnerStorage = null;
    warpStorage = null;
  }

  public Connection getConnection() throws SQLException
  {
    return DbShare.instance().getDataSource(dbShareDataSourceName).getConnection();
  }

  public void executeSync(Runnable runnable)
  {
    getServer().getScheduler().runTask(this, runnable);
  }

  public Location getLocationFromWarp(Warp warp)
  {
    World world = getServer().getWorld(warp.getWorld());
    return new Location(world, warp.getX() + 0.5, warp.getY(), warp.getZ() + 0.5, warp.getYaw(),
      warp.getPitch());
  }

  public GroupLimits getGroupLimitsForSender(Player player)
  {
    for (Map.Entry<String, GroupLimits> entry : groupLimitsMap.entrySet())
    {
      GroupLimits limits = entry.getValue();

      if (player.hasPermission("DeltaWarps.Group." + entry.getKey()))
      {
        return limits;
      }
    }

    return ZERO_GROUP_LIMITS;
  }

  public void sendMessageSync(String message, String receiver)
  {
    getServer().getScheduler().runTask(this, () ->
    {
      CommandSender commandSender;
      if (receiver.equalsIgnoreCase("console"))
      {
        commandSender = getServer().getConsoleSender();
      }
      else
      {
        commandSender = getServer().getPlayerExact(receiver);
      }

      if (commandSender != null)
      {
        commandSender.sendMessage(message);
      }
    });
  }

  public void sendMessagesSync(List<String> messageList, String receiver)
  {
    getServer().getScheduler().runTask(this, () ->
    {
      CommandSender commandSender;
      if (receiver.equalsIgnoreCase("console"))
      {
        commandSender = getServer().getConsoleSender();
      }
      else
      {
        commandSender = getServer().getPlayerExact(receiver);
      }

      if (commandSender != null)
      {
        for (String message : messageList)
        {
          commandSender.sendMessage(message);
        }
      }
    });
  }

  private void readConfiguration(ConfigurationSection config)
  {
    dbShareDataSourceName = config.getString("DbShareDataSourceName", null);
    serverWarpOwnerName = config.getString("ServerWarpOwnerName", "Mr.LobaLoba");
    tryToSupportFactions = config.getBoolean("TryToSupportFactions", true);
    allowedToAddWarps = config.getBoolean("AllowedToAddWarps", true);
    allowedToMoveWarps = config.getBoolean("AllowedToMoveWarps", true);
    warpListPageSize = config.getInt("WarpListPageSize", 50);
    groupLimitsMap = new HashMap<>();
    formatMap = new MessageFormatMap();

    ConfigurationSection section;

    section = config.getConfigurationSection("GroupLimits");
    for (String groupName : section.getKeys(false))
    {
      int normalLimit = section.getInt(groupName + ".Normal", 0);
      int factionLimit = section.getInt(groupName + ".Faction", 0);

      groupLimitsMap.put(groupName, new GroupLimits(normalLimit, factionLimit));
    }

    section = config.getConfigurationSection("Formats");
    for (String formatKey : section.getKeys(false))
    {
      String format = section.getString(formatKey);
      String translated = ChatColor.translateAlternateColorCodes('&', format);
      formatMap.put(formatKey, translated);
    }
  }
}
