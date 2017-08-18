# DeltaWarps
Cross-server warping plugin for BungeeCord and Spigot servers that relies on SQL storage, [DbShare](https://github.com/geeitszee/DbShare)
for connection pooling, and [SockExchange](https://github.com/geeitszee/SockExchange)

## Installation
Copy the JAR into the plugins directory of your Spigot installations. The default configurations 
should be fine except for the `DbShareDataSourceName`. Change that setting to match the name of 
the DbShare data source to load from and save to.

## Commands
`/warp add`
- Permission: `DeltaWarps.Add` for the command. `DeltaWarps.Add.IgnoreNotAllowed` to add even
when the config has blocked adding warps.
- Description: Allows the player to create warps

`/warp give`
- Permission: `DeltaWarps.Give`
- Description: Adds (or removes) a number of available warps in addition to the group limits

`/warp info`
- Permission: `DeltaWarps.Info` for the command. `DeltaWarps.Info.Warp.SeeCoords` to see warp
coordinates when doing `/warp info w`. `DeltaWarps.Info.Player.SeePrivateWarps` to see a player's
private warps.
- Description: View warp information for a single warp, a player, or a faction

`/warp list`
- Permission: `DeltaWarps.List`
- Description: List public warps (one page at a time)

`/warp move`
- Permission: `DeltaWarps.Move` for the command. `DeltaWarps.Move.IgnoreNotAllowed` to move even
when the config has blocked moving warps. `DeltaWarps.Move.IgnoreOwner` to move warps of other players.
- Description: Move warps to a different location 

`/warp remove`
- Permission: `DeltaWarps.Remove` for the command. `DeltaWarps.Remove.IgnoreOwner` to remove the warps
of other players.
- Description: Remove warps

`/warp <warp name>`
- Permission: `DeltaWarps.Use.Public`, `DeltaWarps.Use.Private`, `DeltaWarps.Use.Faction` to use warps
of those specific types. `DeltaWarps.Use.Special.<warp name>` to allow access to a single warp even
without other permissions. `DeltaWarps.ForceUse` to force another player in the server to use a warp
even without other permissions.
- Description: Warp to a ... warp

`/serverwarp`
- Permission: `DeltaWarps.ServerWarp`
- Description: Create a warp that belongs to the server/network

## Licence ([GPLv3](http://www.gnu.org/licenses/gpl-3.0.en.html))
```
DeltaWarps - Cross-server warping plugin for Spigot servers.
Copyright (C) 2015  Trace Bachi (tracebachi@gmail.com)

DeltaWarps is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

DeltaWarps is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with DeltaWarps.  If not, see <http://www.gnu.org/licenses/>.
```
