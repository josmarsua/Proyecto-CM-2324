package com.example.myapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.fragment.app.FragmentActivity;

import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.User;
import com.example.myapplication.fragments.AccountFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class Utilities {

    public static Date getDateFromString(String date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            return formatter.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
    public static Bitmap getBitMapFromByteArray(byte[] imageBytes) {
        if( imageBytes == null)
        {
            Log.e(TAG, "Bytestring provided was null");
            return null;
        }
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes.length);
        if (bitmap == null) {
            Log.w(TAG, "unable to decode image.");
            return null;
        }
        return bitmap;
    }

    public static boolean isUserNameAlreadyTaken(FragmentActivity fragmentActivity, String newUsername) {

        Future<User> getUserFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(fragmentActivity),
                        new Function<AppDatabase, User>() {
                            @Override
                            public User apply(AppDatabase appDatabase) {
                                UserDao userDao = appDatabase.userDao();
                                User currentUser = userDao.findByUsername(newUsername);
                                if (currentUser == null) {
                                    // cannot be
                                    throw new RuntimeException("Unable to get current logged in user!");
                                }
                                return currentUser;
                            }
                        }));
        try {
            User user = getUserFuture.get();
            if (user != null) {
                return true;
            }
            // already a user exists with the new user name
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void changeFragment(Fragment from, Fragment to){
        FragmentManager fragmentManager = from.getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, to);
        fragmentTransaction.commit();
    }
}
