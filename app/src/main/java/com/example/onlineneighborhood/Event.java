package com.example.onlineneighborhood;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Event {
    private String id;
    private FirebaseUser host;
    private String suburb;
    private String address;
    private String name;
    private String description;
    private String time;
    private String date;

    private ArrayList<FirebaseUser> attendees;





    public Event(){

    }

    public Event(String id, FirebaseUser host, String suburb, String address, String name, String description, String time, String date, ArrayList<FirebaseUser> attendees) {
        this.id = id;
        this.host = host;
        this.suburb = suburb;
        this.address = address;
        this.name = name;
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

    public FirebaseUser getHost() {
        return host;
    }

    public void setHost(FirebaseUser host) {
        this.host = host;
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ArrayList<FirebaseUser> getAttendees() {
        return attendees;
    }

    public void setAttendees(ArrayList<FirebaseUser> attendees) {
        this.attendees = attendees;
    }
}
