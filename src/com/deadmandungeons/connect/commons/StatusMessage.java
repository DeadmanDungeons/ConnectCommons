package com.deadmandungeons.connect.commons;

import java.lang.reflect.Type;
import java.util.UUID;

import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;
import com.google.gson.annotations.SerializedName;

/**
 * A Message type that is used to update the {@link Status} of the subject identified by {@link #getId()}
 * @author Jon
 */
@MessageType("status")
public class StatusMessage extends Message {
	
	public static final MessageCreator<StatusMessage> CREATOR = new MessageCreator<StatusMessage>(StatusMessage.class) {
		
		@Override
		public StatusMessage createInstance(Type type) {
			return new StatusMessage(null, null);
		}
	};
	
	private final Status status;
	
	public StatusMessage(UUID id, Status status) {
		super(id);
		this.status = status;
	}
	
	@Override
	public Status getData() {
		return status;
	}
	
	@Override
	protected boolean isDataValid() {
		return status != null;
	}
	
	public static enum Status {
		@SerializedName("connected")
		CONNECTED,
		@SerializedName("disconnected")
		DISCONNECTED;
	}
	
}
