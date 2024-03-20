package com.example.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "friend_request", indices = {@Index(value = {"sender", "receiver"},
        unique = true)})
public class FriendRequest {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "sender")
    public int sender;

    @ColumnInfo(name = "receiver")
    public int receiver;

    @ColumnInfo(name = "status")
    private String status;

    public FriendRequest(int sender, int receiver, String status) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = status;
    }

    public int getUid() {
        return uid;
    }

    public int getSender() {
        return sender;
    }

    public int getReceiver() {
        return receiver;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "uid=" + uid +
                ", sender=" + sender +
                ", receiver=" + receiver +
                ", status='" + status + '\'' +
                '}';
    }
}
