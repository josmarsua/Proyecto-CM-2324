package com.example.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"event_id", "user_id"},
        unique = true)})

public class Watchlist {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "event_id")
    public int eventId;

    public Watchlist(int userId, int eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "Watchlist{" +
                "uid=" + uid +
                ", userId=" + userId +
                ", eventId=" + eventId +
                '}';
    }
}
