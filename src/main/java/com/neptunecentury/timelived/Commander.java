package com.neptunecentury.timelived;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Commander {

    /**
     * Registers the commands used by the mod
     *
     * @param commandName The root command name
     */
    public static void registerCommands(String commandName, Config cfg) {

        // Register the command tree
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal(commandName)
                // Register query command
                .then(CommandManager.literal("query")
                        .then(CommandManager.literal("player")
                                // Add argument for a player entity
                                .then(CommandManager.argument("player", EntityArgumentType.players())
                                        .executes(context -> {
                                                    var players = EntityArgumentType.getPlayers(context, "player");

                                                    players.forEach((player) -> {
                                                        // Get the stats for the player
                                                        var playerDeathData = TimeLived.getPlayerDeathData(player);
                                                        if (playerDeathData != null) {
                                                            // Get the time the player is alive for.
                                                            var timeAlive = TimeLived.getTimeAlive(player, playerDeathData);
                                                            // Get the formatted time lived
                                                            var daysLived = TimeLived.getDaysLived(timeAlive);
                                                            var previousDaysLived = TimeLived.getDaysLived(playerDeathData.longestTimeLived);

                                                            context.getSource().sendFeedback(() -> {
                                                                var msg = cfg.queryPlayerMessage;
                                                                msg = TimeLived.replaceVariable(msg, daysLived, previousDaysLived, player);
                                                                return Text.literal(msg).formatted(Formatting.GREEN);
                                                            }, false);
                                                        } else {
                                                            // Could not find the stats for the player
                                                            context.getSource().sendFeedback(() -> {
                                                                var msg = cfg.statsNotFoundMessage;
                                                                msg = TimeLived.replaceVariable(msg, 0, 0, player);
                                                                return Text.literal(msg).formatted(Formatting.RED);
                                                            }, false);
                                                        }
                                                    });
                                                    return 1;
                                                }
                                        )
                                ))
                        // Add command to get world record
                        .then(CommandManager.literal("worldRecord").executes(context -> {
                            // We need to get all the players currently on the server.
                            // Find the player with the longest time-lived record, including current living time.
                            // A player may be still alive and already broke an existing record, so, get the time
                            // a player has lived and their previous record and return whichever is greater

                            // Get the players
                            var players = TimeLived._server.getPlayerManager().getPlayerList();
                            ServerPlayerEntity recordHolder = null;
                            long maxTimeLived = 0;
                            for (var player : players) {
                                var playerMaxTimeLived = TimeLived.getMaxTimeLived(player);
                                if (playerMaxTimeLived > maxTimeLived) {
                                    maxTimeLived = playerMaxTimeLived;
                                    recordHolder = player;
                                }

                            }

                            // If there is a record holder, send player the message with record stats
                            if (recordHolder != null) {
                                // Display the world record to the user
                                ServerPlayerEntity finalRecordHolder = recordHolder;
                                long finalMaxTimeLived = maxTimeLived;
                                context.getSource().sendFeedback(() -> {
                                    var playerDeathData = TimeLived.getPlayerDeathData(finalRecordHolder);
                                    // Get the formatted time lived
                                    var daysLived = TimeLived.getDaysLived(finalMaxTimeLived);
                                    var previousDaysLived = TimeLived.getDaysLived(playerDeathData.longestTimeLived);

                                    var msg = cfg.queryWorldRecordMessage;
                                    msg = TimeLived.replaceVariable(msg, daysLived, previousDaysLived, finalRecordHolder);
                                    return Text.literal(msg).formatted(Formatting.GREEN);
                                }, false);
                            }

                            return 1;
                        }))
                )
        ));
    }


}
