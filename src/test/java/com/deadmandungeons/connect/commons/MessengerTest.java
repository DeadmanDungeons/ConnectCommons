package com.deadmandungeons.connect.commons;

import com.deadmandungeons.connect.commons.messenger.Messenger;
import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;
import com.deadmandungeons.connect.commons.messenger.exceptions.MessageParseException;
import com.deadmandungeons.connect.commons.messenger.messages.Message;
import com.deadmandungeons.connect.commons.messenger.messages.MessageType;
import com.deadmandungeons.connect.commons.messenger.messages.StatusMessage;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertTrue;


public class MessengerTest {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMessageTypeRegistration() {
        Messenger.builder().registerMessageType(InvalidMessageType.class).build();
    }

    @Test
    public void testMessengerSerialization() throws MessageParseException {
        StatusMessage statusMessage = new StatusMessage(UUID.randomUUID(), StatusMessage.Status.ONLINE);

        Messenger messenger = Messenger.builder().registerMessageType(StatusMessage.class).build();
        String json = messenger.serialize(statusMessage);
        Message[] deserialized = messenger.deserialize(json);

        assertTrue(deserialized != null && deserialized.length == 1);
        assertTrue(deserialized[0].getType().equals(statusMessage.getType()));
        assertTrue(((StatusMessage) deserialized[0]).getId().equals(statusMessage.getId()));
        assertTrue(((StatusMessage) deserialized[0]).getStatus() == statusMessage.getStatus());
    }

    // TODO add more tests

    @MessageType("$$ INVALID $$")
    public static class InvalidMessageType extends Message {

        @Override
        public void validate() throws InvalidMessageException {
            throw new InvalidMessageException("INVALID");
        }
    }

}
