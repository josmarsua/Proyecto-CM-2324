package com.example.myapplication.database;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.database.dao.AttendeeDao;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.dao.FriendRequestDao;
import com.example.myapplication.database.dao.InviteeDao;
import com.example.myapplication.database.dao.RatingDao;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.dao.WatchlistDao;
import com.example.myapplication.database.entities.Attendee;
import com.example.myapplication.database.entities.Event;
import com.example.myapplication.database.entities.FriendRequest;
import com.example.myapplication.database.entities.Invitee;
import com.example.myapplication.database.entities.Rating;
import com.example.myapplication.database.entities.User;
import com.example.myapplication.database.entities.Watchlist;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

@Database(entities = {User.class, Event.class, Invitee.class, Attendee.class, Rating.class,
        FriendRequest.class, Watchlist.class}, version = 4, exportSchema = true)
@TypeConverters({DBDataConverters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "eventBuddyDB")
                    .fallbackToDestructiveMigration()
                    .createFromAsset("database/dbv5.db")
                    .build();
            instance.confirmPrepop();
        }
        return instance;
    }
    public abstract UserDao userDao();
    public abstract EventDao eventDao();
    public abstract AttendeeDao attendeeDao();
    public abstract InviteeDao inviteeDao();
    public abstract RatingDao ratingDao();
    public abstract WatchlistDao watchlistDao();
    public abstract FriendRequestDao friendRequestDao();

    private void confirmPrepop()
    {
        // THIS METHOD IS FOR DEBUGGING AND WILL BE REPLACED BY TESTCASES IN THE FUTURE
        Future<Integer> registerFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(instance,
                        new Function<AppDatabase, Integer>() {
                            @Override
                            public Integer apply(AppDatabase appDatabase) {
                                UserDao userDao = appDatabase.userDao();
                                //TODO: FIX THIS, FNAME AND LNAME NOT BEING ADDED TO DB
                                User checkUser = userDao.findByUsername("johndoe");
                                Log.e("DATABASE", checkUser.toString());
                                if( checkUser == null)
                                {
                                    Log.e("DATABASE", "Registration Failed");
                                    return -1;
                                }
                                return 0;
                            }
                        }));

        try {
            Integer retval = registerFuture.get();
            if(retval == 0) {
                Log.e("DATABASE", "PREPOP SUCC");

            }
            else {
                Log.e("DATABASE", "PREPOP FAILED");
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            Log.e("DATABASE", "PREPOP FAILED");
        }
    }

}
