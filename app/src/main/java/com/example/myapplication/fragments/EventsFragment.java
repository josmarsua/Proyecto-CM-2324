package com.example.myapplication.fragments;
import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.myapplication.Constants;
import com.example.myapplication.EventCard;
import com.example.myapplication.EventCardAdapter;
import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.R;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.entities.Event;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EventsFragment extends Fragment {
    private RecyclerView eventsView;
    private ArrayList<EventCard> eventsList;
    private EventCardAdapter adapter;
    private BottomNavigationView menu;
    private String selectedCategory = "All events";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layoutView = inflater.inflate(R.layout.activity_menu, null);
        menu = layoutView.findViewById(R.id.bottomNavigationView);
        menu.setSelectedItemId(R.id.home);
        menu.performClick();

        View view = inflater.inflate(R.layout.fragment_events_layout, null);
        eventsView = view.findViewById(R.id.recycler_view_events);
        super.onResume();



        Spinner spinner = view.findViewById(R.id.spinner4);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(view.getContext(),
                R.array.categories_array, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = parent.getItemAtPosition(position).toString();
                applyCategoryFilter(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "All events";
                //applyCategoryFilter(selectedCategory);
            }
        });


        eventsList = new ArrayList<>();
        eventsView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapter = new EventCardAdapter(view.getContext(), eventsList);
        eventsView.setAdapter(adapter);

        try {
            listEvents();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void listEvents() throws ExecutionException, InterruptedException {
        Log.e("TAG", "List Events invoked.\n");
        Future<List<Event>> publicEventsFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            return eventDao.getByVisibility(Constants.EVENT_VISIBILITY_PUBLIC);
                        }));

        List<Event> publicEvents = publicEventsFuture.get();
        List<EventCard> promotedEvents = new ArrayList<>();
        List<EventCard> notPromotedEvents = new ArrayList<>();

        for(Event publicEvent : publicEvents) {
                    EventCard newCard = new EventCard(publicEvent.name, publicEvent.uid,publicEvent.image,
                            Utilities.getDateFromString(publicEvent.date), publicEvent.location,
                            publicEvent.category, publicEvent.promoted);

            if(publicEvent.promoted) {
                promotedEvents.add(newCard);
            } else {
                notPromotedEvents.add(newCard);
            }
        }

        eventsList.addAll(promotedEvents);
        eventsList.addAll(notPromotedEvents);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void applyCategoryFilter(String selectedCategory) {
        List<EventCard> filteredEvents = new ArrayList<>();

        if (selectedCategory.equals("All events")) {
            // No category selected, display all events
            filteredEvents.addAll(eventsList);
        } else {
            // Filter events based on the selected category
            for (EventCard eventCard : eventsList) {
                if (eventCard.getCategory().equals(selectedCategory)) {
                    filteredEvents.add(eventCard);
                }
            }
        }
        // Update the adapter with filtered events
        adapter.setEventList(filteredEvents);
    }


}