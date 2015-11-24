package com.deadmandungeons.connect.messenger;

import java.lang.reflect.Type;
import java.util.UUID;

import com.deadmandungeons.connect.messenger.Messenger.Message;
import com.deadmandungeons.connect.messenger.Messenger.MessageType;
import com.google.gson.InstanceCreator;
import com.google.gson.annotations.SerializedName;

@MessageType("status")
public class StatusMessage extends Message {
	
	public static final InstanceCreator<StatusMessage> CREATOR = new InstanceCreator<StatusMessage>() {
		
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
