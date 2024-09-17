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

    private static final Config _cfg = new ConfigManager<Config>(MOD_ID, logger).load(Config.class);
    private static final String DAYS_LIVED_VARIABLE = "{daysLived}";
    private static final String PREVIOUS_DAYS_LIVED_VARIABLE = "{previousDaysLived}";
    private static final String PLAYER_NAME_VARIABLE = "{playerName}";

    @Override
    public void onInitialize() {
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
            // Get how many days the player lived
            var daysLived = getDaysLived(timeLived);
            var previousDaysLived = getDaysLived(playerDeathData.longestTimeLived);

            // Since the world is still ticking while the player is dead, update the last time of death
            // after the player has respawned.
            playerDeathData.timePlayerLastDied = world.getTimeOfDay();

            // Get message to send to the player
            var msg = getTimeLivedMessage(_cfg.timeLivedMessages, daysLived, previousDaysLived, newPlayer);
            if (msg != null) {
                // If days are negative... then the player must have traveled back in time!
                if (_cfg.timeTravelMessage != null && daysLived < 0) {
                    msg.append(" ");
                    var timeTravelMsg = _cfg.timeTravelMessage;
                    timeTravelMsg = replaceVariable(timeTravelMsg, daysLived, previousDaysLived, newPlayer);
                    msg.append(Text.literal(timeTravelMsg));
                }

                // Output the chat message to the player
                newPlayer.sendMessage(msg.formatted(Formatting.GREEN));
            }

            // Check if the player reached a new record and send a message if they did.
            if (didPlayerBeatPersonalRecord(timeLived, playerDeathData)) {
                sendNewRecordMessage(newPlayer, daysLived, previousDaysLived);
            }

        });
    }

    /**
     * Checks if the player beat their record days lived
     *
     * @param timeLived       The ticks the player has lived for
     * @param playerDeathData The player death data
     * @return True if the player exceeded their last record
     */
    public boolean didPlayerBeatPersonalRecord(long timeLived, PlayerDeathData playerDeathData) {
        var beatRecord = false;
        // Check if we should display new record message
        if (timeLived > playerDeathData.longestTimeLived) {
            // Set new record.
            playerDeathData.longestTimeLived = timeLived;
            beatRecord = true;
        }

        return beatRecord;
    }

    /**
     * Sends a message to the player that they have beat their record. Also sends a message to other
     * players if configured.
     *
     * @param newPlayer         The player who died after respawn
     * @param daysLived         The number of days lived
     * @param previousDaysLived The previous record days lived
     */
    private void sendNewRecordMessage(ServerPlayerEntity newPlayer, double daysLived, double previousDaysLived) {
        // Output the chat message to the player
        if (_cfg.newRecordMessage != null) {
            var msg = replaceVariable(_cfg.newRecordMessage, daysLived, previousDaysLived, newPlayer);
            newPlayer.sendMessage(Text.literal(msg).formatted(Formatting.AQUA));
        }

        // Check if we should send message to others
        if (_cfg.enableMessagesToOthers && _cfg.newRecordMessageToOthers != null) {
            // Get the players on the server
            var server = newPlayer.getServer();
            if (server != null) {
                var playerManager = server.getPlayerManager();
                var players = playerManager.getPlayerList();
                // Loop through each player and send a message, alerting everyone of the dead player's death.
                for (var player : players) {
                    if (player != newPlayer) {
                        // Send player a message
                        var msg = replaceVariable(_cfg.newRecordMessageToOthers, daysLived, previousDaysLived, newPlayer);
                        player.sendMessage(Text.literal(msg).formatted(Formatting.AQUA));
                    }
                }
            }
        }
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
     * Gets a message for the user
     *
     * @param messages          An array list of messages to choose from
     * @param daysLived         The number of days the player lived
     * @param previousDaysLived The previous number of days the player lived
     * @param player            The player entity
     * @return Custom message
     */
    public static MutableText getTimeLivedMessage(@NotNull ArrayList<TimeLivedMessage> messages, double daysLived, double previousDaysLived, ServerPlayerEntity player) {
        String msg = null;
        // Check the days lived and get the appropriate message
        for (TimeLivedMessage tlm : messages) {
            // Check if message starting with the largest first.
            if (daysLived >= tlm.minDaysLived) {
                msg = tlm.message;
                break;
            }
        }

        // Choose first message if we didn't find a valid one.
        if (msg == null && !messages.isEmpty()) {
            // Since messages are sorted from highest to lowest days, pick the last element as default
            msg = messages.get(messages.size() - 1).message;
        }

        if (msg != null) {
            // Replace variables with data
            msg = replaceVariable(msg, daysLived, previousDaysLived, player);

            return Text.literal(msg);
        } else {
            return null;
        }
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

    /**
     * Replaces the variable with formatted data
     *
     * @param msg               The message to search
     * @param daysLived         The number of days the player lived
     * @param previousDaysLived The number of previous days the player lived
     * @param player            The player
     * @return The formatted string
     */
    public static String replaceVariable(@NotNull String msg, double daysLived, double previousDaysLived, ServerPlayerEntity player) {
        var newMsg = msg;
        // Get formatted days lived and previous days lived (record)
        var formattedDays = formatDaysLived(daysLived);
        var formattedPreviousDays = formatDaysLived(previousDaysLived);
        // Replace variables with data
        newMsg = newMsg.replace(DAYS_LIVED_VARIABLE, formattedDays);
        newMsg = newMsg.replace(PREVIOUS_DAYS_LIVED_VARIABLE, formattedPreviousDays);
        newMsg = newMsg.replace(PLAYER_NAME_VARIABLE, player.getEntityName());

        return newMsg;
    }


    public static Config get_cfg() {
        return _cfg;
    }
}
