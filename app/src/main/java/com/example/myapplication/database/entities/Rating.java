package com.example.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(indices = {@Index(value = {"event_id", "user_id"},
        unique = true)})
public class Rating {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "event_id")
    public int eventId;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "comment")
    public String comment;

    @ColumnInfo(name = "rating")
    public int rating;

    @ColumnInfo(name="date")
    public String date;

    public Rating(int eventId, int userId, String comment, int rating) {
        this.eventId = eventId;
        this.userId = userId;
        this.comment = comment;
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "uid=" + uid +
                ", eventId=" + eventId +
                ", userId=" + userId +
                ", comment='" + comment + '\'' +
                ", rating=" + rating +
                ", date='" + date + '\'' +
                '}';
    }
}
