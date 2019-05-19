package com.nht.instagram.Models;

public class UserSettings {
    private UserAccountSetting settings;
    private User user;

    public UserSettings(UserAccountSetting settings, User user) {
        this.settings = settings;
        this.user = user;
    }

    public UserSettings() {

    }

    public UserAccountSetting getSettings() {
        return settings;
    }

    public void setSettings(UserAccountSetting settings) {
        this.settings = settings;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "UserSettings{" +
                "settings=" + settings +
                ", user=" + user +
                '}';
    }
}
