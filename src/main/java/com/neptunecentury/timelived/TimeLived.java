package com.neptunecentury.timelived;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

public class TimeLived implements ModInitializer {
    public static final String MOD_ID = "time-lived";
    public static final HashMap<UUID, PlayerDeathData> playerDeathDataHash = new HashMap<>();
    //private static final String keyId = "time_since_last_death";
    //public static final Identifier TIME_SINCE_LAST_DEATH = new Identifier("time-lived", keyId);
    public static MinecraftServer _server;
    public static final String TIME_LIVED_PLAYER_DEATH_DATA = "TimeLivedPlayerDeathData";
    public static final String TIME_PLAYER_LAST_DIED = "TimePlayerLastDied";
    public static final String TIME_PLAYER_JUST_DIED = "TimePlayerJustDied";
    public static final String LONGEST_TIME_LIVED = "LongestTimeLived";

    @Override
    public void onInitialize() {

        // Register the commands
        Commander.registerCommands("timelived");

        // Register event when player joins server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // A player has joined the server.
            // Set the server instance
            _server = server;
            var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(handler.player.getUuid(), null);
            if (playerDeathData == null) {
                playerDeathData = new PlayerDeathData();
                // Get the time of day the player joined if there is no death data because this
                // could be the first time the player joined.
                playerDeathData.timePlayerLastDied = server.getOverworld().getTimeOfDay();
                // Create new instance and save it in the hashmap
                TimeLived.playerDeathDataHash.put(handler.player.getUuid(), playerDeathData);
            }
        });

        //ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
        // When the player leaves, remove the data from the hashmap.
        //playerDeathDataHash.remove(handler.player.getUuid());
        //}));

        //ServerPlayerEvents.COPY_FROM.register(((oldPlayer, newPlayer, alive) -> {
        //}));

        // Register when player respawns
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // Once the player respawns, calculate how long the player live for by taking the last
            // time of death, how long they were dead for, and the current world time.
            // First, get the player death data from the hashmap if it exists
            var playerDeathData = TimeLived.playerDeathDataHash.get(oldPlayer.getUuid());
            if (playerDeathData == null) {
                return;
            }

            // Get the current world
            var world = oldPlayer.getServerWorld();
            if (world == null) {
                return;
            }

            // Calculate how long the player lived.
            var timeLived = playerDeathData.timePlayerJustDied - playerDeathData.timePlayerLastDied;

            // Since the world is still ticking while the player is dead, update the last time of death
            // after the player has respawned.
            playerDeathData.timePlayerLastDied = world.getTimeOfDay();

            // Set some custom messages.
            MutableText msg = getTimeLivedMessage(timeLived);

            // If days are negative... then the player must have traveled back in time!
            if (timeLived < 0) {
                msg.append(" ");
                msg.append(Text.translatable("time-lived.text.time-travel"));
            }

            // Output the chat message to the player
            newPlayer.sendMessage(msg.formatted(Formatting.GREEN));

            if (timeLived > playerDeathData.longestTimeLived) {
                // Get old record
                var previousDaysLived = getDaysLived(playerDeathData.longestTimeLived);
                var formattedPreviousDaysLived = formatDaysLived(previousDaysLived);

                // Set new record.
                playerDeathData.longestTimeLived = timeLived;

                // Output the chat message to the player
                var newRecordMsg = Text.translatable("time-lived.text.new-record", formattedPreviousDaysLived);
                newPlayer.sendMessage(newRecordMsg.formatted(Formatting.AQUA));
            }

        });
    }

    /**
     * Calculates the number of days the player lived from the ticks.
     *
     * @param timeLived The ticks the player lived
     * @return The number of days the player lived
     */
    public static double getDaysLived(long timeLived) {
        return (double) timeLived / 24000;
    }

    /**
     * Formats the time lived to days.
     *
     * @param daysLived The number of days the player lived
     * @return The formatted number of days the player lived
     */
    public static String formatDaysLived(double daysLived) {
        var df = new DecimalFormat("#.##");
        return df.format(daysLived);
    }

    /**
     * Formats the time lived to days
     *
     * @param timeLived The number of ticks the player lived
     * @return The formatted days the player lived
     */
    public static String getFormattedDDaysLived(long timeLived) {
        var daysLived = getDaysLived(timeLived);
        return formatDaysLived(daysLived);
    }

    /**
     * Gets a message for the user
     *
     * @param timeLived The number of ticks the player lived
     * @return Custom message
     */
    private static @NotNull MutableText getTimeLivedMessage(long timeLived) {
        MutableText msg;
        var daysLived = getDaysLived(timeLived);
        var formattedDays = formatDaysLived(daysLived);
        if (daysLived > 1) {
            msg = Text.translatable("time-lived.text.congrats", formattedDays);
        } else if (daysLived > 0.5) {
            msg = Text.translatable("time-lived.text.try-again", formattedDays);
        } else if (daysLived > 0.1) {
            msg = Text.translatable("time-lived.text.try-harder", formattedDays);
        } else {
            msg = Text.translatable("time-lived.text.maybe-next-time", formattedDays);
        }
        return msg;
    }

    /**
     * Gets the death data for a player
     *
     * @param player The player to get the death data for
     * @return The player death data
     */
    public static PlayerDeathData getDaysLivedForPlayer(ServerPlayerEntity player) {
        return TimeLived.playerDeathDataHash.get(player.getUuid());
    }
}
