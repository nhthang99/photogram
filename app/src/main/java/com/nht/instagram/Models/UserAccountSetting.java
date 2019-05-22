package com.nht.instagram.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserAccountSetting implements Parcelable {

    private String descriptions;
    private String display_name;
    private long followers;
    private long following;
    private long posts;
    private String profile_photo;
    private String username;
    private String user_id;

    public UserAccountSetting(String descriptions, String display_name, long followers, long following, long posts, String profile_photo, String username, String user_id) {
        this.descriptions = descriptions;
        this.display_name = display_name;
        this.followers = followers;
        this.following = following;
        this.posts = posts;
        this.profile_photo = profile_photo;
        this.username = username;
        this.user_id = user_id;
    }

    public UserAccountSetting(){

    }

    protected UserAccountSetting(Parcel in) {
        descriptions = in.readString();
        display_name = in.readString();
        followers = in.readLong();
        following = in.readLong();
        posts = in.readLong();
        profile_photo = in.readString();
        username = in.readString();
        user_id = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(descriptions);
        dest.writeString(display_name);
        dest.writeLong(followers);
        dest.writeLong(following);
        dest.writeLong(posts);
        dest.writeString(profile_photo);
        dest.writeString(username);
        dest.writeString(user_id);
    }

    public static final Creator<UserAccountSetting> CREATOR = new Creator<UserAccountSetting>() {
        @Override
        public UserAccountSetting createFromParcel(Parcel in) {
            return new UserAccountSetting(in);
        }

        @Override
        public UserAccountSetting[] newArray(int size) {
            return new UserAccountSetting[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(String descriptions) {
        this.descriptions = descriptions;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public long getFollowers() {
        return followers;
    }

    public void setFollowers(long followers) {
        this.followers = followers;
    }

    public long getFollowing() {
        return following;
    }

    public void setFollowing(long following) {
        this.following = following;
    }

    public long getPosts() {
        return posts;
    }

    public void setPosts(long posts) {
        this.posts = posts;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserAccountSetting{" +
                "descriptions='" + descriptions + '\'' +
                ", display_name='" + display_name + '\'' +
                ", followers=" + followers +
                ", following=" + following +
                ", posts=" + posts +
                ", profile_photo='" + profile_photo + '\'' +
                ", username='" + username + '\'' +
                ", user_id='" + user_id + '\'' +
                '}';
    }
}
