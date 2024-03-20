package com.example.myapplication.fragments;

import androidx.fragment.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.Constants;
import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.Event;
import com.example.myapplication.database.entities.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class PromotionFragment extends Fragment {
    private Event event;

    private Button promoteButton;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("event")) {
            event = arguments.getParcelable("event");
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_promotion, container, false);

        promoteButton = rootView.findViewById(R.id.promoteButton);
        promoteButton.setOnClickListener(view -> {
            event.setPromoted(true);

        });

        return rootView;
    }



}
