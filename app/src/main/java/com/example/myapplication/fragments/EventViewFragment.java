package com.example.myapplication.fragments;

import android.annotation.SuppressLint;

import com.example.myapplication.Comment;
import com.example.myapplication.CommentAdapter;
import com.example.myapplication.EventCard;
import com.example.myapplication.EventCardAdapter;
import com.example.myapplication.database.dao.RatingDao;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.Rating;
import com.example.myapplication.database.entities.User;
import com.google.android.material.snackbar.Snackbar;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.Constants;
import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.AttendeeDao;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.entities.Attendee;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.Event;
import com.example.myapplication.database.dao.WatchlistDao;
import com.example.myapplication.database.entities.Watchlist;
import com.example.myapplication.database.dao.AttendeeDao;
import com.example.myapplication.database.entities.Attendee;
import com.google.android.material.textfield.TextInputEditText;
import com.example.myapplication.database.entities.User;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.List;

import java.util.function.Function;

public class EventViewFragment extends Fragment {

    private static final String TAG = "EventViewFragment";

    private TextView eventName;

    private RecyclerView commentsView;
    private ArrayList<Comment> commentList;
    private CommentAdapter adapter;
    private TextView eventTimeDateAndVenue;

    private TextView eventDetails;

    private TextView eventCategory;

    private Event event;

    private ImageView imageView;
    private TextView userAttending_btn;

    private Button mDeleteEventButton;

    private Button mEditEventButton;

    private Button addToWatchlistButton;
    private int eventID;
    private Button imAttendingButton;
    private Button submitCommentButton;
    private TextInputEditText commentTextField;
    private int num_of_users;
    private TextView author_name;
    //private Button userAttending_btn;

    Button backButton;

    @SuppressLint("MissingInflatedId")
    private Button promotionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_event_view, container, false);

        this.eventName = view.findViewById(R.id.event_title);
        this.eventTimeDateAndVenue = view.findViewById(R.id.event_date_time_venue);
        this.eventDetails = view.findViewById(R.id.event_details);
        this.eventCategory = view.findViewById(R.id.event_view_category);
        this.imageView = view.findViewById(R.id.event_image);
        this.promotionButton = view.findViewById(R.id.promotionbutton);

        this.addToWatchlistButton = view.findViewById(R.id.add_to_watchlist_button);
        this.imAttendingButton = view.findViewById(R.id.im_attending_button);
        this.submitCommentButton = view.findViewById(R.id.submit_comment);
        this.userAttending_btn = view.findViewById(R.id.user_attending_btn);
        this.commentsView = view.findViewById(R.id.recycler_view_comments);
        this.commentTextField = view.findViewById(R.id.comment_text);
        this.commentList = new ArrayList<>();
        this.commentsView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.adapter = new CommentAdapter(view.getContext(), commentList);
        this.commentsView.setAdapter(adapter);
        this.submitCommentButton.setOnClickListener(this::submitComment);
        this.mDeleteEventButton = view.findViewById(R.id.event_delete_activity_button);
        this.mEditEventButton = view.findViewById(R.id.event_edit_activity_button);
        this.author_name = view.findViewById(R.id.owner_name);
        this.backButton = view.findViewById(R.id.back);


        Bundle bundle = this.getArguments();
        assert(bundle != null);
        int eventId = bundle.getInt(Constants.EVENT_ID, -1);

        if (eventId == -1) {
            // cannot happen
            throw new RuntimeException(Constants.EVENT_ID + " not passed from parent to event view activity");
        }
        this.eventID = eventId;

        try{
            listComments(eventId);

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }



        Future<Event> currentEventFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            return eventDao.getById(eventId);
                        }));

        try {
            this.event = currentEventFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());

        //getting the list of usernames who attends the event
        Future<ArrayList<String>> usernamesFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                appDatabase -> {
                    AttendeeDao attendeeDao = appDatabase.attendeeDao();
                    return (ArrayList<String>) attendeeDao.getUsernamesByEventId(eventId);
                }
        ));

        ArrayList<String> usernames;
        try {
            usernames = usernamesFuture.get();
            Log.e(TAG, "Got Usernames: " + usernames);
            this.num_of_users = usernames.size();
            String btn_text = this.num_of_users + "User(s) Attending";
            userAttending_btn.setText(btn_text);
          userAttending_btn.setOnClickListener(v -> {
                dialogFragment dialogFragment = new dialogFragment(usernames);
                dialogFragment.show(requireActivity().getSupportFragmentManager(), "users_dialog");
                });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        checkEventInWatchlistAndSetButton();
        checkUserInAttendeeListAndSetButton();

        int Loggedid = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);
        int Creatorid = event.getownerUserId();
        this.eventName.setText(this.event.getName());
        this.eventTimeDateAndVenue.setText(getEventTimeDateAndVenue(this.event));
        this.eventDetails.setText(this.event.getDescription());
        this.eventCategory.setText(this.event.getCategory());
        if (this.event.getImage() != null) {
            this.imageView.setImageBitmap(getBitMapFromByteArray(this.event.getImage()));
        }

        try {
            this.setButtonVisibility();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.mDeleteEventButton.setOnClickListener(v -> {
            try {
                deleteEvent();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        this.mEditEventButton.setOnClickListener(v -> {
            try {
                editEvent();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


        int ownerId = this.eventID;

        Future<User> ownerFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {
                            UserDao userDao = appDatabase.userDao();
                            return userDao.getUserById(ownerId);
                        }
                ));
        try {
            User owner = ownerFuture.get();
            String ownerUsername = owner.username; // replace with your method to get username
            // Finally, set the owner's username to the TextView
            this.author_name.setText(ownerUsername);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        addToWatchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String buttonText = addToWatchlistButton.getText().toString();
                if (buttonText.equals("Add to Watchlist")) {
                    addToWatchlist();
                } else {
                    removeFromWatchlist();
                }
            }
        });

        imAttendingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String buttonText = imAttendingButton.getText().toString();
                if (buttonText.equals("I'm Attending")) {
                    addToAttendee();
                } else {
                    removeFromAttendee();
                }
            }
        });

        if (Loggedid == Creatorid) {
            promotionButton.setVisibility(View.VISIBLE);
        } else {
            promotionButton.setVisibility(View.GONE);

        }

        promotionButton.setOnClickListener(
                v -> {
                    GlobalExecutorService.getInstance().submit(
                            new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                                    appDatabase -> {
                                        EventDao eventDao = appDatabase.eventDao();
                                         eventDao.setPromoted(eventId, true);
                                        return null;
                                    }));
                    Toast.makeText(getActivity(), "Event successfully promoted!", Toast.LENGTH_SHORT).show();
                }
        );

        return view;
    }


    private void checkEventInWatchlistAndSetButton() {
        Future<Watchlist> watchlistFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {
                            WatchlistDao watchlistDao = appDatabase.watchlistDao();
                            return watchlistDao.getWatchlistByUserIdAndEventId(
                                    (GlobalState.getInt(GlobalState.keys.USER_ID.name(), 0)),
                                    this.eventID
                            );
                        }));

        Watchlist checkWatchlist;
        try {
            checkWatchlist = watchlistFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (checkWatchlist != null) {
            addToWatchlistButton.setText("Remove from Watchlist");
        } else {
            addToWatchlistButton.setText("Add to Watchlist");
        }
    }

    private Bitmap getBitMapFromByteArray(byte[] imageBytes) {
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
        if (bitmap == null) {
            Log.w(TAG, "unable to decode image for event " + this.event.getName());
            return null;
        }
        return bitmap;
    }

    private String getEventTimeDateAndVenue(Event event) {
        Date date = Utilities.getDateFromString(event.getDate());
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return "at " + event.getLocation() + " on " + localDateTime.getDayOfMonth() + "/" +
                localDateTime.getMonthValue() + "/" + localDateTime.getYear() + " from " +
                getHourAndMinute(localDateTime) + " onwards";
    }

    @NonNull
    private String getHourAndMinute(LocalDateTime date) {
        String hour = String.format(Locale.ENGLISH, "%d", date.getHour());
        String minute = String.format(Locale.ENGLISH, "%d", date.getMinute());
        if (date.getHour() < 10) {
            hour = String.format(Locale.ENGLISH, "%s%d", "0", date.getHour());
        }
        if (date.getMinute() == 0) {
            minute = String.format(Locale.ENGLISH, "%s%d", "0", date.getMinute());
        }
        return hour + ":" + minute;
    }

    private void setButtonVisibility() throws InterruptedException, ExecutionException {
        int currentUserId = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);
        if (currentUserId == -1) {
            // not found
            Log.e(TAG, "unable to find the current user", null);
            return;
        }
        Future<List<Event>> eventCreationFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(this.getContext()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            if(eventDao.getById(event.uid).ownerUserId != currentUserId) {
                                mDeleteEventButton.setVisibility(View.GONE);
                                mEditEventButton.setVisibility(View.GONE);
                            }
                            else {
                                mDeleteEventButton.setVisibility(View.VISIBLE);
                                mEditEventButton.setVisibility(View.VISIBLE);
                            }
                            return null;
                        }));
        eventCreationFuture.get();
    }

    private void deleteEvent() throws InterruptedException, ExecutionException {
        Future<List<Event>> eventCreationFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(this.getContext()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            eventDao.delete(this.event);
                            return null;
                        }));

        List<Event> events = eventCreationFuture.get();
        Toast.makeText(getActivity(), "Event deleted successfully!", Toast.LENGTH_SHORT).show();
        changeFragment(new ManageEventsFragment());
    }

    private void editEvent() throws InterruptedException, ExecutionException {
        changeFragment(new EventEditFragment(this.event.uid));
    }

    private void checkUserInAttendeeListAndSetButton() {
        Log.e(TAG, "Checking Attendence\n");
        Future<ArrayList<String>> usernamesFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {

                            AttendeeDao attendeeDao = appDatabase.attendeeDao();
                            return (ArrayList<String>) attendeeDao.getUsernamesByEventId(this.eventID);
                        }
                ));

        ArrayList<String> usernames;
        try {
            usernames = usernamesFuture.get();
            if( usernames.contains(GlobalState.getLoggedInUser()) ) imAttendingButton.setText("Cancel Attendance");
            else {
                Log.e(TAG, "No Attendance for this user\n");
                imAttendingButton.setText("I'm Attending");
            }
        }
         catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void addToAttendee() {
        Integer retvalAttendee = 0;
        Future<Integer> attendeeFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {
                            AttendeeDao attendeeDao = appDatabase.attendeeDao();
                            Attendee attendee = new Attendee(
                                    this.event.uid, GlobalState.getInt(GlobalState.keys.USER_ID.name(), 0)
                            );
                            try {
                                Log.e(TAG, "Adding Attendee to DB: " + attendee);
                                attendeeDao.insert(attendee);
                            } catch (SQLiteConstraintException e) {
                                Log.e(TAG, "Trying to add duplicate Attendee Entries\n");
                                return -1;
                            }
                            return 0;
                        }));
        try {
            retvalAttendee = attendeeFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (retvalAttendee == 0) {
            getActivity().runOnUiThread(() -> {

                Snackbar.make(requireView(), "You have successfully marked your attendance", Snackbar.LENGTH_SHORT).show();
                checkUserInAttendeeListAndSetButton();
                imAttendingButton.setText("Cancel Attendance");
            });
        } else {
            getActivity().runOnUiThread(() -> {
                Snackbar.make(requireView(), "You have already marked your attendance", Snackbar.LENGTH_SHORT).show();
            });
        }
    }

    private void removeFromAttendee() {
        final Integer[] retvalAttendee = {0};

        Future<Integer> attendeeFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {
                            AttendeeDao attendeeDao = appDatabase.attendeeDao();
                            Attendee attendee = attendeeDao.getByEventIdAndUserId(
                                    (GlobalState.getInt(GlobalState.keys.USER_ID.name(), 0)),
                                    this.event.uid
                            );
                            if (attendee == null) {
                                Log.e(TAG, "No matching Attendee entry found in DB to remove");
                                return -1;
                            }

                            try {
                                Log.e(TAG, "Removing Attendee from DB: " + attendee);
                                attendeeDao.delete(attendee);
                                return 0;
                            } catch (SQLiteConstraintException e) {
                                Log.e(TAG, "Error removing attendee from database: " + e.getMessage());
                                return -1;
                            }

                        }));

        try {
            retvalAttendee[0] = attendeeFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        getActivity().runOnUiThread(() -> {
            if (retvalAttendee[0] == 0) {
                imAttendingButton.setText("I'm Attending");
                Snackbar.make(requireView(), "You have successfully canceled your attendance", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(requireView(), "Error canceling your attendance", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void addToWatchlist() {
        Integer retval = 0;
        Future<Integer> watchlistFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {
                            WatchlistDao watchlistDao = appDatabase.watchlistDao();
                            Watchlist watchlist = new Watchlist(
                                    (GlobalState.getInt(GlobalState.keys.USER_ID.name(), 0)),
                                    this.event.uid
                            );
                            try {
                                Log.e(TAG, "Adding Watchlist to DB: "+ watchlist);
                                watchlistDao.addToWatchlist(watchlist);
                            }catch (SQLiteConstraintException e) {
                                Log.e(TAG, "Trying to add duplicate Watchlist Entries\n");
                                return -1;
                            }
                            return  0;
                        }));
        try {
            retval = watchlistFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if( retval == 0)
        {
            getActivity().runOnUiThread(() -> {
                Snackbar.make(requireView(), "Event added to watchlist", Snackbar.LENGTH_SHORT).show();
                addToWatchlistButton.setText("Remove from watchlist");
            });
        }
        else {
            getActivity().runOnUiThread(() -> {
                Snackbar.make(requireView(), "Event already added to watchlist", Snackbar.LENGTH_SHORT).show();
            });
        }
    }

    private void removeFromWatchlist( ) {
        final Integer[] retval = {0};
        Future<Integer> watchlistFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                        appDatabase -> {
                            WatchlistDao watchlistDao = appDatabase.watchlistDao();
                            Watchlist watchlist = watchlistDao.getWatchlistByUserIdAndEventId(
                                    (GlobalState.getInt(GlobalState.keys.USER_ID.name(), 0)),
                                    this.event.uid
                            );
                            if (watchlist == null) {
                                Log.e(TAG, "No matching Watchlist entry found in DB to remove");
                                return -1;
                            }
                            try {
                                Log.e(TAG, "Removing Watchlist to DB: "+ watchlist);
                                watchlistDao.removeFromWatchlist(watchlist);
                                return 0;
                            } catch (SQLiteConstraintException e) {
                                Log.e(TAG, "Error removing event from watchlist: " + e.getMessage());
                                return -1;
                            }
                        }));

        try {
            retval[0] = watchlistFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        getActivity().runOnUiThread(() -> {
            if (retval[0] == 0) {
                addToWatchlistButton.setText("Add to Watchlist");
                Snackbar.make(requireView(), "Event removed from watchlist", Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(requireView(), "Error removing event from watchlist", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    public void listComments(int eventID) throws ExecutionException, InterruptedException {

        Future<List<Rating>> eventCommentsFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            RatingDao ratingDao = appDatabase.ratingDao();
                            return ratingDao.getByEventId(eventID);
                        }));

        List<Rating> eventRatings = eventCommentsFuture.get();
        if(eventRatings == null)
        {
            Log.e(TAG, "No Comments found for this event\n");
            return;
        }
        for(Rating rating : eventRatings) {

            Future<User> userFuture = GlobalExecutorService.getInstance().submit(
                    new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                            appDatabase -> {
                                UserDao userDao = appDatabase.userDao();
                                return userDao.loadById(rating.userId);
                            }));
            User user = userFuture.get();
            if(user == null) throw new RuntimeException("User was null for a Comment");

            Comment newComment = new Comment(rating.userId, rating.eventId,
                    rating.uid,user.username, rating.date, rating.rating, rating.comment);

            commentList.add(newComment);
        }
        adapter.notifyDataSetChanged();
    }

    private void submitComment(View view)
    {
        Future<Integer> commentFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                         String comment = this.commentTextField.getText().toString();
                            if (comment.isEmpty()) return  -1;

                            Rating rating = new Rating(this.eventID,GlobalState.getInt(GlobalState.keys.USER_ID.name(), 0), this.commentTextField.getText().toString(), 0);
                        Log.e(TAG, "Submitting Rating: " + rating);
                        RatingDao ratingDao = appDatabase.ratingDao();
                        ratingDao.insert(rating);
                    return 0;
                        }));
        try {
            int retval = commentFuture.get();
            if( retval == -1) Log.e(TAG, "Comment was empty");
        } catch (ExecutionException | InterruptedException e) {
        throw new RuntimeException(e);
    }

    }




    private void changeFragment(Fragment fragment){
        Bundle bundle = new Bundle();
        bundle.putParcelable("event", event);
        fragment.setArguments(bundle);

        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.commit();
    }


}