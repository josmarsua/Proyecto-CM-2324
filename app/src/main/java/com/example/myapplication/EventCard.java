package com.example.myapplication;

import android.net.Uri;

import java.util.Date;

public class EventCard {
    String eventName;
    int eventId;
    byte[] image;
    Date date;
    String location;
    boolean promoted;
    String category;

    public EventCard(String eventName, int eventId, byte[] image, Date date, String location, String category, boolean promoted) {
        this.eventName = eventName;
        this.eventId = eventId;
        this.image = image;
        this.date = date;
        this.location = location;
        this.promoted = false;
        this.category = category;
        this.promoted = promoted;
    }

    public String getLocation() {
        return location;
    }

    public Date getDate() {
        return date;
    }

    public byte[] getImage() {
        return image;
    }

    public int getEventId() {
        return eventId;
    }

    public String getEventName() {
        return eventName;
    }

    public String getCategory() { return category; }

}
