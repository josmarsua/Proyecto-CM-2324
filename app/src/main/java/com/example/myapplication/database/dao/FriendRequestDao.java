package com.example.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.versionedparcelable.ParcelField;

import com.example.myapplication.database.entities.FriendRequest;

import java.util.List;

@Dao
public interface FriendRequestDao {

    @Insert
    void insert(FriendRequest event);

    @Update
    void update(FriendRequest event);

    @Delete
    void delete(FriendRequest event);

    @Query("SELECT * from friend_request WHERE sender = :senderId")
    List<FriendRequest> getAllFriendRequestsSentByUser(int senderId);

    @Query("SELECT * from friend_request WHERE receiver = :receiverId")
    List<FriendRequest> getAllFriendRequestsReceivedByUser(int receiverId);

    @Query("SELECT * from friend_request WHERE sender = :senderId AND receiver = :receiverId LIMIT 1")
    FriendRequest getFriendRequestBySenderAndReciever(int senderId, int receiverId);

    @Query("SELECT * from friend_request WHERE receiver = :receiverId AND status = :status")
    List<FriendRequest> getAllFriendRequestsReceivedByUserAndStatus(int receiverId, String status);
}
