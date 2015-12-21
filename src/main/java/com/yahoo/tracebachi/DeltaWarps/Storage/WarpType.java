package com.yahoo.tracebachi.DeltaWarps.Storage;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public enum WarpType
{
    PUBLIC,
    FACTION,
    PRIVATE;

    public static WarpType fromString(String type)
    {
        switch(type.toLowerCase())
        {
            case "public":
                return PUBLIC;
            case "faction":
                return FACTION;
            case "private":
                return PRIVATE;
            default:
                return null;
        }
    }
}
