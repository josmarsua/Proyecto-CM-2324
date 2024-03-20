package com.example.myapplication.fragments;

import static com.example.myapplication.Constants.PENDING_FRIEND_REQUEST_STATUS;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.UserListAdaptor;
import com.example.myapplication.UserListDetails;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.FriendRequestDao;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.FriendRequest;
import com.example.myapplication.database.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class FriendRequestListFragment extends Fragment {

    private static final String TAG = "FriendRequestViewFragment";

    private RecyclerView recyclerUserListView;

    private UserListAdaptor userListAdaptor;

    List<UserListDetails> friendRequestList;

    public FriendRequestListFragment() {
        friendRequestList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.recyclerUserListView = view.findViewById(R.id.recyclerview_user_list);
        this.recyclerUserListView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        this.userListAdaptor = new UserListAdaptor(getContext(), this.friendRequestList);
        listFriendRequests();
        this.recyclerUserListView.setAdapter(this.userListAdaptor);
        this.recyclerUserListView = view.findViewById(R.id.recyclerview_user_list);
    }

    private void listFriendRequests() {
        if (!GlobalState.isLoggedIn()) {
            // not loggged in
            Log.e(TAG, "User not logged in", null);
            return;
        }
        // get the current logged in user id

        int currentUserId = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);
        if (currentUserId == -1) {
            // not found
            String errorMsg = "unable to get the current user id";
            Log.e(TAG, errorMsg, null);
            throw new RuntimeException(errorMsg);
        }
        List<FriendRequest> friendRequests;
        Future<List<FriendRequest>> friendRequestsFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            FriendRequestDao friendRequestDao = appDatabase.friendRequestDao();
                            Log.e(TAG, "Getting Friend Requests for User with UID: " + String.valueOf(currentUserId));
                                    return friendRequestDao.
                                    getAllFriendRequestsReceivedByUserAndStatus(currentUserId, PENDING_FRIEND_REQUEST_STATUS);
                        }));

        try {
            friendRequests = friendRequestsFuture.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (friendRequests == null || friendRequests.size() == 0) {
            // no friend requests
            Toast toast = Toast.makeText(getActivity(), "No pending friend requests!", Toast.LENGTH_SHORT);
            toast.show();
            getParentFragmentManager().popBackStack();
            return;
        }
        int[]userIds = new int[friendRequests.size()];
        for (int i = 0 ; i < userIds.length ; i++) {
            userIds[i] = friendRequests.get(i).getSender();
        }
        // get users
        List<User> friendRequestSenders = null;
        Future<List<User>> friendReuestSenders = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getContext().getApplicationContext()),
                        appDatabase -> {
                            UserDao userDao = appDatabase.userDao();
                            return userDao.loadAllByIds(userIds);
                        }));

        try {
            friendRequestSenders = friendReuestSenders.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        for (User user : friendRequestSenders) {
            friendRequestList.add(new UserListDetails(user.uid, user.firstName, user.lastName,
                    user.username, user.email, user.image));
        }
        userListAdaptor.notifyDataSetChanged();
    }

    public void onResume() {
        super.onResume();
    }
}
