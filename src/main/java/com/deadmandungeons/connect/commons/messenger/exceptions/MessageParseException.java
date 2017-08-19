package com.deadmandungeons.connect.commons.messenger.exceptions;

public class MessageParseException extends Exception {

    private static final long serialVersionUID = -1131480517964557118L;

    public MessageParseException(String message) {
        super(message);
    }

    public MessageParseException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
