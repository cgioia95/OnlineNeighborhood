package com.example.onlineneighborhood;

import java.io.Serializable;
import java.util.ArrayList;

public class UserInformation implements Serializable {

    private String uid;
    private String name;
    private String preference;
    private String dob;
    private String bio;
    private ArrayList<Event> myEvents;
    private ArrayList<Event> myEventsAttending;

    public UserInformation(){

    }

    public UserInformation(String id){
        this.uid = id;
    }


    public UserInformation( String name, String preference, String dob, String bio) {
        this.name = name;
        this.preference = preference;
        this.dob = dob;
        this.bio = bio;
    }

    public UserInformation(String name, String preference, String dob, String bio, ArrayList<Event> myEvents) {
        this.name = name;
        this.preference = preference;
        this.dob = dob;
        this.bio = bio;
        this.myEvents = myEvents;
        this.myEventsAttending = myEventsAttending;
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

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public ArrayList<Event> getMyEvents() {
        return myEvents;
    }

    public void setMyEvents(ArrayList<Event> myEvents) {
        this.myEvents = myEvents;
    }

    public ArrayList<Event> getMyEventsAttending() {
        return myEventsAttending;
    }

    public void setMyEventsAttending(ArrayList<Event> myEventsAttending) {
        this.myEventsAttending = myEventsAttending;
    }

}
