package com.example.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"event_id", "user_id"},
        unique = true)})
public class Attendee {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "event_id")
    public int eventId;

    @ColumnInfo(name = "user_id")
    public int userId;

    public Attendee(int eventId, int userId) {
        this.eventId = eventId;
        this.userId = userId;
    }
    @Override
    public String toString() {
        return "Attendee{" +
                "uid=" + uid +
                ", eventId=" + eventId +
                ", userId=" + userId +
                '}';
    }
}
