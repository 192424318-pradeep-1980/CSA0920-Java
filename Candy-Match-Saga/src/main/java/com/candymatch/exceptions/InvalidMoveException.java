package com.candymatch.exceptions;

/**
 * Thrown when a player attempts an invalid candy swap or move (out of bounds, non-adjacent, or no matches formed).
 */
public class InvalidMoveException extends Exception {
    public InvalidMoveException(String message) {
        super(message);
    }
}
