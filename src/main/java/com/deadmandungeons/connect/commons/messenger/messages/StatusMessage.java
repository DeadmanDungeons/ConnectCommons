package com.deadmandungeons.connect.commons.messenger.messages;

import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;
import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * A Message type that is used to update the {@link Status} of the subject identified by {@link #getId()}
 * @author Jon
 */
@MessageType("status")
public class StatusMessage extends IdentifiableMessage {

    private final Status status;

    public StatusMessage(UUID id, Status status) {
        super(id);
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public void validate() throws InvalidMessageException {
        super.validate();
        if (status == null) {
            throw new InvalidMessageException("status cannot be null");
        }
    }

    public enum Status {
        @SerializedName("online")
        ONLINE,
        @SerializedName("offline")
        OFFLINE
    }

}
