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

import com.gmail.tracebachi.DeltaEssentials.Spigot.PlayerLoadingSaving.PlayerPostLoadEvent;
import com.gmail.tracebachi.DeltaWarps.DeltaWarpsConstants.Channels;
import com.gmail.tracebachi.DeltaWarps.DeltaWarpsConstants.Formats;
import com.gmail.tracebachi.DeltaWarps.Storage.Warp;
import com.gmail.tracebachi.DeltaWarps.Storage.WarpType;
import com.gmail.tracebachi.SockExchange.Messages.ReceivedMessage;
import com.gmail.tracebachi.SockExchange.Spigot.SockExchangeApi;
import com.gmail.tracebachi.SockExchange.Utilities.CaseInsensitiveMap;
import com.gmail.tracebachi.SockExchange.Utilities.MessageFormatMap;
import com.gmail.tracebachi.SockExchange.Utilities.Registerable;
import com.google.common.base.Preconditions;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author GeeItsZee (tracebachi@gmail.com)
 */
public class WarpTeleporter implements Listener, Registerable
{
  private static final long WARP_REQUEST_DURATION_MILLIS = TimeUnit.SECONDS.toMillis(5);

  private final DeltaWarpsPlugin plugin;
  private final MessageFormatMap formatMap;
  private final SockExchangeApi api;
  private final Map<String, WarpOnLoadRequest> warpOnLoadRequestMap;
  private final Consumer<ReceivedMessage> warpChannelListener;
  private ScheduledFuture<?> cleanupFuture;

  public WarpTeleporter(
    DeltaWarpsPlugin plugin, MessageFormatMap formatMap, SockExchangeApi api)
  {

    Preconditions.checkNotNull(plugin, "plugin");
    Preconditions.checkNotNull(formatMap, "formatMap");
    Preconditions.checkNotNull(api, "api");

    this.plugin = plugin;
    this.formatMap = formatMap;
    this.api = api;
    this.warpOnLoadRequestMap = new CaseInsensitiveMap<>(new HashMap<>());
    this.warpChannelListener = this::onWarpChannelRequest;
  }

  @Override
  public void register()
  {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);

    api.getMessageNotifier().register(Channels.WARP, warpChannelListener);

    cleanupFuture = api.getScheduledExecutorService().scheduleAtFixedRate(
      this::cleanupRequests, 10, 10, TimeUnit.SECONDS);
  }

  @Override
  public void unregister()
  {
    HandlerList.unregisterAll(this);

    api.getMessageNotifier().unregister(Channels.WARP, warpChannelListener);

    if (cleanupFuture != null)
    {
      cleanupFuture.cancel(false);
      cleanupFuture = null;
    }
  }

  @EventHandler
  public void onPlayerLoaded(PlayerPostLoadEvent event)
  {
    Player player = event.getPlayer();
    WarpOnLoadRequest warpRequest = warpOnLoadRequestMap.remove(player.getName());

    if (warpRequest != null)
    {
      Warp warp = warpRequest.getWarp();
      teleportToWarpWithEvent(player, warp);
    }
  }

  public void teleportToWarp(Player playerToWarp, Warp warp)
  {
    Preconditions.checkNotNull(playerToWarp, "playerToWarp");
    Preconditions.checkNotNull(warp, "warp");

    String warpServer = warp.getServer();

    if (api.getServerName().equalsIgnoreCase(warpServer))
    {
      teleportToWarpWithEvent(playerToWarp, warp);
    }
    else
    {
      String nameToWarp = playerToWarp.getName();
      ByteArrayDataOutput out = ByteStreams.newDataOutput(256);

      out.writeUTF(nameToWarp);
      writeWarpToDataOutput(warp, out);

      // Send the message to warp the player and move the player to the other server
      api.sendToServer(Channels.WARP, out.toByteArray(), warpServer);
      api.movePlayers(Collections.singleton(nameToWarp), warpServer);
    }
  }

  private void onWarpChannelRequest(ReceivedMessage receivedMessage)
  {
    ByteArrayDataInput in = receivedMessage.getDataInput();
    String nameToWarp = in.readUTF();
    Warp warp = readWarpFromDataInput(in);

    plugin.executeSync(() ->
    {
      Player playerToWarp = plugin.getServer().getPlayerExact(nameToWarp);

      if (playerToWarp == null)
      {
        warpOnLoadRequestMap.put(nameToWarp, new WarpOnLoadRequest(warp));
      }
      else
      {
        teleportToWarpWithEvent(playerToWarp, warp);
      }
    });
  }

  private void teleportToWarpWithEvent(Player player, Warp warp)
  {
    String warpName = warp.getName();
    WarpType warpType = warp.getType();
    Location location = plugin.getLocationFromWarp(warp);
    PlayerWarpEvent event = new PlayerWarpEvent(player, warpName, warpType, location);

    // Call the event
    plugin.getServer().getPluginManager().callEvent(event);

    // If the event is cancelled, do nothing
    if (event.isCancelled())
    {
      return;
    }

    player.sendMessage(formatMap.format(Formats.WARPING_TO, warpName));
    player.teleport(location, TeleportCause.COMMAND);
  }

  private void cleanupRequests()
  {
    Iterator<WarpOnLoadRequest> iter = warpOnLoadRequestMap.values().iterator();
    long currentTime = System.currentTimeMillis();

    while (iter.hasNext())
    {
      if (iter.next().getExpiresAtMillis() <= currentTime)
      {
        iter.remove();
      }
    }
  }

  private Warp readWarpFromDataInput(ByteArrayDataInput in)
  {
    Warp warp = new Warp();
    warp.setName(in.readUTF());
    warp.setOwnerId(in.readInt());
    warp.setX(in.readInt());
    warp.setY(in.readInt());
    warp.setZ(in.readInt());
    warp.setYaw(in.readInt());
    warp.setPitch(in.readInt());
    warp.setWorld(in.readUTF());
    warp.setType(WarpType.fromString(in.readUTF()));

    // Check if the warp has a faction name
    if (in.readBoolean())
    {
      warp.setFaction(in.readUTF());
    }
    else
    {
      warp.setFaction(null);
    }

    warp.setServer(in.readUTF());

    return warp;
  }

  private void writeWarpToDataOutput(Warp warp, ByteArrayDataOutput out)
  {
    out.writeUTF(warp.getName());
    out.writeInt(warp.getOwnerId());
    out.writeInt(warp.getX());
    out.writeInt(warp.getY());
    out.writeInt(warp.getZ());
    out.writeInt((int) warp.getYaw());
    out.writeInt((int) warp.getPitch());
    out.writeUTF(warp.getWorld());
    out.writeUTF(warp.getType().name());

    if (warp.getFaction() == null)
    {
      out.writeBoolean(false);
    }
    else
    {
      out.writeBoolean(true);
      out.writeUTF(warp.getFaction());
    }

    out.writeUTF(warp.getServer());
  }

  private static class WarpOnLoadRequest
  {
    private final Warp warp;
    private final long expiresAtMillis;

    private WarpOnLoadRequest(Warp warp)
    {
      this.warp = warp;
      this.expiresAtMillis = System.currentTimeMillis() + WARP_REQUEST_DURATION_MILLIS;
    }

    public Warp getWarp()
    {
      return warp;
    }

    public long getExpiresAtMillis()
    {
      return expiresAtMillis;
    }
  }
}
