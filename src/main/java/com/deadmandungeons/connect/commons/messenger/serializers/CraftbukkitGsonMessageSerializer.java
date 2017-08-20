package com.deadmandungeons.connect.commons.messenger.serializers;

import com.deadmandungeons.connect.commons.messenger.exceptions.MessageParseException;
import com.deadmandungeons.connect.commons.messenger.messages.Message;
import com.google.common.base.Supplier;
import org.bukkit.craftbukkit.libs.com.google.gson.Gson;
import org.bukkit.craftbukkit.libs.com.google.gson.GsonBuilder;
import org.bukkit.craftbukkit.libs.com.google.gson.InstanceCreator;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonDeserializationContext;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonDeserializer;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonElement;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonParseException;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonSerializationContext;
import org.bukkit.craftbukkit.libs.com.google.gson.JsonSerializer;
import org.bukkit.craftbukkit.libs.com.google.gson.TypeAdapter;
import org.bukkit.craftbukkit.libs.com.google.gson.TypeAdapterFactory;
import org.bukkit.craftbukkit.libs.com.google.gson.reflect.TypeToken;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonReader;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonToken;
import org.bukkit.craftbukkit.libs.com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;

public class CraftbukkitGsonMessageSerializer extends MessageSerializer {

    private final GsonBuilder builder = new GsonBuilder();
    private volatile Gson gson;

    public CraftbukkitGsonMessageSerializer() {
        builder.registerTypeAdapter(Message.class, new MessageDeserializer());
        builder.registerTypeAdapterFactory(new EnumTypeAdapterFactory());
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

    // Ignore case of enum constants
    private class EnumTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            final Map<String, T> enumConstants = getEnumConstants(type.getRawType());
            if (enumConstants.isEmpty()) {
                return null;
            }
            // Backwards compatibility hack
            final boolean writeLower = type.getRawType().getSimpleName().equals("Status");

            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (value == null) {
                        out.nullValue();
                    } else {
                        String enumName = ((Enum<?>) value).name();
                        out.value(writeLower ? enumName.toLowerCase() : enumName);
                    }
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    if (in.peek() == JsonToken.NULL) {
                        in.nextNull();
                        return null;
                    }
                    return enumConstants.get(in.nextString().toUpperCase());
                }
            };
        }

    }

}
