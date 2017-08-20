package com.deadmandungeons.connect.commons;

import com.deadmandungeons.connect.commons.messenger.Messenger;
import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;
import com.deadmandungeons.connect.commons.messenger.exceptions.MessageParseException;
import com.deadmandungeons.connect.commons.messenger.messages.Message;
import com.deadmandungeons.connect.commons.messenger.messages.MessageType;
import com.deadmandungeons.connect.commons.messenger.messages.StatusMessage;
import org.junit.Test;

import java.util.Arrays;
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

    @Test
    public void testMessengerDeserializationWithPrivateMessage() throws MessageParseException {
        PrivateMessageType privateMessage = new PrivateMessageType("Private Access Works");

        Messenger messenger = Messenger.builder().registerMessageType(PrivateMessageType.class).build();
        for (int i = 0; i < 5; i++) {
            String json = messenger.serialize(privateMessage);
            Message[] deserialized = messenger.deserialize(json);

            assertTrue(deserialized != null && deserialized.length == 1);
            assertTrue(deserialized[0].getType().equals(privateMessage.getType()));
            assertTrue(((PrivateMessageType) deserialized[0]).value.equals(privateMessage.value));
        }
    }

    @Test
    public void testMessengerDeserializationWithArrayParameter() throws MessageParseException {
        ArrayMessageType arrayMessage = new ArrayMessageType("Arrays Work");

        Messenger messenger = Messenger.builder().registerMessageType(ArrayMessageType.class).build();
        for (int i = 0; i < 5; i++) {
            String json = messenger.serialize(arrayMessage);
            Message[] deserialized = messenger.deserialize(json);

            assertTrue(deserialized != null && deserialized.length == 1);
            assertTrue(deserialized[0].getType().equals(arrayMessage.getType()));
            assertTrue(Arrays.equals(((ArrayMessageType) deserialized[0]).values, arrayMessage.values));
        }
    }

    @Test
    public void testMessengerDeserializationWithEnumFields() throws MessageParseException {
        String statusMessageJson = "[{\"status\":\"ONline\",\"id\":\"780e33be-1d57-4f15-9b8e-370e82c2378b\",\"type\":\"status\"}]";

        Messenger messenger = Messenger.builder().build();
        Message[] deserialized = messenger.deserialize(statusMessageJson);

        assertTrue(deserialized != null && deserialized.length == 1);
        assertTrue(deserialized[0].getType().equals("status"));
        assertTrue(((StatusMessage) deserialized[0]).getStatus() == StatusMessage.Status.ONLINE);
    }

    // TODO add more tests

    @MessageType("$$ INVALID $$")
    public static class InvalidMessageType extends Message {

        @Override
        public void validate() throws InvalidMessageException {
            throw new InvalidMessageException("INVALID");
        }
    }

    @MessageType("private")
    private static class PrivateMessageType extends Message {

        private final String value;

        private PrivateMessageType(String value) {
            this.value = value;
        }

        @Override
        public void validate() throws InvalidMessageException {
            // valid
        }
    }

    @MessageType("array")
    private static class ArrayMessageType extends Message {

        private final String[] values;

        private ArrayMessageType(String... values) {
            this.values = values;
        }

        @Override
        public void validate() throws InvalidMessageException {
            // valid
        }
    }

}
