package com.neptunecentury.timelived;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Config implements IConfig {
    /**
     * Version of the config file
     */
    public Integer configVersion;
    /**
     * List of time lived messages to register.
     */
    public ArrayList<TimeLivedMessage> timeLivedMessages;
    /**
     * List of time lived messages to register for other players
     */
    public ArrayList<TimeLivedMessage> timeLivedMessagesToOthers;
    /**
     * Message sent when player reached a new record
     */
    public String newRecordMessage = "All right! New record! You surpassed your previous record of {previousDaysLived} day(s)!";
    /**
     * Message sent to others when a player has reached a new record.
     */
    public String newRecordMessageToOthers = "All right! {playerName} surpassed their previous record of {previousDaysLived} day(s)!";
    public String queryPlayerMessage = "{playerName} has lived for {daysLived} day(s). Previous record is {previousDaysLived} day(s).";
    public String queryWorldRecordMessage = "The current record holder for longest time lived is {playerName}, surviving a total of {daysLived} day(s).";
    public String statsNotFoundMessage = "Statistics not found for {playerName}.";
    public String timeTravelMessage = "Wait... did you travel back in time?";
    public boolean enableMessagesToOthers = true;

    public Config() {

    }

    /**
     * Sets the defaults for the configuration and handles any upgrades
     *
     * @return true if the configuration needs to be saved to disk
     */
    @Override
    public boolean setDefaults() {
        var needsSaving = false;

        // Set config version if not present. This will be used in future upgrade paths
        if (this.configVersion == null || this.configVersion < 1) {
            this.configVersion = 1;
            needsSaving = true;
        }

        // Build default time lived messages
        if (this.timeLivedMessages == null) {
            // Configure default messages to player
            this.timeLivedMessages = new ArrayList<>(List.of(new TimeLivedMessage[]{
                    new TimeLivedMessage(500, new ArrayList<>(List.of("Incredible! You lived for {daysLived} day(s)! You're a legend!"))),
                    new TimeLivedMessage(366, new ArrayList<>(List.of("Amazing! You lived for {daysLived} day(s). That's seriously impressive!"))),
                    new TimeLivedMessage(365, new ArrayList<>(List.of("Amazing! You lived for {daysLived} day(s). That's a whole Minecraft year!"))),
                    new TimeLivedMessage(100, new ArrayList<>(List.of("Wow! You lived for {daysLived} day(s). That is quite an accomplishment!"))),
                    new TimeLivedMessage(1, new ArrayList<>(List.of("Congrats, you lived for {daysLived} day(s)."))),
                    new TimeLivedMessage(0.5, new ArrayList<>(List.of("You lived for {daysLived} day(s). How about we try that again, shall we?"))),
                    new TimeLivedMessage(0.1, new ArrayList<>(List.of("You lived for {daysLived} day(s). Let's see if we can last a bit longer next time."))),
                    new TimeLivedMessage(0, new ArrayList<>(List.of("You lived for {daysLived} day(s). Maybe next time will be better.")))
            }));

            needsSaving = true;
        }

        // Build default time lived messages sent to other players
        if (this.timeLivedMessagesToOthers == null) {
            // Configure default messages to other players
            this.timeLivedMessagesToOthers = new ArrayList<>(List.of(new TimeLivedMessage[]{
                    new TimeLivedMessage(500, new ArrayList<>(List.of("Incredible! {playerName} lived for {daysLived} day(s)! Legendary!"))),
                    new TimeLivedMessage(366, new ArrayList<>(List.of("Amazing! {playerName} lived for {daysLived} day(s). That's seriously impressive!"))),
                    new TimeLivedMessage(365, new ArrayList<>(List.of("Amazing! {playerName} lived for {daysLived} day(s). That's a whole Minecraft year!"))),
                    new TimeLivedMessage(100, new ArrayList<>(List.of("Wow! {playerName} lived for {daysLived} day(s). That is quite an accomplishment!"))),
                    new TimeLivedMessage(1, new ArrayList<>(List.of("{playerName} lived for {daysLived} day(s)."))),
                    new TimeLivedMessage(0, new ArrayList<>(List.of("{playerName} only lived for {daysLived} day(s). Let's give them some encouragement!"))),

            }));

            needsSaving = true;
        }

        // Config upgrade
        if (this.configVersion < 2) {
            // Update the config version
            this.configVersion = 2;
            // Convert the loaded messages to array format and save them
            upgradeMessages(this.timeLivedMessages);
            upgradeMessages(this.timeLivedMessagesToOthers);

            needsSaving = true;
        }

        // Order and reverse the time lived messages according to the min days lived
        this.timeLivedMessages.sort(Comparator.comparingDouble((TimeLivedMessage tlm) -> tlm.minDaysLived));
        Collections.reverse(this.timeLivedMessages);

        // Order and reverse the time lived messages to others according to the min days lived
        this.timeLivedMessagesToOthers.sort(Comparator.comparingDouble((TimeLivedMessage tlm) -> tlm.minDaysLived));
        Collections.reverse(this.timeLivedMessagesToOthers);

        return needsSaving;
    }

    /**
     * Upgrades the old config to the new version
     * @param messages List of messages to convert
     */
    private void upgradeMessages(ArrayList<TimeLivedMessage> messages) {
        // Convert the loaded messages to array format and save them
        for (var tlm : messages) {
            // Take the message property and put it in the messages array list.
            if (tlm.messages == null) {
                tlm.messages = new ArrayList<>();
                if (tlm.message != null) {
                    tlm.messages.add(tlm.message);
                    // Clear the old message property
                    tlm.message = null;
                }

            }

        }
    }

}
