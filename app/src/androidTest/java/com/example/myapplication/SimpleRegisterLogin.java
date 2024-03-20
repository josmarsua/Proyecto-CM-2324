package com.example.myapplication;

import android.content.Context;

import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import android.util.Log;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.User;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SimpleRegisterLogin {
    private UserDao userDao;
    private AppDatabase appDatabase;

    @Before
    public void createDatabase() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        appDatabase = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        userDao = appDatabase.userDao();
    }
    @After
    public void closeDatabase() {
        appDatabase.close();


    }@Test
    public void useAppContext() {
        User user = new User("username", "password", "email");
        Future<Integer> registerFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(appDatabase,
                        new Function<AppDatabase, Integer>() {
                            @Override
                            public Integer apply(AppDatabase appDatabase) {
                                userDao.register(user);
                                return 0;
                            }
                        }));

        try {
            Integer retval = registerFuture.get();
            assertEquals(retval.intValue(), 0);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        Future<User> userFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(appDatabase,
                        new Function<AppDatabase, User>() {
                            @Override
                            public User apply(AppDatabase appDatabase) {
                                UserDao userDao = appDatabase.userDao();
                                return userDao.login(user.username, user.password);
                            }
                        }));

        try {
            User loggedInUser = userFuture.get();
            assertNotNull(loggedInUser);
            System.out.println("Logged in User: ");
            user.toString();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
                    }
    }
}

