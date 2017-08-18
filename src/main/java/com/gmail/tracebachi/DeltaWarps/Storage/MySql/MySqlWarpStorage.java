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
package com.gmail.tracebachi.DeltaWarps.Storage.MySql;

import com.gmail.tracebachi.DeltaWarps.DeltaWarpsPlugin;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpStorage;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.google.common.base.Preconditions;

import java.sql.*;
import java.util.*;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class MySqlWarpStorage implements WarpStorage
{
  private static final int SQL_CODE_ER_DUP_ENTRY = 1062;
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
  private static final String GET_WARP_BY_NAME =
    " SELECT name, ownerId, x, y, z, yaw, pitch, world, type, faction, server" +
    " FROM deltawarps_warp" +
    " WHERE name = ?;";
  private static final String INSERT_WARP =
    " INSERT INTO deltawarps_warp" +
    " (name, ownerId, x, y, z, yaw, pitch, world, type, faction, server)" +
    " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
  private static final String DELETE_WARP_BY_NAME =
    " DELETE FROM deltawarps_warp" +
    " WHERE name = ?" +
    " LIMIT 1;";
  private static final String UPDATE_WARP =
    " UPDATE deltawarps_warp" +
    " SET x = ?, y = ?, z = ?, yaw = ?, pitch = ?, world = ?, type = ?, faction = ?, server = ?" +
    " WHERE name = ?" +
    " LIMIT 1;";
  private static final String GET_WARPS_BY_OWNER_ID =
    " SELECT name, ownerId, x, y, z, yaw, pitch, world, type, faction, server" +
    " FROM deltawarps_warp" +
    " WHERE ownerId = ?;";
  private static final String GET_OWNER_WARP_COUNT_BY_TYPE =
    " SELECT type, COUNT(name)" +
    " FROM deltawarps_warp" +
    " WHERE ownerId = ?" +
    " GROUP BY type;";
  private static final String GET_PUBLIC_WARPS =
    " SELECT name, ownerId, x, y, z, yaw, pitch, world, type, faction, server" +
    " FROM deltawarps_warp" +
    " WHERE type = 'PUBLIC'" +
    " LIMIT ?" +
    " OFFSET ?;";
  private static final String GET_FACTION_WARPS =
    " SELECT name, ownerId, x, y, z, yaw, pitch, world, type, faction, server" +
    " FROM deltawarps_warp" +
    " WHERE faction = ? and server = ?;";

  private final DeltaWarpsPlugin plugin;

  public MySqlWarpStorage(DeltaWarpsPlugin plugin)
  {
    Preconditions.checkNotNull(plugin, "plugin");

    this.plugin = plugin;
  }

  @Override
  public boolean createTable() throws Exception
  {
    try (Connection conn = plugin.getConnection())
    {
      try (Statement st = conn.createStatement())
      {
        st.execute(CREATE_WARP_TABLE);
        return true;
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public Warp getByName(String warpName)
  {
    ExtraPreconditions.checkNotEmpty(warpName, "warpName");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(GET_WARP_BY_NAME))
      {
        // 1 = name
        st.setString(1, warpName);

        try (ResultSet resultSet = st.executeQuery())
        {
          if (resultSet.next())
          {
            return getWarpFromResultSet(resultSet);
          }
        }
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }

    return null;
  }

  @Override
  public Result add(Warp warp)
  {
    Preconditions.checkNotNull(warp, "warp");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(INSERT_WARP))
      {
        // 1 = name
        // 2 = ownerId
        // 3 = x
        // 4 = y
        // 5 = z
        // 6 = yaw
        // 7 = pitch
        // 8 = world
        // 9 = type
        // 10 = faction
        // 11 = server
        st.setString(1, warp.getName().toLowerCase());
        st.setInt(2, warp.getOwnerId());
        st.setInt(3, warp.getX());
        st.setInt(4, warp.getY());
        st.setInt(5, warp.getZ());
        st.setFloat(6, warp.getYaw());
        st.setFloat(7, warp.getPitch());
        st.setString(8, warp.getWorld());
        st.setString(9, warp.getType().name());
        st.setString(10, warp.getFaction());
        st.setString(11, warp.getServer());

        st.executeUpdate();
        return Result.SUCCESS;
      }
    }
    catch (SQLException e)
    {
      if(e.getErrorCode() == SQL_CODE_ER_DUP_ENTRY)
      {
        return Result.WARP_NAME_EXISTS;
      }
      else
      {
        e.printStackTrace();
        return Result.EXCEPTION;
      }
    }
  }

  @Override
  public Result removeByName(String warpName)
  {
    ExtraPreconditions.checkNotEmpty(warpName, "warpName");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(DELETE_WARP_BY_NAME))
      {
        // 1 = name
        st.setString(1, warpName);

        return st.executeUpdate() == 1 ? Result.SUCCESS : Result.WARP_NOT_FOUND;
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return Result.EXCEPTION;
    }
  }

  @Override
  public Result update(Warp newWarp)
  {
    Preconditions.checkNotNull(newWarp, "newWarp");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(UPDATE_WARP))
      {
        // 1 = x
        // 2 = y
        // 3 = z
        // 4 = yaw
        // 5 = pitch
        // 6 = world
        // 7 = type
        // 8 = faction
        // 9 = server
        // 10 = name
        st.setInt(1, newWarp.getX());
        st.setInt(2, newWarp.getY());
        st.setInt(3, newWarp.getZ());
        st.setFloat(4, newWarp.getYaw());
        st.setFloat(5, newWarp.getPitch());
        st.setString(6, newWarp.getWorld());
        st.setString(7, newWarp.getType().toString());
        st.setString(8, newWarp.getFaction());
        st.setString(9, newWarp.getServer());
        st.setString(10, newWarp.getName());

        return st.executeUpdate() == 1 ? Result.SUCCESS : Result.WARP_NOT_FOUND;
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return Result.EXCEPTION;
    }
  }

  @Override
  public List<Warp> getByOwnerId(int ownerId)
  {
    Preconditions.checkArgument(ownerId > 0, "ownerId");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(GET_WARPS_BY_OWNER_ID))
      {
        // 1 = ownerId
        st.setInt(1, ownerId);

        try (ResultSet resultSet = st.executeQuery())
        {
          List<Warp> warpList = new ArrayList<>();

          while (resultSet.next())
          {
            warpList.add(getWarpFromResultSet(resultSet));
          }

          return warpList;
        }
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  @Override
  public Map<WarpType, Integer> getByOwnerIdGroupByType(int ownerId)
  {
    Preconditions.checkArgument(ownerId > 0, "ownerId");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(GET_OWNER_WARP_COUNT_BY_TYPE))
      {
        // 1 = ownerId
        st.setInt(1, ownerId);

        try (ResultSet resultSet = st.executeQuery())
        {
          Map<WarpType, Integer> counts = new HashMap<>(3);

          while (resultSet.next())
          {
            String type = resultSet.getString("type");
            int count = resultSet.getInt("COUNT(name)");
            WarpType warpType = WarpType.fromString(type);

            if (warpType != null)
            {
              counts.put(warpType, count);
            }
          }

          return counts;
        }
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return Collections.emptyMap();
    }
  }

  @Override
  public List<Warp> getPublicWarps(int limit, int offset)
  {
    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(GET_PUBLIC_WARPS))
      {
        // 1 = limit
        // 2 = offset
        st.setInt(1, Math.max(0, limit));
        st.setInt(2, Math.max(0, offset));

        try (ResultSet resultSet = st.executeQuery())
        {
          List<Warp> warpList = new ArrayList<>(16);

          while (resultSet.next())
          {
            warpList.add(getWarpFromResultSet(resultSet));
          }

          return warpList;
        }
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  @Override
  public List<Warp> getFactionWarps(String factionId, String serverName)
  {
    ExtraPreconditions.checkNotEmpty(factionId, "factionId");
    ExtraPreconditions.checkNotEmpty(serverName, "serverName");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(GET_FACTION_WARPS))
      {
        // 1 = faction
        // 2 = server
        st.setString(1, factionId);
        st.setString(2, serverName);

        try (ResultSet resultSet = st.executeQuery())
        {
          List<Warp> warpList = new ArrayList<>(4);

          while (resultSet.next())
          {
            warpList.add(getWarpFromResultSet(resultSet));
          }

          return warpList;
        }
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  private Warp getWarpFromResultSet(ResultSet resultSet) throws SQLException
  {
    Warp warp = new Warp();
    warp.setName(resultSet.getString("name"));
    warp.setOwnerId(resultSet.getInt("ownerId"));
    warp.setX(resultSet.getInt("x"));
    warp.setY(resultSet.getInt("y"));
    warp.setZ(resultSet.getInt("z"));
    warp.setYaw(resultSet.getFloat("yaw"));
    warp.setPitch(resultSet.getFloat("pitch"));
    warp.setWorld(resultSet.getString("world"));
    warp.setType(WarpType.fromString(resultSet.getString("type")));
    warp.setFaction(resultSet.getString("faction"));
    warp.setServer(resultSet.getString("server"));
    return warp;
  }
}
