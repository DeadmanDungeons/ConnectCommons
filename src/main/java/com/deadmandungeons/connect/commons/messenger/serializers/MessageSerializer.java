package com.deadmandungeons.connect.commons.messenger.serializers;

import com.deadmandungeons.connect.commons.messenger.exceptions.MessageParseException;
import com.deadmandungeons.connect.commons.messenger.messages.Message;
import com.google.common.base.Supplier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public abstract class MessageSerializer {

    private final Map<String, Class<? extends Message>> messageTypes = new ConcurrentHashMap<>();

    protected void addNewMessageType(String messageType, Class<? extends Message> messageClass) {
        String typeName = normalizeTypeName(messageType);
        Class<? extends Message> existingMessageType = messageTypes.get(typeName);
        if (existingMessageType != null && existingMessageType != messageClass) {
            throw new IllegalStateException("A Message type named '" + messageType + "' has already been registered");
        }

        messageTypes.put(typeName, messageClass);
    }

    protected Class<? extends Message> getExistingMessageType(String messageType) throws IllegalArgumentException {
        if (messageType == null) {
            throw new IllegalArgumentException("Missing 'type' property");
        }
        Class<? extends Message> messageClass = messageTypes.get(normalizeTypeName(messageType));
        if (messageClass == null) {
            throw new IllegalArgumentException("Cannot deserialize json Message of unknown type '" + messageType + "'");
        }
        return messageClass;
    }

    protected <T> Map<String, T> getEnumConstants(Class<? super T> enumType) {
        if (enumType.isEnum()) {
            Map<String, T> enumConstants = new HashMap<>();
            for (Object enumValue : enumType.getEnumConstants()) {
                @SuppressWarnings("unchecked")
                T enumConstant = (T) enumValue;
                enumConstants.put(((Enum<?>) enumConstant).name(), enumConstant);
            }
            return enumConstants;
        }
        return Collections.emptyMap();
    }

    private static String normalizeTypeName(String str) {
        return (str != null ? str.trim().toLowerCase() : null);
    }


    public abstract String toJson(Message[] messages);

    public abstract <T> T fromJson(String json, Class<T> messageType) throws MessageParseException;

    public abstract <T extends Message> void registerMessageType(String messageType, Class<T> messageClass, Supplier<T> messageSupplier);

}
