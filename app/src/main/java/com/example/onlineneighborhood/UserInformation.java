package com.example.onlineneighborhood;

import java.io.Serializable;

public class UserInformation implements Serializable {

    public String uid;
    public String name;
    public String preference;
    public String dob;
    public String bio;

    public UserInformation(){

    }

    public UserInformation(String name, String preference) {
        this.name = name;
        this.preference = preference;
    }


    public UserInformation(String uid, String name, String preference) {
        this.uid = uid;
        this.name = name;
        this.preference = preference;
    }

    public UserInformation(String name, String preference, String dob, String bio) {
        this.name = name;
        this.preference = preference;
        this.dob = dob;
        this.bio = bio;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreference() {
        return preference;
    }

    public void setPreference(String preference) {
        this.preference = preference;
    }




}
