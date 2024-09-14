package com.neptunecentury.timelived;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;

public class TimeLived implements ModInitializer {
    public static final String MOD_ID = "time-lived";
    public static final HashMap<UUID, PlayerDeathData> playerDeathDataHash = new HashMap<>();

    public static MinecraftServer _server;
    public static final String TIME_LIVED_PLAYER_DEATH_DATA = "TimeLivedPlayerDeathData";
    public static final String TIME_PLAYER_LAST_DIED = "TimePlayerLastDied";
    public static final String TIME_PLAYER_JUST_DIED = "TimePlayerJustDied";
    public static final String LONGEST_TIME_LIVED = "LongestTimeLived";
    // Logger
    public static final Logger logger = LoggerFactory.getLogger(MOD_ID);

    private Config _cfg;

    @Override
    public void onInitialize() {

        // Load the config file
        _cfg = new ConfigManager<Config>(MOD_ID, logger).load(Config.class);
        // Order and reverse the time lived messages
        _cfg.timeLivedMessages.sort(Comparator.comparingDouble((TimeLivedMessage tlm) -> tlm.minDaysLived));
        Collections.reverse(_cfg.timeLivedMessages);

        // Register the commands
        Commander.registerCommands("timelived", _cfg);

        // Register event when player joins server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // A player has joined the server.
            // Set the server instance
            _server = server;

            // Get the player death data from the hash
            var playerDeathData = TimeLived.playerDeathDataHash.getOrDefault(handler.player.getUuid(), null);
            // Check if the player death data needs to be cleared from the hash. This may be needed
            // if the player joins a new world, and no nbt data is loaded into the hash, resulting
            // in left over data from the previous world, which we don't want.
            if (playerDeathData != null && playerDeathData.needsHashDataCleared) {
                playerDeathData = null;
            }

            // If no data, create new instance
            if (playerDeathData == null) {
                playerDeathData = new PlayerDeathData();
                // Get the time of day the player joined if there is no death data because this
                // could be the first time the player joined.
                playerDeathData.timePlayerLastDied = server.getOverworld().getTimeOfDay();
                // Create new instance and save it in the hashmap
                TimeLived.playerDeathDataHash.put(handler.player.getUuid(), playerDeathData);
            }
        });

        ServerPlayConnectionEvents.DISCONNECT.register(((handler, server) -> {
            // When the player leaves, mark the data for clearing. This will get cleared
            // if the player loads another world and the nbt data is read into the hash table,
            // OR if the player joins a new world and the nbt data is not cleared and the
            // needsHashDataCleared flag is set.
            var playerDeathData = playerDeathDataHash.get(handler.player.getUuid());
            if (playerDeathData != null) {
                playerDeathData.needsHashDataCleared = true;
            }
        }));

        // Register when player respawns
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            // If the player is alive, then do nothing. This can happen if the player respawns, but
            // not as a result of death, live traveling to the overworld from the end.
            if (alive) {
                return;
            }

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
            if (msg != null) {
                // If days are negative... then the player must have traveled back in time!
                if (timeLived < 0) {
                    msg.append(" ");
                    msg.append(Text.literal(_cfg.timeTravelMessage));
                }

                // Output the chat message to the player
                newPlayer.sendMessage(msg.formatted(Formatting.GREEN));
            }

            if (timeLived > playerDeathData.longestTimeLived) {
                // Get old record
                var previousDaysLived = getDaysLived(playerDeathData.longestTimeLived);
                var formattedPreviousDaysLived = formatDaysLived(previousDaysLived);

                // Set new record.
                playerDeathData.longestTimeLived = timeLived;

                // Output the chat message to the player
                var newRecordMsg = Text.literal(_cfg.newRecordMessage.formatted(formattedPreviousDaysLived));
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
    private MutableText getTimeLivedMessage(long timeLived) {
        MutableText msg = null;
        var daysLived = getDaysLived(timeLived);
        var formattedDays = formatDaysLived(daysLived);

        // Check the days lived and get the appropriate message
        for (var x = 0; x < _cfg.timeLivedMessages.size(); x++) {
            var tlm = _cfg.timeLivedMessages.get(x);
            // Check if message starting with the largest first.
            if (daysLived >= tlm.minDaysLived) {
                msg = Text.literal(tlm.message.formatted(formattedDays));
                break;
            }
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
