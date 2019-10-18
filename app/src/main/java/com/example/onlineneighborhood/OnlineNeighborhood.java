package com.example.onlineneighborhood;

import android.app.Application;

public class OnlineNeighborhood extends Application {


    private String suburb, suburbName;

    public String getsuburb() {
        return suburb;
    }

    public void setsuburb(String someVariable) {
        this.suburb = someVariable;
    }

    public String getSuburbName() {
        return suburbName;
    }

    public void setSuburbName(String suburbName) {
        this.suburbName = suburbName;
    }
}
