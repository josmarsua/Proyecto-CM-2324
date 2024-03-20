package com.example.myapplication.fragments;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.BackgroundColorSpan;
import android.text.style.StyleSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SearchFragment extends Fragment {
    SearchBar searchBar;
    SearchView searchView;
    RecyclerView searchResultsDynamic;
    RecyclerView searchResults;
    List<EventCard> queryResultEventsList;
    EventCardAdapter adapterDynamicResults;
    EventCardAdapter adapterResults;
    TextWatcher textWatcher;
    String currentName;
    String currentLocation;
    String currentUserName;
    TextView textView;
    TextView noResults;
    TextView searchByUsername;
    TextView searchByLocation;
    boolean initState = true;

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_search, container, false);

        searchBar = view.findViewById(R.id.search_bar);
        searchView = view.findViewById(R.id.search_view);

        // ==> instructions + no results view
        textView = view.findViewById(R.id.no_results);
        textView.setText("Here you can search for events\n with any of these keywords:\n");

        noResults = view.findViewById(R.id.see_no_evil);
        noResults.setText("1. search by the event name");

        searchByLocation = view.findViewById(R.id.see_no_evil2);
        SpannableString searchLocation = new SpannableString("2. in: search by location");
        visualiseKeywords(searchLocation, 3, Constants.LOCATION_SEARCH.length());
        searchByLocation.setText(searchLocation);

        searchByUsername = view.findViewById(R.id.see_no_evil3);
        SpannableString searchUsername = new SpannableString("3. by: search by username");
        visualiseKeywords(searchUsername, 3, Constants.USERNAME_SEARCH.length());
        searchByUsername.setText(searchUsername);


        // ==> set up the recycler views
        searchResultsDynamic = view.findViewById(R.id.recycler_view);
        searchResults = view.findViewById(R.id.recycler_view_queried);

        queryResultEventsList = new ArrayList<>();
        searchResultsDynamic.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapterDynamicResults = new EventCardAdapter(view.getContext(), queryResultEventsList);
        searchResultsDynamic.setAdapter(adapterDynamicResults);

        searchResults.setLayoutManager(new LinearLayoutManager(view.getContext()));
        adapterResults = new EventCardAdapter(view.getContext(), queryResultEventsList);
        searchResults.setAdapter(adapterResults);

        searchView.getEditText().setOnEditorActionListener(
                (v, actionId, event) -> {
                    searchBar.setText(searchView.getText());
                    searchView.hide();

                    adapterResults.notifyDataSetChanged();
                    adapterDynamicResults.notifyDataSetChanged();

                    searchByLocation.setVisibility(View.GONE);
                    searchByUsername.setVisibility(View.GONE);

                    if(queryResultEventsList.isEmpty() &&
                      !searchView.getText().toString().isEmpty()) {
                        noResults();
                        return false;
                    } else {
                        textView.setVisibility(View.GONE);
                        noResults.setVisibility(View.GONE);
                    }

                    if(Objects.requireNonNull(searchView.getText()).toString().isEmpty()) {
                        init();
                    }

                    return false;
                });

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                try {
                    if(query.isEmpty()) {
                        init();
                    }
                    search(query);
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        searchView.getEditText().addTextChangedListener(textWatcher);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    void search(String searchedText) throws ExecutionException, InterruptedException {

        queryResultEventsList.clear();
        adapterDynamicResults.notifyDataSetChanged();
        adapterResults.notifyDataSetChanged();

        currentName = "";
        currentLocation = "";
        currentUserName = "";

        SpannableString input =  parseInput(searchedText);

        searchView.getEditText().removeTextChangedListener(textWatcher);
        searchView.getEditText().setText(input);
        searchView.getEditText().setSelection(input.length());
        searchView.getEditText().addTextChangedListener(textWatcher);

        if(currentName.isEmpty() && currentLocation.isEmpty() && currentUserName.isEmpty()) {
            adapterDynamicResults.notifyDataSetChanged();
            return;
        }

        query();

        adapterDynamicResults.notifyDataSetChanged();
    }

    void query() throws ExecutionException, InterruptedException {

        // TODO: eventually optimize
        Future<List<Event>> queriedEventsFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            return eventDao.searchQuery("%" + currentName + "%",
                                                       "%" + currentLocation + "%",
                                                      "%" + currentUserName + "%");
                        }));

        List<Event> queriedEvents = queriedEventsFuture.get();

        for(Event event : queriedEvents) {
            byte[] image = event.getImage();
            EventCard newCard = new EventCard(event.name, event.uid, event.image, Utilities.getDateFromString(event.date), event.location, event.category, event.promoted);

            queryResultEventsList.add(newCard);
        }
    }

    SpannableString parseInput(String input) {

        SpannableString inputVisualised = new SpannableString(input);
        int position = 0;

        int indexOfLocation = input.indexOf(Constants.LOCATION_SEARCH);
        int indexOfUserName = input.indexOf(Constants.USERNAME_SEARCH);

        TreeSet<Integer> sortedIndices = new TreeSet<>();
        if(indexOfLocation != -1) sortedIndices.add(indexOfLocation);
        if(indexOfUserName != -1) sortedIndices.add(indexOfUserName);
        sortedIndices.add(input.length());

        while(!sortedIndices.isEmpty()) {
            String substring = input.substring(position, sortedIndices.first());

            // ==> EVENT NAME
            if(!substring.contains(Constants.LOCATION_SEARCH) && !substring.contains(Constants.USERNAME_SEARCH)) {
                currentName = substring.trim();
            }

            // ==> EVENT LOCATION
            if(substring.contains(Constants.LOCATION_SEARCH)) {
                substring = substring.replace(Constants.LOCATION_SEARCH, "").trim();
                currentLocation = substring;
                inputVisualised = visualiseKeywords
                        (inputVisualised, indexOfLocation, Constants.LOCATION_SEARCH.length());
            }

            // ==> EVENT CREATOR USERNAME
            if(substring.contains(Constants.USERNAME_SEARCH)) {
                substring = substring.replace(Constants.USERNAME_SEARCH, "").trim();
                currentUserName = substring;
                inputVisualised = visualiseKeywords
                        (inputVisualised, indexOfUserName, Constants.USERNAME_SEARCH.length());
            }

            position = sortedIndices.first();
            sortedIndices.pollFirst();
        }

        return inputVisualised;
    }

    SpannableString visualiseKeywords(SpannableString input, int index, int len) {

        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        input.setSpan(boldSpan, index, index + len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        BackgroundColorSpan highlightSpan = new BackgroundColorSpan(Color.LTGRAY);
        input.setSpan(highlightSpan, index, index + len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return input;
    }

    void init() {

        searchBar.setText(null);

        searchByLocation.setVisibility(View.VISIBLE);
        searchByUsername.setVisibility(View.VISIBLE);

        textView.setText("Here you can search for events\n with any of these keywords:\n");
        textView.setVisibility(View.VISIBLE);

        noResults.setVisibility(View.VISIBLE);
        noResults.setText("1. search by the event name");
        noResults.setTextSize(18);
    }

    void noResults() {
        textView.setText("No matching results found!");
        textView.setVisibility(View.VISIBLE);

        noResults.setVisibility(View.VISIBLE);
        noResults.setText("\uD83D\uDE14\uD83D\uDE14\uD83D\uDE14️");
        noResults.setTextSize(32);
    }

    public void onResume() {
        super.onResume();
        if(!queryResultEventsList.isEmpty()) {

            searchByLocation.setVisibility(View.GONE);
            searchByUsername.setVisibility(View.GONE);
            textView.setVisibility(View.GONE);
            noResults.setVisibility(View.GONE);

        }
    }
}