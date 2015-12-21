package com.yahoo.tracebachi.DeltaWarps.Storage;

/**
 * Created by Trace Bachi (tracebachi@yahoo.com, BigBossZee) on 12/18/15.
 */
public class GroupLimits
{
    private final short normalLimit;
    private final short factionLimit;

    public GroupLimits(int normalLimit, int factionLimit)
    {
        this.normalLimit = (short) normalLimit;
        this.factionLimit = (short) factionLimit;
    }

    public short getNormal()
    {
        return normalLimit;
    }

    public short getFaction()
    {
        return factionLimit;
    }
}
