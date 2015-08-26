package com.deadmandungeons.connect.messenger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;


public final class Messenger {
	
	private final Map<String, Class<? extends Message>> messageTypes;
	private final Gson gson;
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static final class Builder {
		
		private final GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
		private final Map<String, Class<? extends Message>> messageTypes = new HashMap<>();
		
		private Builder() {
			registerMessageType(CommandMessage.class, CommandMessage.CREATOR);
			registerMessageType(StatusMessage.class, StatusMessage.CREATOR);
		}
		
		public <T extends Message> Builder registerMessageType(Class<T> typeClass, InstanceCreator<T> creator) {
			if (typeClass == Message.class || !Message.class.isAssignableFrom(typeClass)) {
				throw new IllegalArgumentException("typeClass must be a subclass of Message");
			}
			String type = Message.getType(typeClass);
			Class<? extends Message> existingTypeClass = messageTypes.get(type);
			if (existingTypeClass != null) {
				if (typeClass != existingTypeClass) {
					throw new IllegalStateException("A Message type named '" + type + "' has already been registered");
				}
				return this;
			}
			
			messageTypes.put(type, typeClass);
			gsonBuilder.registerTypeAdapter(typeClass, creator);
			return this;
		}
		
		public Messenger build() {
			return new Messenger(this);
		}
		
	}
	
	private Messenger(Builder builder) {
		this.messageTypes = Collections.unmodifiableMap(builder.messageTypes);
		gson = builder.gsonBuilder.registerTypeAdapter(Message.class, new MessageDeserializer()).create();
	}
	
	
	public String serialize(Message... messages) {
		// validate messages before serializing
		for (Message msg : messages) {
			if (!msg.isValid()) {
				throw new IllegalArgumentException("serialized messages must be valid");
			}
		}
		return gson.toJson(messages, Message[].class);
	}
	
	public Message[] deserialize(String rawMsg) throws MessageParseException {
		Message[] messages = null;
		if (rawMsg.startsWith("[") && rawMsg.endsWith("]")) {
			messages = getObjFromJson(rawMsg, Message[].class);
			if (messages.length == 0) {
				throw new MessageParseException("Empty json array with no message to parse");
			}
		} else {
			Message message = getObjFromJson(rawMsg, Message.class);
			if (message != null) {
				messages = new Message[] { message };
			}
		}
		return messages;
	}
	
	
	private <T> T getObjFromJson(String rawMsg, Class<T> dataClass) throws MessageParseException {
		try {
			return gson.fromJson(rawMsg, dataClass);
		} catch (JsonParseException e) {
			throw new MessageParseException(e);
		}
	}
	
	private static String normalize(String str) {
		return (str != null ? str.trim().toLowerCase() : null);
	}
	
	
	public static abstract class Message {
		
		private static final Map<Class<? extends Message>, String> types = new HashMap<>();
		
		private final String type;
		private final UUID id;
		
		protected Message(UUID id) {
			this.id = id;
			type = getType(getClass());
		}
		
		public String getType() {
			return type;
		}
		
		public UUID getId() {
			return id;
		}
		
		public final boolean isValid() {
			return id != null && isDataValid();
		}
		
		public abstract Object getData();
		
		protected abstract boolean isDataValid();
		
		private static String getType(Class<? extends Message> messageClass) {
			String type = types.get(messageClass);
			if (type == null) {
				MessageType messageType = messageClass.getAnnotation(MessageType.class);
				if (messageType == null) {
					String msg = "The Message class '" + messageClass + "' must be annotated with the MessageType annotation";
					throw new IllegalStateException(msg);
				}
				type = normalize(messageType.value());
				if (type == null || type.trim().isEmpty()) {
					throw new IllegalStateException("The MessageType annotation value for '" + messageClass + "' cannot be empty");
				}
				types.put(messageClass, type);
			}
			return type;
		}
		
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface MessageType {
		
		String value();
		
	}
	
	public static class MessageParseException extends Exception {
		
		private static final long serialVersionUID = -1131480517964557118L;
		
		public MessageParseException(String message) {
			super(message);
		}
		
		public MessageParseException(JsonParseException cause) {
			super(cause.getMessage(), cause);
		}
		
	}
	
	private class MessageDeserializer implements JsonDeserializer<Message>, JsonSerializer<Message> {
		
		@Override
		public Message deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
			JsonObject jsonObj = json.getAsJsonObject();
			JsonElement messageTypeElem = jsonObj.get("type");
			if (messageTypeElem == null) {
				throw new JsonParseException("Missing 'type' property");
			}
			String messageType = normalize(messageTypeElem.getAsString());
			Class<? extends Message> messageClass = messageTypes.get(messageType);
			if (messageClass == null) {
				throw new JsonParseException("Cannot deserialize json Message of unknown type '" + messageType + "'");
			}
			
			return context.deserialize(json, messageClass);
		}
		
		// This serializer is also required due to a strange gson bug where Message subclass properties wont be serialized
		// if a deserializer for the base class (Message) is present and the subclass is represented as the base class
		@Override
		public JsonElement serialize(Message message, Type type, JsonSerializationContext context) {
			return context.serialize(message, message.getClass());
		}
		
	}
	
}
