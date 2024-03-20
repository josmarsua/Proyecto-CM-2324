package com.example.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.database.entities.Attendee;

import java.util.List;

@Dao
public interface AttendeeDao {

    @Insert
    void insert(Attendee attendee);

    @Update
    void update(Attendee attendee);

    @Delete
    void delete(Attendee attendee);

    @Query("SELECT * FROM attendee WHERE uid = :id LIMIT 1")
    Attendee get(int id);

    @Query("SELECT * FROM attendee WHERE event_id = :eventId ")
    List<Attendee> getByEventId(int eventId);

    @Query("SELECT * FROM attendee WHERE event_id = :eventId AND user_id = :userId LIMIT 1")
    Attendee getByEventIdAndUserId(int userId,int eventId);


    @Query("SELECT COALESCE(username, 'Default Username') " +
            "FROM attendee AS attendees " +
            "INNER JOIN User AS users ON attendees.user_id = users.uid " +
            "WHERE attendees.event_id = :eventId")
    List<String> getUsernamesByEventId(int eventId);
    @Query("DELETE FROM attendee WHERE event_id = :eventId ")
    void deleteByEventId(int eventId);

    @Query("SELECT * FROM attendee WHERE user_id = :userId ")
    List<Attendee> getAttendeesByUserId(int userId);

}
