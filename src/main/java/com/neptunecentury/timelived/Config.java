package com.neptunecentury.timelived;

import java.util.ArrayList;
import java.util.List;

public class Config implements IConfig {
    /**
     * List of time lived messages to register.
     */
    public ArrayList<TimeLivedMessage> timeLivedMessages;
    public String newRecordMessage = "All right! New record! You surpassed your previous record of %s day(s)!";
    public String queryPlayerMessage = "%s has lived for %s day(s). Previous record is %s day(s).";
    public String statsNotFoundMessage = "Statistics not found for %s.";
    public String timeTravelMessage = "Wait... did you travel back in time?";

    public Config() {

    }

    @Override
    public void setDefaults() {
        this.timeLivedMessages = new ArrayList<TimeLivedMessage>();
        this.timeLivedMessages.addAll(List.of(new TimeLivedMessage[]{
                new TimeLivedMessage(0, "You lived for %s day(s). Uh... maybe next time will be better."),
                new TimeLivedMessage(0.1, "You lived for %s day(s). Let's see if we can last a bit longer next time."),
                new TimeLivedMessage(0.5, "You lived for %s day(s). How about we try that again, shall we?"),
                new TimeLivedMessage(1, "Congrats, you lived for %s day(s)."),
                new TimeLivedMessage(100, "Wow! You lived for %s day(s). That is quite an accomplishment!"),
                new TimeLivedMessage(365, "Amazing! You lived for %s day(s). That's a whole Minecraft year!")
        }));

    }
}
