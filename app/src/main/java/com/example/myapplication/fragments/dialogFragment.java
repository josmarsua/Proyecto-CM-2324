package com.example.myapplication.fragments;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.example.myapplication.R;

import java.util.ArrayList;

public class dialogFragment extends DialogFragment {
    private ArrayList<String> usernames_list;
    public dialogFragment(ArrayList<String> user_list) {
        this.usernames_list = user_list;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.fragment_dialog, null);

        ListView listView = dialogView.findViewById(R.id.listUsers);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, usernames_list);
        listView.setAdapter(adapter);

        builder.setView(dialogView)
                .setTitle("Users Attending the Event")
                .setPositiveButton("Close", null);

        return builder.create();
    }
}