package com.deadmandungeons.connect.commons.messenger.exceptions;

public class InvalidMessageException extends Exception {

    private static final long serialVersionUID = 330763119431699203L;

    public InvalidMessageException(String message) {
        super(message);
    }

    public InvalidMessageException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
