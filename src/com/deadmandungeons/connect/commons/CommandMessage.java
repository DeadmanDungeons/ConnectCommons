package com.deadmandungeons.connect.commons;

import java.lang.reflect.Type;
import java.util.UUID;

import com.deadmandungeons.connect.commons.Messenger.IdentifiableMessage;
import com.deadmandungeons.connect.commons.Messenger.Message;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;
import com.google.gson.annotations.SerializedName;

/**
 * A message type that is used to send a {@link Command} for the subject identified by {@link #getId()}
 * @author Jon
 */
@MessageType("command")
public class CommandMessage extends IdentifiableMessage {
	
	public static final MessageCreator<CommandMessage> CREATOR = new MessageCreator<CommandMessage>(CommandMessage.class) {
		
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
