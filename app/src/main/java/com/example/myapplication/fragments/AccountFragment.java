package com.example.myapplication.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.Utilities;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.UserDao;
import com.example.myapplication.database.entities.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";

    private static final String IMAGE_JPEG = "image/jpeg";

    Button logoutButton;
    ImageView currentProfileImage;
    TextView currentUserFullName;
    TextInputLayout usernameLayout;

    TextInputLayout passwordLayout;
    TextInputLayout emailLayout;
    TextInputLayout phoneNoLayout;
    TextInputLayout bioLayout;

    MaterialButton editProfileButton;
    MaterialButton editUsernameButton;
    MaterialButton editPasswordButton;
    MaterialButton editEmailButton;
    MaterialButton editPhonNoButton;
    MaterialButton editBioButton;

    MaterialTextView usernameText;
    MaterialTextView passwordText;
    MaterialTextView emailText;
    MaterialTextView phoneNoText;
    MaterialTextView bioText;

    MaterialButton addFriendsButton;

    MaterialButton viewFriendRequestsButton;

    private ActivityResultLauncher<Intent> imagePicker;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragmemt_account, container, false);
        logoutButton = view.findViewById(R.id.logout_button);
        currentProfileImage = view.findViewById(R.id.user_account_image);
        currentUserFullName = view.findViewById(R.id.full_name);

        usernameLayout = view.findViewById(R.id.acc_username_layout);
        passwordLayout = view.findViewById(R.id.acc_password_layout);
        emailLayout = view.findViewById(R.id.acc_email_layout);
        phoneNoLayout = view.findViewById(R.id.acc_phone_no_layout);
        bioLayout = view.findViewById(R.id.account_bio_layout);

        editUsernameButton = view.findViewById(R.id.edit_username);
        editPasswordButton = view.findViewById(R.id.edit_password);
        editEmailButton = view.findViewById(R.id.edit_email);
        editPhonNoButton = view.findViewById(R.id.edit_phone_no);
        editBioButton = view.findViewById(R.id.edit_bio);
        editProfileButton = view.findViewById(R.id.edit_profile_image);
        editProfileButton.setOnClickListener(v -> {
            showImageSelector();
        });

        usernameText = view.findViewById(R.id.current_username);
        usernameText.setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY);
        passwordText = view.findViewById(R.id.current_password);
        emailText = view.findViewById(R.id.current_email);
        phoneNoText = view.findViewById(R.id.current_phone_no);
        bioText = view.findViewById(R.id.current_bio);
        addFriendsButton = view.findViewById(R.id.add_friends);
        viewFriendRequestsButton = view.findViewById(R.id.view_friend_requests);

        editUsernameButton.setOnClickListener(this::showUsernameDialog);
        editPasswordButton.setOnClickListener(this::showPasswordDialog);
        editEmailButton.setOnClickListener(this::showEmailContactDialog);
        editBioButton.setOnClickListener(this::showBioDialog);
        editPhonNoButton.setOnClickListener(this::showPhoneContactDialog);
        addFriendsButton.setOnClickListener(v -> {
            handleAddFriends();
        });

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        Log.d(TAG, imageUri.toString());
                        uploadImage(imageUri);
                        Toast.makeText(getActivity(), "Image successfully uploaded", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        viewFriendRequestsButton.setOnClickListener(v -> {
            handleViewFriendRequests();
        });

        logoutButton.setOnClickListener(
                v -> {
                    GlobalState.removeLoginState();
                    changeFragment(new LoginFragmentI());
                }
        );
        return view;
    }


    private void uploadImage(Uri imageUri) {
        Bitmap selectedImage;
        try {
            selectedImage = getBitmapFromUri(imageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (selectedImage == null) {
            return;
        }
        byte[] bitMapImageByteArr = getBitmapByteArray(selectedImage);
        Future<Integer> updateFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                        new Function<AppDatabase, Integer>() {
                            @Override
                            public Integer apply(AppDatabase appDatabase) {
                                UserDao userDao = appDatabase.userDao();
                                Log.e(TAG, String.format("Looking for user: %s in DB.", GlobalState.getLoggedInUser()));
                                User currentUser = userDao.findByUsername(GlobalState.getLoggedInUser());
                                if(currentUser == null) return -1;
                                currentUser.image = bitMapImageByteArr;
                                userDao.updateUsers(currentUser);
                                return 0;
                            }
                        }));
        try {
            Integer retval = updateFuture.get();
            if(retval == -1) throw new RuntimeException("User Not Found in Database.");
            GlobalState.saveImage(bitMapImageByteArr);
            reloadData();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBitmapByteArray(Bitmap bitmap) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.8),
                (int) (bitmap.getHeight() * 0.8), true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 25, stream);
        return stream.toByteArray();
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    private void showImageSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(IMAGE_JPEG);
        imagePicker.launch(intent);
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = requireActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.addToBackStack(null); // Optional: Add the transaction to the back stack
        fragmentTransaction.commit();
    }


    @Override
    public void onResume()
    {
        super.onResume();
        this.setFields();

    }

    private void setFields()
    {
        String currentUser = GlobalState.getLoggedInUser();
        if (currentUser.isEmpty()) {
            // not found
            String error = "unable to find the current user in account fragment";
            Log.e(TAG, error, null);
            throw new RuntimeException(error);
        }
        String loggedInUserEmail = GlobalState.getLoggedInUserEmail();
        String loggedInUserPhone = GlobalState.getLoggedInUserPhone();
        String loggedInUserFullName = GlobalState.getLoggedInUserFirstname() + " " + GlobalState.getLoggedInUserLastname();

        byte[] image = GlobalState.getImage();
        if (image != null) {
            currentProfileImage.setImageBitmap(Utilities.getBitMapFromByteArray(image));
        }
        currentUserFullName.setText(loggedInUserFullName);
        String currentBio = GlobalState.getString(GlobalState.keys.USER_BIO.name(),String.format("Hi, my name is %s.", currentUser));
        usernameText.setText(currentUser);
        emailText.setText(loggedInUserEmail);
        if (loggedInUserPhone.isEmpty()) {
            phoneNoText.setText("+43xxxxxxxxxxx");
        } else {
            phoneNoText.setText(loggedInUserPhone);
        }
        bioText.setText(currentBio);
    }
    private void reloadData() {
        setFields();
        FragmentTransaction ft = getParentFragmentManager().beginTransaction();
        ft.detach(this).attach(this).commit();
    }


    public void handleViewFriendRequests() {
        changeFragment(new FriendRequestListFragment());
    }

    private void showUsernameDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.username_edit_field_layout, null);
        EditText newUsernameText = dialogView.findViewById(R.id.username_edit_text);
        builder.setView(dialogView);
        builder.setTitle("New Username");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextInputEditText usernameEditText = dialogView.findViewById(R.id.username_edit_text);
                String username = usernameEditText.getText().toString();
                String currentLoggedInUsername = GlobalState.getLoggedInUser();
                if (username.isEmpty() || username.equals(currentLoggedInUsername)) {
                    return;
                }
                if (Utilities.isUserNameAlreadyTaken(getActivity(), username)) {
                    Toast.makeText(getActivity(), "User already exists with username " +
                            username, Toast.LENGTH_SHORT).show();
                    return;
                }
                Future<Integer> updateFuture = GlobalExecutorService.getInstance().submit(
                        new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                                new Function<AppDatabase, Integer>() {
                                    @Override
                                    public Integer apply(AppDatabase appDatabase) {
                                        UserDao userDao = appDatabase.userDao();
                                        Log.e(TAG, String.format("Looking for user: %s in DB.", GlobalState.getLoggedInUser()));
                                        User currentUser = userDao.findByUsername(GlobalState.getLoggedInUser());
                                        if (currentUser == null) return -1;
                                        currentUser.username = username;
                                        Log.e(TAG, "Updating to DB:" + currentUser.toString());
                                        userDao.updateUsers(currentUser);
                                        return 0;
                                    }
                                }));
                try {
                    Integer retval = updateFuture.get();
                    if (retval == -1) throw new RuntimeException("User Not Found in Database.");
                    GlobalState.saveString(GlobalState.keys.USERNAME.name(), username);
                    reloadData();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }); // Set a null click listener initially
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();

//        Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//        positiveButton.setEnabled(true);
//
//        newUsernameText.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (s.length() == 0) {
//                    return;
//                }
//                if (isUserNameAlreadyTaken(s.toString())) {
//                    Toast.makeText(getActivity(), "User already exists with username " + s,
//                            Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                positiveButton.setEnabled(true);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (s.toString().isEmpty()) {
//                    positiveButton.setEnabled(false);
//                }
//            }
//        });

//        DialogInterface.OnClickListener positiveClickListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                TextInputEditText usernameEditText = dialogView.findViewById(R.id.username_edit_text);
//                String username = usernameEditText.getText().toString();
//                if (username.isEmpty()) {
//                    return;
//                }
//                Future<Integer> updateFuture = GlobalExecutorService.getInstance().submit(
//                        new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
//                                new Function<AppDatabase, Integer>() {
//                                    @Override
//                                    public Integer apply(AppDatabase appDatabase) {
//                                        UserDao userDao = appDatabase.userDao();
//                                        Log.e(TAG, String.format("Looking for user: %s in DB.", GlobalState.getLoggedInUser()));
//                                        User currentUser = userDao.findByUsername(GlobalState.getLoggedInUser());
//                                        if (currentUser == null) return -1;
//                                        currentUser.username = username;
//                                        Log.e(TAG, "Updating to DB:" + currentUser.toString());
//                                        userDao.updateUsers(currentUser);
//                                        return 0;
//                                    }
//                                }));
//                try {
//                    Integer retval = updateFuture.get();
//                    if (retval == -1) throw new RuntimeException("User Not Found in Database.");
//                    GlobalState.saveString(GlobalState.keys.USERNAME.name(), username);
//                    reloadData();
//                } catch (InterruptedException | ExecutionException e) {
//                    e.printStackTrace();
//                }
//            }
//        };

        // Set the custom click listener to the positive button
//        positiveButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Call the custom click listener
//                positiveClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
//            }
//        });
//        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                positiveClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
//            }
//        });
    }

    private void showPasswordDialog(View vew) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.password_edit_field_layout, null);
        builder.setTitle("New Password");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextInputEditText passwordEditText = dialogView.findViewById(R.id.acc_password_edit);
                String password = passwordEditText.getText().toString();
                if (password.isEmpty()) {
                    return;
                }
                Future<Integer> updateFuture = GlobalExecutorService.getInstance().submit(
                        new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                                new Function<AppDatabase, Integer>() {
                                    @Override
                                    public Integer apply(AppDatabase appDatabase) {
                                        UserDao userDao = appDatabase.userDao();
                                        Log.e(TAG, String.format("Looking for user: %s in DB.", GlobalState.getLoggedInUser()));
                                        User currentUser = userDao.findByUsername(GlobalState.getLoggedInUser());
                                        if(currentUser == null) return -1;
                                        currentUser.password = password;
                                        userDao.updateUsers(currentUser);
                                        return 0;
                                    }
                                }));
                try {
                    Integer retval = updateFuture.get();
                    if(retval == -1) throw new RuntimeException("User Not Found in Database.");
                    reloadData();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showEmailContactDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.email_edit_field_layout, null);
        builder.setTitle("New Email");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextInputEditText emailEditText = dialogView.findViewById(R.id.acc_contact_email_edit);
                String email = emailEditText.getText().toString();
                if (email.isEmpty()) {
                    return;
                }
                Future<Integer> updateFuture = GlobalExecutorService.getInstance().submit(
                        new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                                new Function<AppDatabase, Integer>() {
                                    @Override
                                    public Integer apply(AppDatabase appDatabase) {
                                        UserDao userDao = appDatabase.userDao();
                                        Log.e(TAG, String.format("Looking for user: %s in DB.", GlobalState.getLoggedInUser()));
                                        User currentUser = userDao.findByUsername(GlobalState.getLoggedInUser());
                                        if(currentUser == null) return -1;
                                        currentUser.email = email;
                                        userDao.updateUsers(currentUser);
                                        return 0;
                                    }
                                }));
                try {
                    Integer retval = updateFuture.get();
                    if(retval == -1) throw new RuntimeException("User Not Found in Database.");
                    GlobalState.saveString(GlobalState.keys.USER_EMAIL.name(), email);
                    reloadData();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showPhoneContactDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.phone_no_edit_field_layout, null);
        builder.setTitle("New Phone Number");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextInputEditText phoneEditText = dialogView.findViewById(R.id.acc_contact_phone_no_edit);
                String phoneNo = phoneEditText.getText().toString();
                if (phoneNo.isEmpty()) {
                    return;
                }
                Future<Integer> updateFuture = GlobalExecutorService.getInstance().submit(
                        new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                                new Function<AppDatabase, Integer>() {
                                    @Override
                                    public Integer apply(AppDatabase appDatabase) {
                                        UserDao userDao = appDatabase.userDao();
                                        Log.e(TAG, String.format("Looking for user: %s in DB.", GlobalState.getLoggedInUser()));
                                        User currentUser = userDao.findByUsername(GlobalState.getLoggedInUser());
                                        if(currentUser == null) return -1;
                                        currentUser.phoneNumber = phoneNo;
                                        userDao.updateUsers(currentUser);
                                        return 0;
                                    }
                                }));
                try {
                    Integer retval = updateFuture.get();
                    if(retval == -1) throw new RuntimeException("User Not Found in Database.");
                    GlobalState.saveString(GlobalState.keys.USER_PHONE.name(), phoneNo);
                    reloadData();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void handleAddFriends() {
        changeFragment(new UserListFragment());
    }

    private void showBioDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.bio_edit_field_layout, null);
        builder.setTitle("New Bio");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextInputEditText bioEditText = dialogView.findViewById(R.id.acc_aboutme_exitext);
                String bio = bioEditText.getText().toString();
                if (bio.isEmpty()) {
                    return;
                }
                Future<Integer> updateFuture = GlobalExecutorService.getInstance().submit(
                        new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                                new Function<AppDatabase, Integer>() {
                                    @Override
                                    public Integer apply(AppDatabase appDatabase) {
                                        UserDao userDao = appDatabase.userDao();
                                        Log.d(TAG, String.format("Looking for user: %s in DB.", GlobalState.getLoggedInUser()));
                                        User currentUser = userDao.findByUsername(GlobalState.getLoggedInUser());
                                        if(currentUser == null) return -1;
                                        currentUser.bio = bio;
                                        userDao.updateUsers(currentUser);
                                        return 0;
                                    }
                                }));
                try {
                    Integer retval = updateFuture.get();
                    if(retval == -1) throw new RuntimeException("User Not Found in Database.");
                    GlobalState.saveString(GlobalState.keys.USER_BIO.name(), bio);
                    reloadData();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }


}