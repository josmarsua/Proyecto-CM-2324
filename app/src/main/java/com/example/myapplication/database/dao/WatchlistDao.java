package com.example.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myapplication.database.entities.Watchlist;


@Dao
public interface WatchlistDao {

    @Insert(onConflict = OnConflictStrategy.FAIL)
    void addToWatchlist(Watchlist watchlist);

    @Query("SELECT * FROM Watchlist WHERE user_id = :userId AND event_id = :eventId LIMIT 1")
    Watchlist getWatchlistByUserIdAndEventId(int userId, int eventId);

    @Delete
    void removeFromWatchlist(Watchlist watchlist);


}
