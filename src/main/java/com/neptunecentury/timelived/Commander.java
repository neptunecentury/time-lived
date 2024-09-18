package com.neptunecentury.timelived;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
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
                        // Add argument for a player entity
                        .then(CommandManager.argument("player", EntityArgumentType.players())
                                .executes(context -> {
                                            var players = EntityArgumentType.getPlayers(context, "player");

                                            players.forEach((player) -> {
                                                // Get the stats for the player
                                                var playerDeathData = TimeLived.getDaysLivedForPlayer(player);
                                                if (playerDeathData != null) {
                                                    // Get the current world time
                                                    var currentTime = player.getServerWorld().getTimeOfDay();
                                                    var timeLived = currentTime - playerDeathData.timePlayerLastDied;
                                                    // Get the formatted time lived
                                                    var daysLived = TimeLived.getDaysLived(timeLived);
                                                    var previousDaysLived = TimeLived.getDaysLived(playerDeathData.longestTimeLived);

                                                    context.getSource().sendFeedback(() -> {
                                                        String msg = cfg.queryPlayerMessage;
                                                        msg = TimeLived.replaceVariable(msg, daysLived, previousDaysLived, player);
                                                        return Text.literal(msg).formatted(Formatting.GREEN);
                                                    }, false);
                                                } else {
                                                    context.getSource().sendFeedback(() -> {
                                                        String msg = cfg.statsNotFoundMessage;
                                                        msg = TimeLived.replaceVariable(msg, 0, 0, player);
                                                        return Text.literal(msg).formatted(Formatting.RED);
                                                    }, false);
                                                }
                                            });
                                            return 1;
                                        }
                                )
                        )
                )
        ));
    }
}
