package com.deadmandungeons.connect.commons;

import java.lang.reflect.Type;

import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;

@MessageType("heartbeat")
public class HeartbeatMessage extends Message {
	
	public static final MessageCreator<HeartbeatMessage> CREATOR = new MessageCreator<HeartbeatMessage>(HeartbeatMessage.class) {
		
		@Override
		public HeartbeatMessage createInstance(Type type) {
			return new HeartbeatMessage(null);
		}
	};
	
	private String data;
	
	public HeartbeatMessage(String data) {
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
	
	@Override
	public boolean isValid() {
		return data != null;
	}
	
}
