package com.example.myapplication.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.Constants;
import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.FriendRequestDao;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.FriendRequest;
import com.example.myapplication.database.entities.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class UserViewFragment extends Fragment {

    private static final String TAG = "UserViewFragment";
    public static final String BTN_TEXT_ACCEPT_FRIEND_REQUEST = "Accept Friend Request";
    public static final String BTN_TEXT_EXISTING_FRIEND = "Already a Friend!";
    public static final String BTN_STATUS_PENDING_FRIEND_REQUEST = "Friend Request Pending";
    private int userId;
    private User user;
    private ImageView profileImage;
    private MaterialTextView fullNameView;
    private MaterialTextView usernameView;
    private MaterialTextView emailView;
    private MaterialTextView phoneNoView;
    private MaterialTextView bioView;
    private Button friendRequestStatusButton;

    private MaterialButton backButton;

    public UserViewFragment(int userId) {
        this.userId = userId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_user_view, container, false);

        Future<User> userFuture = GlobalExecutorService.getInstance().submit(new DatabaseCallable<>(AppDatabase.getInstance(getActivity().getApplicationContext()),
                appDatabase -> {
                    UserDao userDao = appDatabase.userDao();
                    return user = userDao.loadById(this.userId);
                }
        ));
        try {
            User user = userFuture.get();
            if (user == null) Log.e(TAG, "Cant find User by this ID");
            else {
                this.user = user;
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profileImage = view.findViewById(R.id.other_user_profile_image);
        fullNameView = view.findViewById(R.id.other_user_full_name);
        usernameView = view.findViewById(R.id.other_user_username);
        emailView = view.findViewById(R.id.other_user_email);
        phoneNoView = view.findViewById(R.id.other_user_phone_no);
        bioView = view.findViewById(R.id.other_user_bio);
        backButton = view.findViewById(R.id.back1);

        if (user.image != null) {
            profileImage.setImageBitmap(Utilities.getBitMapFromByteArray(user.image));
        }
        emailView.setText(user.email);
        if (user.phoneNumber != null && !user.phoneNumber.isEmpty()) {
            phoneNoView.setText(user.phoneNumber);
        } else {
            phoneNoView.setText("+43xxxxxxxxxxx");
        }
        usernameView.setText(user.username);
        fullNameView.setText(user.firstName + " " + user.lastName);
        bioView.setText(user.bio);
        this.friendRequestStatusButton = view.findViewById(R.id.friend_request_status_btn);
        setFriendRequestButtonStatus();
        // set the listener
        this.friendRequestStatusButton.setOnClickListener(v -> {
            handleFriendRequest();
            setFriendRequestButtonStatus();
        });

        backButton.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void handleFriendRequest() {
        if (!GlobalState.isLoggedIn()) {
            Log.e("User not logged in", null);
            return;
        }
        int currentUserId = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);
        if (currentUserId == -1) {
            // not found
            Log.e(TAG, "unable to find the current user", null);
            return;
        }
        if (this.friendRequestStatusButton.getText().equals(BTN_TEXT_ACCEPT_FRIEND_REQUEST)) {
            acceptFriendRequest(currentUserId);
        } else {
            sendFriendRequest(currentUserId);
        }
    }

    private void sendFriendRequest(int currentUserId) {
        Future<FriendRequest> friendRequestFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            FriendRequestDao friendRequestDao = appDatabase.friendRequestDao();
                            FriendRequest friendRequest = new FriendRequest(currentUserId,
                                    this.userId, Constants.PENDING_FRIEND_REQUEST_STATUS);
                            Log.e(TAG, "Sent new Friend Request: " + friendRequest);
                            friendRequestDao.insert(friendRequest);
                            return friendRequestDao.getFriendRequestBySenderAndReciever(currentUserId,
                                    this.userId);
                        }));
        try {
            friendRequestFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptFriendRequest(int currentUserId) {
        FriendRequest friendRequest = null;
        Future<FriendRequest> friendRequestFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            FriendRequestDao friendRequestDao = appDatabase.friendRequestDao();
                            return friendRequestDao.getFriendRequestBySenderAndReciever(this.userId,
                                    currentUserId);
                        }));
        try {
            friendRequest = friendRequestFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        // update status and update the DB
        friendRequest.setStatus(Constants.ACCEPTED_FRIEND_REQUEST_STATUS);
        FriendRequest finalFriendRequest = friendRequest;
        friendRequestFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            FriendRequestDao friendRequestDao = appDatabase.friendRequestDao();
                            friendRequestDao.update(finalFriendRequest);
                            return friendRequestDao.getFriendRequestBySenderAndReciever(this.userId,
                                    currentUserId);
                        }));
        try {
            friendRequestFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void setFriendRequestButtonStatus() {
        if (!GlobalState.isLoggedIn()) {
            Log.e("User not logged in", null);
            return;
        }
        int currentUserId = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);
        if (currentUserId == -1) {
            // not found
            Log.e(TAG, "Unable to find the current user", null);
            return;
        }
        // check if the current user has sent a friend request
        // if so, disable the button and change the text
        FriendRequest friendRequest;
        Future<FriendRequest> friendRequestFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            FriendRequestDao friendRequestDao = appDatabase.friendRequestDao();
                            return friendRequestDao.getFriendRequestBySenderAndReciever(currentUserId,
                                    this.userId);
                        }));

        try {
            friendRequest = friendRequestFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (friendRequest != null) {
            // friend request sent, disable the button
            if (friendRequest.getStatus().equals(Constants.PENDING_FRIEND_REQUEST_STATUS)) {
                // status pending
                friendRequestButtoonDisableAndSetStatusAsPending();
            } else if (friendRequest.getStatus().equals(Constants.ACCEPTED_FRIEND_REQUEST_STATUS)) {
                // status accepted
                friendRequestButtoonDisableAndSetStatusAsFriend();
            }
        }
        // if this user has sent a friend request to logged in user, need to handle that.
        friendRequestFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            FriendRequestDao friendRequestDao = appDatabase.friendRequestDao();
                            return friendRequestDao.getFriendRequestBySenderAndReciever(this.userId,
                                    currentUserId);
                        }));

        try {
            friendRequest = friendRequestFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (friendRequest != null) {
            // friend request recieved, change the button text
            if (friendRequest.getStatus().equals(Constants.PENDING_FRIEND_REQUEST_STATUS)) {
                // status pending
                friendRequestButtoonSetStatusReadyToAccept();
            } else if (friendRequest.getStatus().equals(Constants.ACCEPTED_FRIEND_REQUEST_STATUS)) {
                // status accepted
                friendRequestButtoonDisableAndSetStatusAsFriend();
            }
        }
    }

    private void friendRequestButtoonDisableAndSetStatusAsPending() {
        this.friendRequestStatusButton.setEnabled(false);
        this.friendRequestStatusButton.setText(BTN_STATUS_PENDING_FRIEND_REQUEST);
    }

    private void friendRequestButtoonSetStatusReadyToAccept() {
        this.friendRequestStatusButton.setText(BTN_TEXT_ACCEPT_FRIEND_REQUEST);
    }


    private void friendRequestButtoonDisableAndSetStatusAsFriend() {
        this.friendRequestStatusButton.setEnabled(false);
        this.friendRequestStatusButton.setText(BTN_TEXT_EXISTING_FRIEND);
    }
}
