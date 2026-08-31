package com.candymatch.exceptions;

/**
 * Thrown when custom synthesis rule parameters (name, condition, effect string) are invalid or malformed.
 */
public class InvalidRuleException extends Exception {
    public InvalidRuleException(String message) {
        super(message);
    }
}
