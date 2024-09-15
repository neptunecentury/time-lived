package com.neptunecentury.timelived;

import java.util.ArrayList;
import java.util.List;

public class Config implements IConfig {
    /**
     * List of time lived messages to register.
     */
    public ArrayList<TimeLivedMessage> timeLivedMessages;
    public String defaultTimeLivedMessage = "You lived for {daysLived} day(s). Maybe next time will be better.";
    public String newRecordMessage = "All right! New record! You surpassed your previous record of {previousDaysLived} day(s)!";
    public String queryPlayerMessage = "{playerName} has lived for {daysLived} day(s). Previous record is {previousDaysLived} day(s).";
    public String statsNotFoundMessage = "Statistics not found for {playerName}.";
    public String timeTravelMessage = "Wait... did you travel back in time?";

    public Config() {

    }

    @Override
    public void setDefaults() {
        this.timeLivedMessages = new ArrayList<>();
        this.timeLivedMessages.addAll(List.of(new TimeLivedMessage[]{
                new TimeLivedMessage(0.1, "You lived for {daysLived} day(s). Let's see if we can last a bit longer next time."),
                new TimeLivedMessage(0.5, "You lived for {daysLived} day(s). How about we try that again, shall we?"),
                new TimeLivedMessage(1, "Congrats, you lived for {daysLived} day(s)."),
                new TimeLivedMessage(100, "Wow! You lived for {daysLived} day(s). That is quite an accomplishment!"),
                new TimeLivedMessage(365, "Amazing! You lived for {daysLived} day(s). That's a whole Minecraft year!"),
                new TimeLivedMessage(366, "Amazing! You lived for {daysLived} day(s). That's seriously impressive!"),
                new TimeLivedMessage(500, "Incredible! You lived for {daysLived} day(s)! You're a legend!")
        }));

    }
}
