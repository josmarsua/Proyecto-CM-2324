package com.example.myapplication.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.User;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class LoginFragmentI extends Fragment {

    private static final String TAG = "LoginFragment";

    public enum loginAttempt {
        FAILURE, SUCCESS, DATABASE_FAILURE
    }

    private Button mLoginButton;
    private EditText mUsernameInput;
    private EditText mPasswordInput;
    private Button mRegisterButton;

    private View mainView;

    private TextView mTimerTextView;
    private static final long COUNTDOWN_TIME = 15 * 60 * 1000; // 15 minutes in milliseconds
    private CountDownTimer mCountDownTimer;

    private int unsuccessfulAttempts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_login, container, false);

        mLoginButton = rootView.findViewById(R.id.attempt_login_button);
        mRegisterButton = rootView.findViewById(R.id.register_button);

        mLoginButton.setOnClickListener(
                view -> {
                    loginAttempt attempt = attemptLogin();
                    // FIXME - improve error handling?
                    if (attempt == loginAttempt.SUCCESS) {
                        Toast toast = Toast.makeText(getActivity(), "Logged in!", Toast.LENGTH_SHORT);
                        changeFragment(new EventsFragment());
                    } else {
                        Toast toast = Toast.makeText(getActivity(), "Login failed!", Toast.LENGTH_SHORT);
                        toast.show();
                        Log.e(TAG, "error in login", null);
                    }
                }
        );

        mRegisterButton.setOnClickListener(
                view -> {
                  changeFragment(new RegistrationFragment());
                }
        );

        return rootView;
    }

    protected loginAttempt attemptLogin() {
        TextInputLayout usernameLayout = getActivity().findViewById(R.id.login_username);
        TextInputLayout passwordLayout = getActivity().findViewById(R.id.login_password);
        mUsernameInput = usernameLayout.getEditText();
        mPasswordInput = passwordLayout.getEditText();

        String usernameInput = mUsernameInput.getText().toString();
        String passwordInput = mPasswordInput.getText().toString();

        if (TextUtils.isEmpty(usernameInput)) {
            mUsernameInput.setError("Username is required.");
            return loginAttempt.FAILURE;
        }

        if (TextUtils.isEmpty(passwordInput)) {
            mUsernameInput.setError("Password is required.");
            return loginAttempt.FAILURE;
        }

        Future<User> userFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                        new Function<AppDatabase, User>() {
                            @Override
                            public User apply(AppDatabase appDatabase) {
                                UserDao userDao = appDatabase.userDao();
                                return userDao.login(usernameInput, passwordInput);
                            }
                        }));

        try {
            User user = userFuture.get();
            if (user == null) {
                if (unsuccessfulAttempts > 5) {
                    // this is buggy, dont use for now
                    //startCountdownTimer();
                    unsuccessfulAttempts = 0;
                    return loginAttempt.FAILURE;
                }
                unsuccessfulAttempts++;
                return loginAttempt.FAILURE;
            } else {
                GlobalState.saveLoginState(user);
                return loginAttempt.SUCCESS;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return loginAttempt.DATABASE_FAILURE;
        }
    }

    private void changeFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.commit();
    }

    private void startCountdownTimer() {
        String timer_text = "Time remaining: ";

        mTimerTextView.setVisibility(View.VISIBLE);
        mCountDownTimer = new CountDownTimer(COUNTDOWN_TIME, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(minutes);
                String timeRemaining = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                mTimerTextView.setText(timer_text.concat(timeRemaining));
            }

            @Override
            public void onFinish() {
                mLoginButton.setEnabled(true);
                mTimerTextView.setVisibility(View.GONE);
            }
        }.start();
    }

}

