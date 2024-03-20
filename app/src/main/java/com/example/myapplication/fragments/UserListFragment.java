package com.example.myapplication.fragments;

import static com.example.myapplication.Constants.ACCEPTED_FRIEND_REQUEST_STATUS;
import static com.example.myapplication.Constants.PENDING_FRIEND_REQUEST_STATUS;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.EventCard;
import com.example.myapplication.EventCardAdapter;
import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.UserListAdaptor;
import com.example.myapplication.UserListDetails;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.dao.FriendRequestDao;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.Event;
import com.example.myapplication.database.entities.FriendRequest;
import com.example.myapplication.database.entities.User;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class UserListFragment extends Fragment {

    SearchBar searchBar;
    SearchView searchView;
    private static final String TAG = "UserListFragment";
    private RecyclerView recyclerUserListView;
    private RecyclerView recyclerUserListViewDynamic;
    private UserListAdaptor userListAdaptor;
    List<UserListDetails> userDetailsList;
    private UserListAdaptor userListAdaptorDynamic;
    List<UserListDetails> userDetailsListDynamic;
    TextWatcher textWatcher;
    TextView noResultsEmoji;
    TextView noResults;

    public UserListFragment() {

        userDetailsList = new ArrayList<>();
        userDetailsListDynamic = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        return view;
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.noResults = view.findViewById(R.id.no_results);
        this.noResultsEmoji = view.findViewById(R.id.see_no_evil);
        init();

        this.recyclerUserListView = view.findViewById(R.id.recyclerview_user_list);
        this.recyclerUserListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.userListAdaptor = new UserListAdaptor(getContext(), this.userDetailsList);
        this.recyclerUserListView.setAdapter(this.userListAdaptor);

        this.recyclerUserListViewDynamic = view.findViewById(R.id.recycler_view);
        this.recyclerUserListViewDynamic.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.userListAdaptorDynamic = new UserListAdaptor(getContext(), this.userDetailsListDynamic);
        this.recyclerUserListViewDynamic.setAdapter(this.userListAdaptorDynamic);

        this.searchBar = view.findViewById(R.id.search_bar);
        this.searchView = view.findViewById(R.id.search_view);

        searchView.getEditText().setOnEditorActionListener(
                (v, actionId, event) -> {
                    searchBar.setText(searchView.getText());
                    searchView.hide();

                    // ==> query with no results
                    if(userDetailsList.isEmpty() &&
                       !searchView.getText().toString().isEmpty()) {
                       noResults();
                    } else init();

                    // ==> display recommendations
                    if(searchView.getText().toString().isEmpty()) {
                        try {
                            userDetailsListDynamic.clear();
                            userListAdaptorDynamic.notifyDataSetChanged();

                            loadUsersRecommendations();
                        } catch (ExecutionException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    return false;
                });

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                try {
                    if(query.isEmpty()) {
                        searchBar.setText(null);
                        noResultsEmoji.setVisibility(View.GONE);
                        noResults.setVisibility(View.GONE);

                        userDetailsListDynamic.clear();
                        userListAdaptorDynamic.notifyDataSetChanged();

                        loadUsersRecommendations();
                        return;
                    }

                    userDetailsList.clear();
                    userDetailsListDynamic.clear();
                    search(query);
                    userListAdaptor.notifyDataSetChanged();
                    userListAdaptorDynamic.notifyDataSetChanged();
                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        searchView.getEditText().addTextChangedListener(textWatcher);

        try {
            loadUsersRecommendations();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void search(String query) throws ExecutionException, InterruptedException {

        Future<List<User>> queriedUsersFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            UserDao userDao = appDatabase.userDao();
                            return userDao.searchQuery("%" + query + "%");
                        }));

        List<User> queriedUsers = queriedUsersFuture.get();

        for(User user : queriedUsers) {
            UserListDetails newCard = new UserListDetails(user.uid, user.firstName,
                    user.lastName, user.username, user.email, user.image);

            userDetailsList.add(newCard);
        }

        userDetailsListDynamic.addAll(userDetailsList);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadUsersRecommendations() throws ExecutionException, InterruptedException {

        if(userDetailsList.size() == 7) return;

        userDetailsList.clear();

        int currentUserId = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);

        if (currentUserId == -1) {
            // not found
            String errorMsg = "unable to get the current user id";
            Log.e(TAG, errorMsg, null);
            throw new RuntimeException(errorMsg);
        }

        // ==> load friends
        Future<List<FriendRequest>> friendsFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            FriendRequestDao friendRequestDao = appDatabase.friendRequestDao();
                            return friendRequestDao.
                                    getAllFriendRequestsReceivedByUserAndStatus(currentUserId, ACCEPTED_FRIEND_REQUEST_STATUS);
                        }));

        List<FriendRequest> friends = friendsFuture.get();

        List<Integer> friendsIds = new ArrayList<>();
        for(FriendRequest fReq : friends) {
            friendsIds.add(fReq.sender);
        }

        // ==> load all users
        Future<List<User>> allUsersFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            UserDao userDao = appDatabase.userDao();
                            return userDao.getAllExcept(currentUserId);
                        }));

        List<User> allUsers = allUsersFuture.get();
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> !friendsIds.contains(user.uid)).collect(Collectors.toList());;

        Collections.shuffle(filteredUsers);
        filteredUsers = filteredUsers.subList(0, 7);

        for(User user : filteredUsers) {
           userDetailsList.add(new UserListDetails(user.uid, user.firstName, user.lastName,
                                    user.username, user.email, user.image));
        }

        userListAdaptor.notifyDataSetChanged();
    }

    public void onResume() {
        super.onResume();
    }

    void noResults() {
        noResults.setText("No matching results found!");
        noResults.setVisibility(View.VISIBLE);
        noResultsEmoji.setVisibility(View.VISIBLE);
        noResultsEmoji.setText("\uD83D\uDE14\uD83D\uDE14\uD83D\uDE14️");
    }

    void init() {
        noResultsEmoji.setText(null);
        noResultsEmoji.setVisibility(View.GONE);
        noResults.setVisibility(View.GONE);
    }
}
