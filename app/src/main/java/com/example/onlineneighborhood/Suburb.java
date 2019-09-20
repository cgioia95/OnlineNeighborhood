package com.example.onlineneighborhood;

import java.util.ArrayList;

public class Suburb {
    private String id;
    private String subName;
    private String postCode;
    private ArrayList<Event> events;



    public Suburb(){

    }

    public Suburb(String id, String subName, String postCode, ArrayList<Event> events) {
        this.id = id;
        this.subName = subName;
        this.postCode = postCode;
        this.events = events;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubName() {
        return subName;
    }

    public void setSubName(String subName) {
        this.subName = subName;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public ArrayList<Event> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<Event> events) {
        this.events = events;
    }
}
