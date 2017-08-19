package com.deadmandungeons.connect.commons.messenger.messages;

import com.deadmandungeons.connect.commons.ConnectUtils;
import com.deadmandungeons.connect.commons.messenger.exceptions.IdentifierSyntaxException;
import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This abstract class is the base for a Message which can be serialized to and deserialized from a JSON String.
 * All subclasses must be annotated with the {@link MessageType} annotation which is used as the type identifier
 * for deserializing a Message to the proper subclass type.
 * @author Jon
 */
public abstract class Message {

    private static final Map<Class<? extends Message>, String> types = new ConcurrentHashMap<>();

    private final String type;

    protected Message() throws IllegalStateException {
        type = getType(getClass());
    }

    /**
     * @return the type of this Message
     */
    public String getType() {
        return type;
    }


    /**
     * Validate that this message and its data is valid
     * @throws InvalidMessageException if this message and its data is invalid
     */
    public abstract void validate() throws InvalidMessageException;

    /**
     * @param messageClass the Message subclass type to get the corresponding MessageType value for
     * @throws IllegalArgumentException if the given Message subclass is not properly annotated with a {@link MessageType}
     */
    public static String getType(Class<? extends Message> messageClass) throws IllegalArgumentException {
        String type = types.get(messageClass);
        if (type == null) {
            MessageType messageType = messageClass.getAnnotation(MessageType.class);
            if (messageType == null) {
                String msg = "The Message class '" + messageClass + "' must be annotated with the MessageType annotation";
                throw new IllegalArgumentException(msg);
            }
            type = messageType.value().trim().toLowerCase();
            try {
                ConnectUtils.validateIdentifier(type);
            } catch (IdentifierSyntaxException e) {
                throw new IllegalArgumentException("The MessageType annotation value is invalid: " + e.getMessage());
            }

            types.put(messageClass, type);
        }
        return type;
    }

}
