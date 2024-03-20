package com.example.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.database.entities.Rating;

import java.util.List;

@Dao
public interface RatingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Rating rating);

    @Update
    void update(Rating rating);

    @Delete
    void delete(Rating rating);

    @Query("SELECT * FROM rating WHERE uid = :id LIMIT 1")
    Rating get(int id);

    @Query("SELECT * FROM rating WHERE event_id = :eventId ")
    List<Rating> getByEventId(int eventId);

    @Query("DELETE FROM rating WHERE event_id = :eventId ")
    void deleteByEventId(int eventId);
}
