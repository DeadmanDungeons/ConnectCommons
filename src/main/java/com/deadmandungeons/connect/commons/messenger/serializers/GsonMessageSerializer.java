package com.deadmandungeons.connect.commons.messenger.serializers;

import com.deadmandungeons.connect.commons.messenger.exceptions.MessageParseException;
import com.deadmandungeons.connect.commons.messenger.messages.Message;
import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class GsonMessageSerializer extends MessageSerializer {

    private final GsonBuilder builder = new GsonBuilder();
    private volatile Gson gson;

    public GsonMessageSerializer() {
        builder.registerTypeAdapter(Message.class, new MessageDeserializer());
    }

    @Override
    public <T extends Message> void registerMessageType(String messageType, Class<T> messageClass, final Supplier<T> messageSupplier) {
        addNewMessageType(messageType, messageClass);
        builder.registerTypeAdapter(messageClass, new InstanceCreator<T>() {
            @Override
            public T createInstance(Type type) {
                return messageSupplier.get();
            }
        });
        gson = builder.create();
    }

    @Override
    public String toJson(Message[] messages) {
        return gson.toJson(messages);
    }

    @Override
    public <T> T fromJson(String json, Class<T> messageType) throws MessageParseException {
        try {
            return gson.fromJson(json, messageType);
        } catch (JsonParseException e) {
            throw new MessageParseException(e);
        }
    }

    private class MessageDeserializer implements JsonDeserializer<Message>, JsonSerializer<Message> {

        @Override
        public Message deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
            JsonElement messageTypeElem = json.getAsJsonObject().get("type");
            String messageType = (messageTypeElem != null ? messageTypeElem.getAsString() : null);

            return context.deserialize(json, getExistingMessageType(messageType));
        }

        // This serializer is also required due to a strange gson bug where Message subclass properties wont be serialized
        // if a deserializer for the base class (Message) is present and the subclass is represented as the base class
        @Override
        public JsonElement serialize(Message message, Type type, JsonSerializationContext context) {
            return context.serialize(message, message.getClass());
        }

    }

}
