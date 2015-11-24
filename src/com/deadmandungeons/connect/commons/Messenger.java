package com.deadmandungeons.connect.commons;

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


/**
 * This class is used as a utility for easy serialization and deserialization of a {@link Message}.
 * A Message is used to communicate between a server running ConnectMiddleware and its consumer and supplier clients.
 * Messages are serialized to a JSON String, and deserialized back to the Message object while maintaining object type.
 * @author Jon
 */
public final class Messenger {
	
	public static final String SUPPLIER_ID_HEADER = "X-AC-Supplier-ID";
	public static final String SUPPLIER_PASS_HEADER = "X-AC-Supplier-Pass";
	
	private final Map<String, Class<? extends Message>> messageTypes;
	private final Gson gson;
	
	/**
	 * @return a new {@link Messenger.Builder} to be used to build a new Messenger instance
	 */
	public static Builder builder() {
		return new Builder();
	}
	
	/**
	 * The Builder class for a {@link Messenger} instance that allows different {@link Message} types to be registered
	 * and validated before the construction of the Messenger.<br>
	 * The {@link StatusMessage} and {@link CommandMessage} types are registered for every Messenger instance.
	 */
	public static final class Builder {
		
		private final GsonBuilder gsonBuilder = new GsonBuilder().serializeNulls();
		private final Map<String, Class<? extends Message>> messageTypes = new HashMap<>();
		
		private Builder() {
			registerMessageType(CommandMessage.CREATOR);
			registerMessageType(StatusMessage.CREATOR);
		}
		
		/**
		 * Register a {@link Message} type for the built Messenger instance. A Message type must be registered
		 * in order to deserialize a message of that type using {@link Messenger#deserialize(String)}.
		 * @param typeClass - the Message subclass of the type to register
		 * @param creator - an InstanceCreator for the registered type. This creator can simply just return a new
		 * instance of the Message with null values as they will get overwritten with the deserialized values.
		 * @return this Builder instance
		 */
		public <T extends Message> Builder registerMessageType(MessageCreator<T> messageCreator) {
			Class<T> typeClass = messageCreator.messageType;
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
			gsonBuilder.registerTypeAdapter(typeClass, messageCreator);
			return this;
		}
		
		/**
		 * Build the Messenger that will allow messages for any of the registered Message types to be deserialized.
		 * @return the built Messenger instance
		 */
		public Messenger build() {
			return new Messenger(this);
		}
		
	}
	
	private Messenger(Builder builder) {
		this.messageTypes = Collections.unmodifiableMap(builder.messageTypes);
		gson = builder.gsonBuilder.registerTypeAdapter(Message.class, new MessageDeserializer()).create();
	}
	
	
	/**
	 * @param messages - the messages to serialize
	 * @return the JSON of the serialized messages.
	 */
	public String serialize(Message... messages) {
		// validate messages before serializing
		for (Message msg : messages) {
			if (!msg.isValid()) {
				throw new IllegalArgumentException("serialized messages must be valid");
			}
		}
		return gson.toJson(messages, Message[].class);
	}
	
	/**
	 * This can accept a single JSON Message object, or an array of JSON Message objects.
	 * @param rawMsg - the raw message(s) in JSON format to deserialize
	 * @return an Array of the deserialized Message objects
	 * @throws MessageParseException if rawMsg is not a valid representation for a Message of the type it specifies
	 */
	public Message[] deserialize(String rawMsg) throws MessageParseException {
		Message[] messages = null;
		rawMsg = rawMsg.trim();
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
	
	
	/**
	 * This abstract class is the base for a Message which can be serialized to and deserialized from a JSON String.
	 * All subclasses must be annotated with the {@link MessageType} annotation which is used as the type identifier
	 * for deserializing a Message to the proper subclass type.
	 * @author Jon
	 */
	public static abstract class Message {
		
		private static final Map<Class<? extends Message>, String> types = new HashMap<>();
		
		private final String type;
		private final UUID id;
		
		protected Message(UUID id) {
			this.id = id;
			type = getType(getClass());
		}
		
		/**
		 * @return the type of this Message
		 */
		public String getType() {
			return type;
		}
		
		/**
		 * @return the UUID that identifies the subject of this message
		 */
		public UUID getId() {
			return id;
		}
		
		/**
		 * Validate that {@link #getId()} is not null and {@link #getData()} is valid
		 * @return true if the message valid and false otherwise
		 */
		public final boolean isValid() {
			return id != null && isDataValid();
		}
		
		/**
		 * @return the payload data object of this Message
		 */
		public abstract Object getData();
		
		/**
		 * @return true if the payload data object of this message ({@link #getData()}) is valid
		 */
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
	
	public static abstract class MessageCreator<T extends Message> implements InstanceCreator<T> {
		
		private final Class<T> messageType;
		
		public MessageCreator(Class<T> messageType) {
			this.messageType = messageType;
		}
		
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
