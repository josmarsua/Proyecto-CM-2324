package com.example.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.database.entities.Invitee;

import java.util.List;

@Dao
public interface InviteeDao {

    @Insert
    void insert(Invitee invitee);

    @Update
    void update(Invitee invitee);

    @Delete
    void delete(Invitee invitee);

    @Query("SELECT * FROM invitee WHERE uid = :id LIMIT 1")
    Invitee get(int id);

    @Query("SELECT * FROM invitee WHERE event_id = :eventId ")
    List<Invitee> getByEventId(int eventId);

    @Query("DELETE FROM invitee WHERE event_id = :eventId ")
    void deleteByEventId(int eventId);
}
