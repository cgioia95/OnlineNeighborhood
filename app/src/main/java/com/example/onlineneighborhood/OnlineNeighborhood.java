package com.example.onlineneighborhood;

import android.app.Application;

public class OnlineNeighborhood extends Application {


    private String suburb;

    public String getsuburb() {
        return suburb;
    }

    public void setsuburb(String someVariable) {
        this.suburb = someVariable;
    }
}
