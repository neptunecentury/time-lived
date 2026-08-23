package com.neptunecentury.timelived;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class TimeLivedMessage {
    /**
     * The minimum days lived to trigger the message.
     */
    public double minDaysLived;
    /**
     * Message to display when user died and has reached the minimum days lived.
     */
    @Deprecated
    public String message;

    /**
     * New. Messages are stored as array
     */
    public ArrayList<String> messages;

    public TimeLivedMessage(double minDaysLived, ArrayList<String> messages) {
        this.minDaysLived = minDaysLived;
        this.messages = messages;
    }

    /**
     * Returns a random message from the messages for this instance
     * @return a random message from the messages list
     */
    public String getRandomMessage(){
        return messages.get(
                ThreadLocalRandom.current().nextInt(messages.size())
        );
    }

}
