package com.example.onlineneighborhood;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class Event implements Parcelable {
    private String id;
    private UserInformation host;
    private String address;
    private String eventName;
    private String description;
    private String time;
    private String suburb;
    private String date;
  

    private ArrayList<UserInformation> attendees;


    public Event(){

    }

    public Event(String id, UserInformation host, String address, String eventName, String description, String time, String date, ArrayList<UserInformation> attendees) {
        this.id = id;
        this.host = host;
        this.address = address;
        this.eventName = eventName;
        this.description = description;
        this.time = time;
        this.date = date;
        this.attendees = attendees;
    }

    protected Event(Parcel in) {
        id = in.readString();
        suburb = in.readString();
        address = in.readString();
        eventName = in.readString();
        description = in.readString();
        time = in.readString();
        date = in.readString();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(suburb);
        dest.writeString(address);
        dest.writeString(suburb);
        dest.writeString(eventName);
        dest.writeString(description);
        dest.writeString(time);
        dest.writeString(date);
    }
}
