package com.neptunecentury.timelived;

public class PlayerDeathData {
    /**
     * The time the player died (after respawn)
     */
    public long timePlayerLastDied;
    /**
     * The time the player actually died
     */
    public long timePlayerJustDied;
    /**
     * The longest time the player lived
     */
    public long longestTimeLived;

    /**
     * Indicates if the player data in the hash needs to be cleared
     */
    public boolean needsHashDataCleared;
}
