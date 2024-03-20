package com.example.myapplication.fragments;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.EventCard;
import com.example.myapplication.EventCardAdapter;
import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.AttendeeDao;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.entities.Attendee;
import com.example.myapplication.database.entities.Event;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class ManageEventsFragment extends Fragment {
    private ArrayList<EventCard> eventsList;
    private ArrayList<EventCard> myEventsList;
    private ArrayList<EventCard> watchList;
    private ArrayList<EventCard> attendingList;
    private EventCardAdapter adapter0;
    private EventCardAdapter adapter1;
    private EventCardAdapter adapter2;
    private EventCardAdapter adapter3;
    private static final String TAG = "ManageEventsFragment";
    private RecyclerView eventsView;
    private BottomNavigationView menu;
    private Spinner visibilitySelector;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.activity_menu, null);
        menu = layoutView.findViewById(R.id.bottomNavigationView);
        menu.setSelectedItemId(R.id.home);
        menu.performClick();

        View rootView = inflater.inflate(R.layout.fragment_manage_events,null);
        eventsView = rootView.findViewById(R.id.management_recycler_view);
        super.onResume();

        eventsView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));

        eventsList = new ArrayList<>();
        adapter0 = new EventCardAdapter(rootView.getContext(), eventsList);

        myEventsList = new ArrayList<>();
        adapter1 = new EventCardAdapter(rootView.getContext(), myEventsList);

        watchList = new ArrayList<>();
        adapter2 = new EventCardAdapter(rootView.getContext(), watchList);

        attendingList = new ArrayList<>();
        adapter3 = new EventCardAdapter(rootView.getContext(), attendingList);

        this.visibilitySelector = rootView.findViewById(R.id.visibility_levels);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.visibility_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.visibilitySelector.setAdapter(adapter);
        this.visibilitySelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view,
                                       int position, long id) {
                Object item = adapterView.getItemAtPosition(position);
                if (position == 0) {
                    eventsView.setAdapter(adapter0);
                    try {
                        listEvents(eventsList, adapter0, position);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if (position == 1) {
                    eventsView.setAdapter(adapter1);
                    try {
                        listEvents(myEventsList, adapter1, position);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if (position == 2) {
                    eventsView.setAdapter(adapter2);
                    try {
                        listEvents(watchList, adapter2, position);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                else if (position == 3) {
                    eventsView.setAdapter(adapter3);
                    try {
                        listEvents(attendingList, adapter3, position);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        Button createEventButton = rootView.findViewById(R.id.event_create_activity_button2);
        createEventButton.setOnClickListener(v -> {
            onCreateEventButtonClick();
        });

        return rootView;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void listEvents(ArrayList<EventCard> events, EventCardAdapter adapterList, int position) throws ExecutionException, InterruptedException {
        int currentUserId = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);
        if (currentUserId == -1) {
            // not found
            Log.e(TAG, "unable to find the current user", null);
            return;
        }

        events.clear();

        Future<List<Event>> publicEventsFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            AttendeeDao attendeeDao = appDatabase.attendeeDao();
                            List<Event> visibleEvents = new ArrayList<>();
                            if (position == 1)
                                return eventDao.getByOwnerId(currentUserId);
                            else if (position == 2)
                                return eventDao.getWatchlistEvents(currentUserId);
                            else if (position == 3){
                                // TODO: Needs to be changed for the List of Attending Events!
                                for (Attendee attendee : attendeeDao.getAttendeesByUserId(currentUserId))
                                    visibleEvents.add(eventDao.getById(attendee.eventId));
                                return visibleEvents;
                            }
                            else {
                                List<Event> merge = new ArrayList<>();
                                merge.addAll(eventDao.getByOwnerId(currentUserId));
                                merge.addAll(eventDao.getWatchlistEvents(currentUserId));
                                // TODO: Needs to be changed for the List of Attending Events!
                                for (Attendee attendee : attendeeDao.getAttendeesByUserId(currentUserId))
                                    visibleEvents.add(eventDao.getById(attendee.eventId));
                                merge.addAll(visibleEvents);

                                return merge.stream().distinct().collect(Collectors.toList());
                            }
                        }));

        List<Event> visibleEvents = publicEventsFuture.get();

        for (Event visibleEvent : visibleEvents) {
                EventCard newCard = new EventCard(visibleEvent.name, visibleEvent.uid,
                        visibleEvent.image, Utilities.getDateFromString(visibleEvent.date), visibleEvent.location, visibleEvent.category, visibleEvent.promoted);
                events.add(newCard);
            }

        adapterList.notifyDataSetChanged();
    }

    public  void onCreateEventButtonClick() {
        changeFragment(new EventCreationFragment());
    }

    private void changeFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.commit();
    }
}
