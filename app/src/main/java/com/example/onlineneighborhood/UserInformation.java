package com.example.onlineneighborhood;

public class UserInformation {

    public String name;
    public String preference;

    public UserInformation(){

    }

    public UserInformation(String name, String preference) {
        this.name = name;
        this.preference = preference;
    }

    public String getName() {
        return name;
    }
}
