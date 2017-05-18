package com.deadmandungeons.connect.commons;

import com.deadmandungeons.connect.commons.Messenger.InvalidDataException;
import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;

import java.lang.reflect.Type;

@MessageType("heartbeat")
public class HeartbeatMessage extends Message {

    public static final MessageCreator<HeartbeatMessage> CREATOR = new MessageCreator<HeartbeatMessage>(HeartbeatMessage.class) {

        @Override
        public HeartbeatMessage createInstance(Type type) {
            return new HeartbeatMessage(null);
        }
    };

    private String data;

    public HeartbeatMessage(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    @Override
    public void validate() throws InvalidDataException {
        if (data == null) {
            throw new InvalidDataException("heartbeat data cannot be null");
        }
    }

}
