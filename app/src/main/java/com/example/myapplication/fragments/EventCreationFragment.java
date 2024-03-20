package com.example.myapplication.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.myapplication.Constants;
import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.GlobalState;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.entities.Event;
import com.google.android.material.textfield.TextInputLayout;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Future;

public class EventCreationFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private static final String TAG = "EventCreationActivity";

    private static final String IMAGE_JPEG = "image/jpeg";

    private EditText eventNameEditor;

    private EditText descriptionEditor;

    private Spinner visibilitySelector;

    private String selectedVisibility;

    private EditText locationSelector;

    private DatePickerDialog datePickerDialog;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    private TimePickerDialog timePickerDialog;

    private TimePickerDialog.OnTimeSetListener timeSetListener;

    private String selectedDate;

    private String selectedTime;

    private ActivityResultLauncher<Intent> imagePicker;

    private Bitmap selectedImage = null;

    private String selectedCategory;

    Spinner categorySpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_create_event, container, false);

        TextInputLayout eventNameLayout = view.findViewById(R.id.event_name);
        TextInputLayout descriptionEditorLayout = view.findViewById(R.id.event_description);

        //category
        //TextInputLayout categoryEditorLayout = view.findViewById(R.id.event_view_category);
        this.categorySpinner = view.findViewById(R.id.spinnerCategory);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.categories_array, android.R.layout.simple_spinner_item);

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        categorySpinner.setAdapter(categoryAdapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = (String) parent.getItemAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // what to do when nothing selected?
            }
        });

        TextInputLayout locationLayout = view.findViewById(R.id.event_location);
        //this.categoryEditor = categoryEditorLayout.getEditText();
        this.eventNameEditor = eventNameLayout.getEditText();
        this.descriptionEditor = descriptionEditorLayout.getEditText();
        this.locationSelector = locationLayout.getEditText();
        this.visibilitySelector = view.findViewById(R.id.event_visibility_levels);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.event_visibility_levels, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.visibilitySelector.setAdapter(adapter);
        this.visibilitySelector.setOnItemSelectedListener(this);

        Button createEventButton = view.findViewById(R.id.event_create_button);
        createEventButton.setOnClickListener(v -> createEvent());

        // date picker
        dateSetListener = (view1, year, monthOfYear, dayOfMonth) -> selectedDate = year + "-" + ++monthOfYear + "-" + dayOfMonth;
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        this.datePickerDialog = new DatePickerDialog(getActivity(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        this.datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
        Button pickDateButton = view.findViewById(R.id.event_Date);
        pickDateButton.setOnClickListener(v -> {
            this.datePickerDialog.show();
            this.datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            this.datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        });
        // time picker
        timeSetListener = (view12, hourOfDay, minute) -> selectedTime = hourOfDay + ":" + minute;
        this.timePickerDialog = new TimePickerDialog(getActivity(), timeSetListener,
                LocalDateTime.now().getHour(), 0, true);
        Button pickTimeButton = view.findViewById(R.id.event_time);
        pickTimeButton.setOnClickListener(v -> {
            this.timePickerDialog.show();
            this.timePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE);
            this.timePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE);
        });
        Button imageSelectButton = view.findViewById(R.id.event_image);
        imageSelectButton.setOnClickListener(v -> {
            showImageSelector();
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

        return view;
    }

    private void createEvent() {
        String name = eventNameEditor.getText().toString().trim();
        String description = descriptionEditor.getText().toString().trim();
        String location = locationSelector.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            eventNameEditor.setError("Event name is required.");
            return;
        }
        if (TextUtils.isEmpty(description)) {
            descriptionEditor.setError("Event description is required.");
            return;
        }
        if (TextUtils.isEmpty(location)) {
            locationSelector.setError("Event location is required.");
            return;
        }
        if (TextUtils.isEmpty(selectedDate)) {
            Toast.makeText(getActivity(), "Event Date is required", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(selectedTime)) {
            Toast.makeText(getActivity(), "Event time is required", Toast.LENGTH_SHORT).show();
            return;
        }
        // get the current logged in user id
        int currentUserId = GlobalState.getInt(GlobalState.keys.USER_ID.name(), -1);
        if (currentUserId == -1) {
            // not found
            Log.e(TAG, "unable to find the current user", null);
            return;
        }
        Future<List<Event>> eventCreationFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            eventDao.insert(new Event(currentUserId, name, description, selectedCategory, location,
                                    getEventDateTimeAsString(), selectedVisibility,
                                    getBitmapByteArray(this.selectedImage)));
                            List<Event> visibleEventsForUser = eventDao.getByOwnerId(currentUserId);
                            visibleEventsForUser.addAll(eventDao.getByCategory(Constants.EVENT_VISIBILITY_PUBLIC));
                            return visibleEventsForUser;
                        }));

        Toast.makeText(getActivity(), "Event created successfully!", Toast.LENGTH_SHORT).show();

        // TODO: stay here or switch automatically
        changeFragment(new ManageEventsFragment());
    }

    private void changeFragment(Fragment fragment){
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.commit();
    }

    private void showImageSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(IMAGE_JPEG);
        imagePicker.launch(intent);
    }

    private void uploadImage(Uri imageUri) {
        try {
            this.selectedImage = getBitmapFromUri(imageUri);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    public byte[] getBitmapByteArray(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * 0.8),
                (int) (bitmap.getHeight() * 0.8), true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 25, stream);
        return stream.toByteArray();
    }

    private String getEventDateTimeAsString() {
        return this.selectedDate + " " + this.selectedTime;
    }

    private Date getEventDateTimeFromSelectedDateAndTime() {
        String date_string = this.selectedDate + " " + this.selectedTime;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm");
        //formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            return formatter.parse(date_string);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        this.selectedVisibility = parent.getItemAtPosition(pos).toString().trim();
    }

    public void onNothingSelected(AdapterView<?> parent) {
    }

}