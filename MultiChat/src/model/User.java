package model;

public class User {

    private String username;
    private String password;
    private boolean online;

    // Constructor
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.online = false;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isOnline() {
        return online;
    }

    // Setters
    public void setPassword(String password) {
        this.password = password;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}