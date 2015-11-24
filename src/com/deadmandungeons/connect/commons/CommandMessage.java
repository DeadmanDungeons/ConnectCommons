package com.deadmandungeons.connect.messenger;

import java.lang.reflect.Type;
import java.util.UUID;

import com.deadmandungeons.connect.messenger.Messenger.Message;
import com.deadmandungeons.connect.messenger.Messenger.MessageType;
import com.google.gson.InstanceCreator;
import com.google.gson.annotations.SerializedName;

@MessageType("command")
public class CommandMessage extends Message {
	
	public static final InstanceCreator<CommandMessage> CREATOR = new InstanceCreator<CommandMessage>() {
		
		@Override
		public CommandMessage createInstance(Type type) {
			return new CommandMessage(null, null);
		}
	};
	
	private final Command command;
	
	public CommandMessage(UUID id, Command command) {
		super(id);
		this.command = command;
	}
	
	@Override
	public Command getData() {
		return command;
	}
	
	@Override
	public boolean isDataValid() {
		return command != null;
	}
	
	public static enum Command {
		@SerializedName("add")
		ADD,
		@SerializedName("remove")
		REMOVE;
		// more may be added
	}

}
