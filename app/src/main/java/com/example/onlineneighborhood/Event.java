package com.example.onlineneighborhood;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.ArrayList;

public class Event implements Serializable {
    private String id;
    private UserInformation host;
    private String address, eventName, description, time, date, endTime, endDate, type, hostname;
    private Uri uri;
  

    private ArrayList<UserInformation> attendees;


    public Event(){

    }

    public Event(String id){
        this.id = id;
    }

    public Event(String id, UserInformation host, String address, String eventName, String description, String time, String date, String endTime, String endDate, String type, ArrayList<UserInformation> attendees) {
        this.id = id;
        this.host = host;
        this.address = address;
        this.eventName = eventName;
        this.description = description;
        this.time = time;
        this.date = date;
        this.endTime = endTime;
        this.endDate = endDate;
        this.type = type;
        this.attendees = attendees;
    }


    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public void setUri(Uri image){this.uri = image;}

    public Uri getUri() {
        return uri;
    }
}
