package com.deadmandungeons.connect.commons.messenger.messages;

import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;

@MessageType("heartbeat")
public class HeartbeatMessage extends Message {

    private String data;

    public HeartbeatMessage(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public void validate() throws InvalidMessageException {
        if (data == null) {
            throw new InvalidMessageException("heartbeat data cannot be null");
        }
    }

}
