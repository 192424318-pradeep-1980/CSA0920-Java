package com.candymatch.exceptions;

/**
 * Thrown when database operation fails (JDBC connection, table creation, or PreparedStatement query execution errors).
 */
public class DatabaseException extends Exception {
    public DatabaseException(String message) {
        super(message);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
