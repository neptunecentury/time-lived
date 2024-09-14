package com.neptunecentury.timelived;

public class TimeLivedMessage {
    /**
     * The minimum days lived to trigger the message.
     */
    public double minDaysLived;
    /**
     * Message to display when user died and has reached the minimum days lived.
     */
    public String message;

    public TimeLivedMessage() {
    }

    public TimeLivedMessage(double minDaysLived, String message) {
        this.minDaysLived = minDaysLived;
        this.message = message;
    }
}
