package com.candymatch.ailab;

import java.sql.Timestamp;

/**
 * UserSession.java
 * Singleton session manager for the currently logged-in Candy AI Lab player.
 * Tracks user identity, login state, and remember-me preference.
 */
public class UserSession {

    private static UserSession instance;

    private int userId;
    private String playerName;
    private String username;
    private String email;
    private Timestamp lastLogin;
    private boolean loggedIn;
    private boolean rememberMe;

    private UserSession() {
        clear();
    }

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Establishes an authenticated session for the given user.
     */
    public void login(int userId, String playerName, String username, String email,
                      Timestamp lastLogin, boolean rememberMe) {
        this.userId = userId;
        this.playerName = playerName;
        this.username = username;
        this.email = email;
        this.lastLogin = lastLogin;
        this.rememberMe = rememberMe;
        this.loggedIn = true;

        if (rememberMe) {
            AuthenticationManager.saveRememberedUsername(username);
        } else {
            AuthenticationManager.clearRememberedUsername();
        }
    }

    /**
     * Clears the current session and returns the player to an unauthenticated state.
     */
    public void logout() {
        clear();
        AuthenticationManager.clearRememberedUsername();
    }

    private void clear() {
        this.userId = -1;
        this.playerName = null;
        this.username = null;
        this.email = null;
        this.lastLogin = null;
        this.loggedIn = false;
        this.rememberMe = false;
    }

    public boolean isLoggedIn() {
        return loggedIn && userId > 0;
    }

    public int getUserId() { return userId; }
    public String getPlayerName() { return playerName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Timestamp getLastLogin() { return lastLogin; }
    public boolean isRememberMe() { return rememberMe; }
}
