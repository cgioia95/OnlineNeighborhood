package com.example.onlineneighborhood;

public class UserInformation {

    public String name;
    public String preference;
    public String bio;
    public String dob;

    public UserInformation(){

    }
    public UserInformation(String name, String preference, String dob, String bio) {
        this.name = name;
        this.preference = preference;
        this.bio = bio;
        this.dob = dob;
    }

    public UserInformation(String name, String preference) {
        this.name = name;
        this.preference = preference;
    }
}
