package com.deadmandungeons.connect.commons.messenger.exceptions;

import java.util.Arrays;

public class IdentifierSyntaxException extends InvalidMessageException {

    private static final long serialVersionUID = 8567778573045685621L;

    private final SyntaxError error;
    private final Object[] errorVars;

    public IdentifierSyntaxException(SyntaxError error, Object... errorVars) {
        super(String.format(error.message, errorVars));
        this.error = error;
        this.errorVars = errorVars;
    }

    public SyntaxError getError() {
        return error;
    }

    public Object[] getErrorVars() {
        return Arrays.copyOf(errorVars, errorVars.length);
    }

    public enum SyntaxError {
        EMPTY("Identifier cannot be empty"),
        MIN_LENGTH("Identifier cannot be less than %s characters"),
        MAX_LENGTH("Identifier cannot be greater than %s characters"),
        INVALID_CHAR("Identifier contains invalid character '%s'");

        private final String message;

        SyntaxError(String message) {
            this.message = message;
        }
    }

}
