package com.yahoo.tracebachi.DeltaWarps.Commands;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/19/15.
 */
public interface IWarpCommand
{
    HashSet<String> reserved = new HashSet<>(Arrays.asList("add", "give", "info", "list", "move", "remove"));

    void onCommand(CommandSender sender, String[] args);

    void shutdown();
}
