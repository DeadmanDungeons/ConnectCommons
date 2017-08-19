package com.deadmandungeons.connect.commons.messenger;

import com.deadmandungeons.connect.commons.ConnectUtils;
import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;
import com.deadmandungeons.connect.commons.messenger.exceptions.MessageParseException;
import com.deadmandungeons.connect.commons.messenger.messages.HeartbeatMessage;
import com.deadmandungeons.connect.commons.messenger.messages.Message;
import com.deadmandungeons.connect.commons.messenger.messages.StatusMessage;
import com.deadmandungeons.connect.commons.messenger.serializers.CraftbukkitGsonMessageSerializer;
import com.deadmandungeons.connect.commons.messenger.serializers.GsonMessageSerializer;
import com.deadmandungeons.connect.commons.messenger.serializers.MessageSerializer;
import com.google.common.base.Defaults;
import com.google.common.base.Supplier;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;


/**
 * This class is used as a thread safe utility for easy serialization and deserialization of a {@link Message}.
 * A Message is used to communicate between a server running ConnectMiddleware and its consumer and supplier clients.
 * Messages are serialized to a JSON String, and deserialized back to the Message object while maintaining object type.
 * @author Jon
 */
public final class Messenger {

    private static final Comparator<Constructor<?>> CONSTRUCTOR_COMPARATOR = new Comparator<Constructor<?>>() {
        @Override
        public int compare(Constructor a, Constructor b) {
            return a.getParameterTypes().length - b.getParameterTypes().length;
        }
    };

    private final MessageSerializer serializer;

    /**
     * @return a new {@link Messenger.Builder} to be used to build a new Messenger instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The Builder class for a {@link Messenger} instance that allows different {@link Message} types to be registered
     * and validated before the construction of the Messenger.<br>
     * The {@link StatusMessage} and {@link HeartbeatMessage} types are registered for every Messenger instance.
     */
    public static final class Builder {

        private final MessageSerializer serializer;

        private Builder() {
            if (ConnectUtils.checkClass("com.google.gson.Gson")) {
                serializer = new GsonMessageSerializer();
            } else {
                // servers v1.8.0 and below include Gson shaded at org.bukkit.craftbukkit.libs.com.google.gson
                serializer = new CraftbukkitGsonMessageSerializer();
            }

            registerMessageType(StatusMessage.class);
            registerMessageType(HeartbeatMessage.class);
        }

        /**
         * Register a {@link Message} type for the built Messenger instance. A Message type must be registered
         * in order to deserialize a message of that type using {@link Messenger#deserialize(String)}.
         * @param typeClass the Message type class token to register
         * @return this Builder instance
         * @throws IllegalArgumentException if the given type is not a valid subclass of the {@link Message} abstract class
         */
        public <T extends Message> Builder registerMessageType(Class<T> typeClass) throws IllegalArgumentException {
            if (typeClass == Message.class || !Message.class.isAssignableFrom(typeClass)) {
                throw new IllegalArgumentException("typeClass must be a subclass of Message");
            }
            // throws IllegalArgumentException if type is undefined
            String type = Message.getType(typeClass);
            // throws IllegalArgumentException if unconstructable
            final Constructor<?> messageConstructor = findMessageConstructor(typeClass);

            serializer.registerMessageType(type, typeClass, new Supplier<T>() {

                @Override
                public T get() {
                    return newInstance(messageConstructor);
                }
            });
            return this;
        }

        /**
         * Build the Messenger that will allow messenger for any of the registered Message types to be deserialized.
         * @return the built Messenger instance
         */
        public Messenger build() {
            return new Messenger(this);
        }

    }

    private Messenger(Builder builder) {
        this.serializer = builder.serializer;
    }


    /**
     * @param messages the messenger to serialize
     * @return the JSON of the serialized messenger.
     */
    public String serialize(Collection<Message> messages) {
        return serialize(messages.toArray(new Message[messages.size()]));
    }

    /**
     * @param messages the messenger to serialize
     * @return the JSON of the serialized messenger.
     * @throws IllegalArgumentException if any message is invalid ({@link Message#validate()})
     */
    public String serialize(Message... messages) throws IllegalArgumentException {
        // validate messenger before serializing
        for (Message msg : messages) {
            try {
                msg.validate();
            } catch (InvalidMessageException e) {
                throw new IllegalArgumentException("serialized messenger must be valid", e);
            }
        }
        return serializer.toJson(messages);
    }

    /**
     * This can accept a single JSON Message object, or an array of JSON Message objects.
     * <p><b>Note:</b> The returned Messages will not have been validated yet with {@link Message#validate()}</p>
     * @param rawMsg the raw message(s) in JSON format to deserialize
     * @return an Array of the deserialized Message objects
     * @throws MessageParseException if rawMsg is not a valid representation for a Message of the type it specifies
     */
    public Message[] deserialize(String rawMsg) throws MessageParseException {
        Message[] messages;
        rawMsg = rawMsg.trim();
        if (rawMsg.startsWith("[") && rawMsg.endsWith("]")) {
            messages = serializer.fromJson(rawMsg, Message[].class);
            if (messages.length == 0) {
                throw new MessageParseException("Empty json array with no message to parse");
            }
        } else {
            Message message = serializer.fromJson(rawMsg, Message.class);
            messages = new Message[]{message};
        }
        return messages;
    }


    private static Constructor<?> findMessageConstructor(Class<?> type) throws IllegalArgumentException {
        TreeSet<Constructor<?>> sortedConstructors = new TreeSet<>(CONSTRUCTOR_COMPARATOR);
        Collections.addAll(sortedConstructors, type.getDeclaredConstructors());

        RuntimeException exception = null;
        for (Constructor<?> constructor : sortedConstructors) {
            try {
                newInstance(constructor);
                return constructor;
            } catch (RuntimeException e) {
                exception = e;
            }
        }
        if (exception != null) {
            throw exception;
        }
        throw new IllegalArgumentException("Message type " + type + " is missing a valid constructor");
    }

    private static <T> T newInstance(Constructor<?> constructor) throws IllegalArgumentException {
        try {
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            for (int i = 0; i < params.length; i++) {
                params[i] = Defaults.defaultValue(paramTypes[i]);
            }

            @SuppressWarnings("unchecked")
            T instance = (T) constructor.newInstance(params);
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalArgumentException("Message types must be constructable with empty values", e);
        }
    }

}
