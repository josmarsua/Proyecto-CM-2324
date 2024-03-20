package com.example.myapplication.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.R;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.User;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class RegistrationFragment extends Fragment {

    private EditText mUsernameEditText;
    private EditText mFirstNameEditText;
    private EditText mLastNameEditText;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;
    private static final String TAG = "RegisterActivity";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_signup, container, false);
        super.onCreate(savedInstanceState);

        TextInputLayout mUsernameEditTextLayout = view.findViewById(R.id.signup_username);
        mUsernameEditText = mUsernameEditTextLayout.getEditText();

        TextInputLayout mFirstNameEditTextLayout = view.findViewById(R.id.signup_fname);
        mFirstNameEditText = mFirstNameEditTextLayout.getEditText();

        TextInputLayout mLastNameEditTextLayout = view.findViewById(R.id.signup_lname);
        mLastNameEditText = mLastNameEditTextLayout.getEditText();

        TextInputLayout mEmailEditTextLayout = view.findViewById(R.id.signup_email);
        mEmailEditText = mEmailEditTextLayout.getEditText();

        TextInputLayout mPasswordEditTextLayout = view.findViewById(R.id.signup_password);
        mPasswordEditText = mPasswordEditTextLayout.getEditText();

        // Set click listener for register button
        Button registerButton = view.findViewById(R.id.register_button);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        return view;
    }

    private void registerUser() {

        String username = mUsernameEditText.getText().toString().trim();
        String lname = mLastNameEditText.getText().toString().trim();
        String fname = mFirstNameEditText.getText().toString().trim();
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();


        if (TextUtils.isEmpty(username)) {
            Log.e(TAG, "Username was empty");
            mUsernameEditText.setError("Username is required.");
            return;
        }
        if (Utilities.isUserNameAlreadyTaken(getActivity(), username)) {
            mUsernameEditText.setError("Username " + username + " already taken.");
            Toast.makeText(getActivity(), "User already exists with username " +
                    username, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            Log.e(TAG, "Email was empty");

            mEmailEditText.setError("Email is required.");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            mPasswordEditText.setError("Password is required.");
            return;
        }

        if (TextUtils.isEmpty(fname)) {
            Log.e(TAG, "Fname was empty");
        }

        if (TextUtils.isEmpty(lname)) {
            Log.e(TAG, "Lname was empty");
        }

        Future<Integer> registerFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                        new Function<AppDatabase, Integer>() {
                            @Override
                            public Integer apply(AppDatabase appDatabase) {
                                UserDao userDao = appDatabase.userDao();
                                String defaultBio = String.format("Hello, my name is %s.", username);
                                User newUser = new User(fname, lname, username, password , email,
                                        null, defaultBio, null);
                                //TODO: FIX THIS, FNAME AND LNAME NOT BEING ADDED TO DB
                                Log.d(TAG, newUser.toString());
                                userDao.register(newUser);
                                User checkUser = userDao.findByUsername(newUser.username);
                                Log.e(TAG, checkUser.toString());
                                if( checkUser == null)
                                {
                                    Log.e(TAG, "Registration Failed");
                                    return -1;
                                }
                                return 0;
                            }
                        }));

        try {
            Integer retval = registerFuture.get();
            if(retval == 0) {
                Toast.makeText(getActivity(), "Registration successful!", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(getActivity(), "Registration failed!", Toast.LENGTH_SHORT).show();
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.out.println("Registration Failed");
        }

        changeFragment(new EventsFragment()); // temp --> add destination change listener
    }

    private void changeFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.commit();
    }
}
