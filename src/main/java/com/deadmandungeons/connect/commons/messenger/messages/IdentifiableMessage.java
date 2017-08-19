package com.deadmandungeons.connect.commons.messenger.messages;

import com.deadmandungeons.connect.commons.messenger.exceptions.InvalidMessageException;

import java.util.UUID;

public class IdentifiableMessage extends Message {

    private final UUID id;

    protected IdentifiableMessage(UUID id) {
        this.id = id;
    }

    /**
     * @return the UUID that identifies the subject of this message
     */
    public UUID getId() {
        return id;
    }

    @Override
    public void validate() throws InvalidMessageException {
        if (id == null) {
            throw new InvalidMessageException("id cannot be null");
        }
    }

}
