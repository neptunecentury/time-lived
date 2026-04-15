package com.neptunecentury.timelived;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

public class Commander {

    /**
     * Registers the commands used by the mod
     *
     * @param commandName The root command name
     */
    public static void registerCommands(String commandName, Config cfg) {

        // Register the command tree
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(Commands.literal(commandName)
                // Register query command
                .then(Commands.literal("query")
                        .then(Commands.literal("player")
                                // Add argument for a player entity
                                .then(Commands.argument("player", EntityArgument.players())
                                        .executes(context -> {
                                                    var players = EntityArgument.getPlayers(context, "player");

                                                    players.forEach((player) -> {
                                                        // Get the stats for the player
                                                        var playerDeathData = TimeLived.getPlayerDeathData(player);
                                                        if (playerDeathData != null) {
                                                            // Get the time the player is alive for.
                                                            var timeAlive = TimeLived.getTimeAlive(player, playerDeathData);
                                                            // Get the formatted time lived
                                                            var daysLived = TimeLived.getDaysLived(timeAlive);
                                                            var previousDaysLived = TimeLived.getDaysLived(playerDeathData.longestTimeLived);

                                                            context.getSource().sendSuccess(() -> {
                                                                var msg = cfg.queryPlayerMessage;
                                                                msg = TimeLived.replaceVariable(msg, daysLived, previousDaysLived, player);
                                                                return Component.literal(msg).withStyle(ChatFormatting.GREEN);
                                                            }, false);
                                                        } else {
                                                            // Could not find the stats for the player
                                                            context.getSource().sendSuccess(() -> {
                                                                var msg = cfg.statsNotFoundMessage;
                                                                msg = TimeLived.replaceVariable(msg, 0, 0, player);
                                                                return Component.literal(msg).withStyle(ChatFormatting.RED);
                                                            }, false);
                                                        }
                                                    });
                                                    return 1;
                                                }
                                        )
                                ))
                        // Add command to get world record
                        .then(Commands.literal("worldRecord").executes(context -> {
                            // We need to get all the players currently on the server.
                            // Find the player with the longest time-lived record, including current living time.
                            // A player may be still alive and already broke an existing record, so, get the time
                            // a player has lived and their previous record and return whichever is greater

                            // Get the players
                            var players = TimeLived._server.getPlayerList().getPlayers();
                            ServerPlayer recordHolder = null;
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
                                ServerPlayer finalRecordHolder = recordHolder;
                                long finalMaxTimeLived = maxTimeLived;
                                context.getSource().sendSuccess(() -> {
                                    var playerDeathData = TimeLived.getPlayerDeathData(finalRecordHolder);
                                    // Get the formatted time lived
                                    var daysLived = TimeLived.getDaysLived(finalMaxTimeLived);
                                    var previousDaysLived = TimeLived.getDaysLived(playerDeathData.longestTimeLived);

                                    var msg = cfg.queryWorldRecordMessage;
                                    msg = TimeLived.replaceVariable(msg, daysLived, previousDaysLived, finalRecordHolder);
                                    return Component.literal(msg).withStyle(ChatFormatting.GREEN);
                                }, false);
                            }

                            return 1;
                        }))
                )
        ));
    }


}
