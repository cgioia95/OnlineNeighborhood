package com.example.onlineneighborhood;

public class EventCard {
    private String mEvent;
    private String mUserName;


    public EventCard(String mEvent, String mUserName) {
        this.mEvent = mEvent;
        this.mUserName = mUserName;

    }

    public String getmEvent() {
        return mEvent;
    }

    public void setmEvent(String mEvent) {
        this.mEvent = mEvent;
    }

    public String getmUserName() {
        return mUserName;
    }

    public void setmUserName(String mUserName) {
        this.mUserName = mUserName;
    }
}
