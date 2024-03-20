package com.example.myapplication.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Toast;

import com.example.myapplication.GlobalExecutorService;
import com.example.myapplication.R;
import com.example.myapplication.database.AppDatabase;
import com.example.myapplication.database.DatabaseCallable;
import com.example.myapplication.database.dao.EventDao;
import com.example.myapplication.database.entities.Event;
import com.google.android.material.textfield.TextInputLayout;

import java.io.FileDescriptor;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class EventEditFragment extends EventViewFragment implements AdapterView.OnItemSelectedListener {

    private static final String TAG = "EventEditActivity";

    private static final String IMAGE_JPEG = "image/jpeg";

    private EditText eventNameEditor;

    private EditText descriptionEditor;

    private Spinner visibilitySelector;

    private String selectedVisibility;

    private EditText locationSelector;

    private EditText categoryEditor;

    private DatePickerDialog datePickerDialog;

    private DatePickerDialog.OnDateSetListener dateSetListener;

    private TimePickerDialog timePickerDialog;

    private TimePickerDialog.OnTimeSetListener timeSetListener;

    private String selectedDate;

    private String selectedTime;

    private ActivityResultLauncher<Intent> imagePicker;

    private Bitmap selectedImage;

    private int eventID;

    private Event event;

    public EventEditFragment(int uid) {
        super();
        eventID = uid;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_event, container, false);
        TextInputLayout eventNameLayout = view.findViewById(R.id.edit_event_name);
        TextInputLayout descriptionEditorLayout = view.findViewById(R.id.event_edit_description);
        TextInputLayout categoryEditorLayout = view.findViewById(R.id.event_edit_category);
        TextInputLayout locationLayout = view.findViewById(R.id.event_edit_location);

        Future<Event> eventNameFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            return eventDao.getById(eventID);

                        }));
        try {
            event = eventNameFuture.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        eventNameLayout.setHint(event.name);
        descriptionEditorLayout.setHint(event.description);
        categoryEditorLayout.setHint(event.category);
        locationLayout.setHint(event.location);

        this.categoryEditor = categoryEditorLayout.getEditText();
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
        createEventButton.setOnClickListener(v -> {
            try {
                editEvent();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        // date picker
        dateSetListener = (view1, year, monthOfYear, dayOfMonth) -> selectedDate = year + "-" + monthOfYear + "-" + dayOfMonth;
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        this.datePickerDialog = new DatePickerDialog(getActivity(), dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        Button pickDateButton = view.findViewById(R.id.event_Date);
        pickDateButton.setOnClickListener(v -> datePickerDialog.show());
        // time picker
        timeSetListener = (view12, hourOfDay, minute) -> selectedTime = hourOfDay + ":" + minute;
        this.timePickerDialog = new TimePickerDialog(getActivity(), timeSetListener,
                0, 0, true);
        Button pickTimeButton = view.findViewById(R.id.event_time);
        pickTimeButton.setOnClickListener(v -> {
            timePickerDialog.show();
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

    private void editEvent() throws ExecutionException, InterruptedException {
        Future<Event> eventNameFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            return eventDao.getById(eventID);

                        }));

        event = eventNameFuture.get();

        String name = eventNameEditor.getText().toString().trim();
        String description = descriptionEditor.getText().toString().trim();
        String location = locationSelector.getText().toString().trim();
        String category = categoryEditor.getText().toString().trim();

        if (!TextUtils.isEmpty(name)) {
            event.setName(name);
        }
        if (!TextUtils.isEmpty(description)) {
            event.setDescription(description);
        }

        if (!TextUtils.isEmpty(category)) {
            event.setCategory(category);
        }

        if (!TextUtils.isEmpty(location)) {
            event.setLocation(location);
        }

        if (!TextUtils.isEmpty(selectedDate) && TextUtils.isEmpty(selectedTime)) {
            selectedTime = event.getDate().split(" ")[1];
            event.setDate(getEventDateTimeAsString());
        }
        else if (TextUtils.isEmpty(selectedDate) && !TextUtils.isEmpty(selectedTime)) {
            selectedDate = event.getDate().split(" ")[0];
            event.setDate(getEventDateTimeAsString());
        }
        else if (!TextUtils.isEmpty(selectedDate) && !TextUtils.isEmpty(selectedTime)) {
            event.setDate(getEventDateTimeAsString());
        }

        Future<List<Event>> eventCreationFuture = GlobalExecutorService.getInstance().submit(
                new DatabaseCallable<>(AppDatabase.getInstance(getActivity()),
                        appDatabase -> {
                            EventDao eventDao = appDatabase.eventDao();
                            eventDao.update(event);
                            return null;
                        }));

        Toast.makeText(getActivity(), "Event edited successfully!", Toast.LENGTH_SHORT).show();
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

    private String getEventDateTimeAsString() {
        return this.selectedDate + " " + this.selectedTime;
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getActivity().getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private Date getEventDateTimeFromSelectedDateAndTime() {
        String date_string = this.selectedDate + " " + this.selectedTime;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
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
