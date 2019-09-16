package com.example.onlineneighborhood;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Event {
    private String id;
    private UserInformation host;
    private String suburb;
    private String address;
    private String eventName;
    private String description;
    private String time;
    private String date;

    private ArrayList<UserInformation> attendees;


    public Event(){

    }

    public Event(String id, UserInformation host, String suburb, String address, String eventName, String description, String time, String date, ArrayList<UserInformation> attendees) {
        this.id = id;
        this.host = host;
        this.suburb = suburb;
        this.address = address;
        this.eventName = eventName;
        this.description = description;
        this.time = time;
        this.date = date;
        this.attendees = attendees;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSuburb() {
        return suburb;
    }

    public void setSuburb(String suburb) {
        this.suburb = suburb;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getName() {
        return eventName;
    }

    public void setName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public UserInformation getHost() {
        return host;
    }

    public void setHost(UserInformation host) {
        this.host = host;
    }

    public ArrayList<UserInformation> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<UserInformation> attendees) {
        this.attendees = attendees;
    }
}
