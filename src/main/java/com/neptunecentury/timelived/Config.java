package com.neptunecentury.timelived;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Config implements IConfig {
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
    public String statsNotFoundMessage = "Statistics not found for {playerName}.";
    public String timeTravelMessage = "Wait... did you travel back in time?";
    public boolean enableMessagesToOthers = true;

    public Config() {

    }

    @Override
    public void setDefaults() {
        // Configure default messages to player
        this.timeLivedMessages = new ArrayList<>(List.of(new TimeLivedMessage[]{
                new TimeLivedMessage(0, "You lived for {daysLived} day(s). Maybe next time will be better."),
                new TimeLivedMessage(0.1, "You lived for {daysLived} day(s). Let's see if we can last a bit longer next time."),
                new TimeLivedMessage(0.5, "You lived for {daysLived} day(s). How about we try that again, shall we?"),
                new TimeLivedMessage(1, "Congrats, you lived for {daysLived} day(s)."),
                new TimeLivedMessage(100, "Wow! You lived for {daysLived} day(s). That is quite an accomplishment!"),
                new TimeLivedMessage(365, "Amazing! You lived for {daysLived} day(s). That's a whole Minecraft year!"),
                new TimeLivedMessage(366, "Amazing! You lived for {daysLived} day(s). That's seriously impressive!"),
                new TimeLivedMessage(500, "Incredible! You lived for {daysLived} day(s)! You're a legend!")
        }));

        // Configure default messages to other players
        this.timeLivedMessagesToOthers = new ArrayList<>(List.of(new TimeLivedMessage[]{
                new TimeLivedMessage(0, "{playerName} only lived for {daysLived} day(s). Let's give them some encouragement!"),
                new TimeLivedMessage(1, "{playerName} lived for {daysLived} day(s)."),
                new TimeLivedMessage(100, "Wow! {playerName} lived for {daysLived} day(s). That is quite an accomplishment!"),
                new TimeLivedMessage(365, "Amazing! {playerName} lived for {daysLived} day(s). That's a whole Minecraft year!"),
                new TimeLivedMessage(366, "Amazing! {playerName} lived for {daysLived} day(s). That's seriously impressive!"),
                new TimeLivedMessage(500, "Incredible! {playerName} lived for {daysLived} day(s)! Legendary!")
        }));

    }

    @Override
    public void prepareData() {
        if (this.timeLivedMessages == null){
            this.timeLivedMessages = new ArrayList<>();
        }

        // Order and reverse the time lived messages
        this.timeLivedMessages.sort(Comparator.comparingDouble((TimeLivedMessage tlm) -> tlm.minDaysLived));
        Collections.reverse(this.timeLivedMessages);

        if (this.timeLivedMessagesToOthers == null){
            this.timeLivedMessagesToOthers = new ArrayList<>();
        }

        // Order and reverse the time lived messages to others
        this.timeLivedMessagesToOthers.sort(Comparator.comparingDouble((TimeLivedMessage tlm) -> tlm.minDaysLived));
        Collections.reverse(this.timeLivedMessagesToOthers);
    }
}
