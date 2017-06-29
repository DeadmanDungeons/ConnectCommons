package com.deadmandungeons.connect.commons;

import com.deadmandungeons.connect.commons.Messenger.IdentifiableMessage;
import com.deadmandungeons.connect.commons.Messenger.InvalidDataException;
import com.deadmandungeons.connect.commons.Messenger.MessageCreator;
import com.deadmandungeons.connect.commons.Messenger.MessageType;
import org.bukkit.craftbukkit.libs.com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * A Message type that is used to update the {@link Status} of the subject identified by {@link #getId()}
 * @author Jon
 */
@MessageType("status")
public class StatusMessage extends IdentifiableMessage {

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

    public Status getStatus() {
        return status;
    }

    @Override
    public void validate() throws InvalidDataException {
        super.validate();
        if (status == null) {
            throw new InvalidDataException("status cannot be null");
        }
    }

    public enum Status {
        @SerializedName("online")
        ONLINE,
        @SerializedName("offline")
        OFFLINE
    }

}
