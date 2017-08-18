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
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwner;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpOwnerStorage;
import com.gmail.tracebachi.SockExchange.Utilities.ExtraPreconditions;
import com.google.common.base.Preconditions;

import java.sql.*;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class MySqlWarpOwnerStorage implements WarpOwnerStorage
{
  private static final int SQL_CODE_ER_DUP_ENTRY = 1062;
  private static final String CREATE_OWNER_TABLE =
    " CREATE TABLE IF NOT EXISTS deltawarps_owner (" +
    " `id`       INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
    " `name`     VARCHAR(32) NOT NULL UNIQUE KEY," +
    " `normal`   SMALLINT UNSIGNED NOT NULL DEFAULT 0," +
    " `faction`  SMALLINT UNSIGNED NOT NULL DEFAULT 0" +
    " );";
  private static final String UPDATE_SERVER_WARP_OWNER =
    " UPDATE deltawarps_owner" +
    " SET name = ?, normal = 32767, faction = 32767" +
    " WHERE id = 1;";
  private static final String INSERT_SERVER_WARP_OWNER =
    " INSERT INTO deltawarps_owner" +
    " (id, name, normal, faction)" +
    " VALUES(1, ?, 32767, 32767);";
  private static final String GET_WARP_OWNER_BY_ID =
    " SELECT id, name, normal, faction" +
    " FROM deltawarps_owner" +
    " WHERE id = ?;";
  private static final String GET_WARP_OWNER_BY_NAME =
    " SELECT id, name, normal, faction" +
    " FROM deltawarps_owner" +
    " WHERE name = ?;";
  private static final String INSERT_WARP_OWNER =
    " INSERT INTO deltawarps_owner" +
    " (name, normal, faction)" +
    " VALUES(?, ?, ?);";
  private static final String UPDATE_WARP_OWNER_BY_NAME =
    " UPDATE deltawarps_owner" +
    " SET normal = ?, faction = ?" +
    " WHERE name = ?" +
    " LIMIT 1;";

  private final DeltaWarpsPlugin plugin;
  private final String serverWarpOwnerName;

  public MySqlWarpOwnerStorage(DeltaWarpsPlugin plugin, String serverWarpOwnerName)
  {
    Preconditions.checkNotNull(plugin, "plugin");
    ExtraPreconditions.checkNotEmpty(serverWarpOwnerName, "serverWarpOwnerName");

    this.plugin = plugin;
    this.serverWarpOwnerName = serverWarpOwnerName;
  }

  @Override
  public boolean createTable() throws Exception
  {
    try (Connection conn = plugin.getConnection())
    {
      try (Statement st = conn.createStatement())
      {
        st.execute(CREATE_OWNER_TABLE);
      }

      boolean found;

      try (PreparedStatement st = conn.prepareStatement(UPDATE_SERVER_WARP_OWNER))
      {
        // 1 = name
        st.setString(1, serverWarpOwnerName);

        found = st.executeUpdate() > 0;
      }

      // Insert if not found
      if (!found)
      {
        try (PreparedStatement st = conn.prepareStatement(INSERT_SERVER_WARP_OWNER))
        {
          // 1 = name
          st.setString(1, serverWarpOwnerName);

          st.executeUpdate();
        }
      }

      return true;
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public WarpOwner getById(int id)
  {
    Preconditions.checkArgument(id > 0, "id");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(GET_WARP_OWNER_BY_ID))
      {
        // 1 = id
        st.setInt(1, id);

        try (ResultSet resultSet = st.executeQuery())
        {
          if (resultSet.next())
          {
            WarpOwner warpOwner = new WarpOwner();
            warpOwner.setId(resultSet.getInt("id"));
            warpOwner.setName(resultSet.getString("name"));
            warpOwner.setExtraNormalWarps(resultSet.getShort("normal"));
            warpOwner.setExtraFactionWarps(resultSet.getShort("faction"));

            return warpOwner;
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
  public WarpOwner getByName(String ownerName)
  {
    ExtraPreconditions.checkNotEmpty(ownerName, "ownerName");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(GET_WARP_OWNER_BY_NAME))
      {
        // 1 = name
        st.setString(1, ownerName);

        try (ResultSet resultSet = st.executeQuery())
        {
          if (resultSet.next())
          {
            WarpOwner warpOwner = new WarpOwner();
            warpOwner.setId(resultSet.getInt("id"));
            warpOwner.setName(resultSet.getString("name"));
            warpOwner.setExtraNormalWarps(resultSet.getShort("normal"));
            warpOwner.setExtraFactionWarps(resultSet.getShort("faction"));

            return warpOwner;
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
  public Result add(WarpOwner warpOwner)
  {
    Preconditions.checkNotNull(warpOwner, "warpOwner");
    ExtraPreconditions.checkNotEmpty(warpOwner.getName(), "name");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(INSERT_WARP_OWNER))
      {
        // 1 = name
        // 2 = normal warps
        // 3 = faction warps
        st.setString(1, warpOwner.getName());
        st.setInt(2, warpOwner.getExtraNormalWarps());
        st.setInt(3, warpOwner.getExtraFactionWarps());

        st.executeUpdate();
        return Result.SUCCESS;
      }
    }
    catch (SQLException e)
    {
      if(e.getErrorCode() == SQL_CODE_ER_DUP_ENTRY)
      {
        return Result.WARP_OWNER_NAME_EXISTS;
      }
      else
      {
        e.printStackTrace();
        return Result.EXCEPTION;
      }
    }
  }

  @Override
  public Result updateByName(WarpOwner warpOwner)
  {
    Preconditions.checkNotNull(warpOwner, "warpOwner");
    ExtraPreconditions.checkNotEmpty(warpOwner.getName(), "name");

    try (Connection conn = plugin.getConnection())
    {
      try (PreparedStatement st = conn.prepareStatement(UPDATE_WARP_OWNER_BY_NAME))
      {
        // 1 = normal warps
        // 2 = faction warps
        // 3 = name
        st.setInt(1, warpOwner.getExtraNormalWarps());
        st.setInt(2, warpOwner.getExtraFactionWarps());
        st.setString(3, warpOwner.getName());

        return st.executeUpdate() == 1 ? Result.SUCCESS : Result.WARP_OWNER_NOT_FOUND;
      }
    }
    catch (SQLException e)
    {
      e.printStackTrace();
      return Result.EXCEPTION;
    }
  }
}
