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
package com.gmail.tracebachi.DeltaWarps.Commands;

import com.gmail.tracebachi.DeltaRedis.Shared.Interfaces.Shutdownable;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Trace Bachi (tracebachi@gmail.com, BigBossZee) on 12/19/15.
 */
public interface IWarpCommand extends Shutdownable
{
    HashSet<String> reserved = new HashSet<>(Arrays.asList(
        "add",
        "set",
        "remove",
        "delete",
        "move",
        "give",
        "info",
        "list"));

    void onCommand(CommandSender sender, String[] args);
}
