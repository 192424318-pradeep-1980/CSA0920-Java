package com.candymatch.exceptions;

/**
 * Thrown when custom candy parameters (name, score value, synthesis ingredients) are invalid or malformed.
 */
public class InvalidCandyException extends Exception {
    public InvalidCandyException(String message) {
        super(message);
    }
}
