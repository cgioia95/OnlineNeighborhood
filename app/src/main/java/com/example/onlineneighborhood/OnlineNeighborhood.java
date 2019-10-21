package com.example.onlineneighborhood;

import android.app.Application;

public class OnlineNeighborhood extends Application {


    private String suburb, suburbName;
    private int picChange =0;

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

    public int getPicChange() {
        return picChange;
    }

    public void setPicChange(int picChange) {
        this.picChange = picChange;
    }
}
