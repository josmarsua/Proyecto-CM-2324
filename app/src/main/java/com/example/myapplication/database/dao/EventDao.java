package com.example.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.myapplication.database.entities.Event;

import java.util.List;

@Dao
public interface EventDao {

    @Insert
    void insert(Event event);

    @Update
    void update(Event event);

    @Delete
    void delete(Event event);

    @Query("SELECT * FROM event WHERE uid = :id")
    Event getById(int id);

    @Query("SELECT * FROM event WHERE uid IN (:ids)")
    List<Event> getEventsByListOfIds(List<String> ids);

    @Query("SELECT * FROM event WHERE owner_user_id = :id")
    List<Event> getByOwnerId(int id);

    @Query("SELECT owner_user_id FROM event WHERE uid = :eventId LIMIT 1")
    int getownerUserId(int eventId);


    @Query("DELETE FROM event WHERE owner_user_id = :id")
    void deleteByOwnerId(int id);

    @Query("SELECT * FROM event WHERE category = :category")
    List<Event> getByCategory(String category);

    @Query("SELECT * FROM event WHERE visibility = :visibility")
    List<Event> getByVisibility(String visibility);

    @Query("SELECT DISTINCT category FROM event")
    List<String> getCategories();

    @Query("DELETE FROM event")
    void deleteAll();

  @Query("SELECT * FROM event INNER JOIN watchlist ON event.owner_user_id = watchlist.event_id WHERE watchlist.user_id = :userId")
    List<Event> getWatchlistEvents(int userId);
    @Query( "SELECT *" +
            "FROM user AS u " +
            "LEFT JOIN event AS e ON e.owner_user_id = u.uid " +
            "WHERE u.username LIKE :username " +
            "AND e.name LIKE :name " +
            "AND e.location LIKE :location")
    List<Event> searchQuery(String name, String location, String username);

    @Query("UPDATE event SET promoted = :promoted WHERE uid = :id")
    void setPromoted(int id, boolean promoted);
}
