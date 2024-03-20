package com.example.myapplication;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.fragments.UserViewFragment;
import com.google.android.material.card.MaterialCardView;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class UserListAdaptor extends RecyclerView.Adapter<UserListAdaptor.UserListViewHolder> {

    private static final String TAG = "UserListAdaptor";
    private Context context;
    private List<UserListDetails> userDetailsList;

    public UserListAdaptor(Context context, List<UserListDetails> userDetailsList) {
        this.context = context;
        this.userDetailsList = userDetailsList;
    }

    @NonNull
    @Override
    public UserListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_list_item, null, false);
        return new UserListViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull UserListViewHolder holder, int position) {
        int currentPosition = position;
        holder.userCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(GlobalState.isLoggedIn()) {
                    UserListDetails userListDetails = userDetailsList.get(currentPosition);
                    UserViewFragment userViewFragment = new UserViewFragment(userListDetails.getUserId());
                    changeFragment(userViewFragment, v);
                } else {
                    Log.e(TAG, "User not logged in");
                }
            }
        });

        UserListDetails userListDetails = userDetailsList.get(currentPosition);
        holder.fullName.setText(userListDetails.getFirstName() + " " +
                userListDetails.getLastName());
        holder.accountName.setText(userListDetails.getUserName());

        byte[] image = userListDetails.getProfileImage();
        if(image == null) {
            holder.profileImage.setImageResource(R.drawable.person_new);
            holder.profileImage.setColorFilter(Color.GRAY);
            return;
        }
        holder.profileImage.clearColorFilter();
        holder.profileImage.setImageBitmap(getBitMapFromByteArray(image, userListDetails));
    }

    private void changeFragment(Fragment fragment, View view){
        FragmentManager fragmentManager = ((FragmentActivity) view.getContext()).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public int getItemCount() {
        return userDetailsList.size();
    }

    public static class UserListViewHolder extends RecyclerView.ViewHolder{

        private final MaterialCardView userCard;
        private final TextView fullName;
        private final TextView accountName;
        private final CircleImageView profileImage;

        public UserListViewHolder(@NonNull View itemView) {
            super(itemView);
            this.userCard = itemView.findViewById(R.id.user_card);
            this.fullName = itemView.findViewById(R.id.user_full_name);
            this.accountName = itemView.findViewById(R.id.user_name);
            //this.email = itemView.findViewById(R.id.user_email);
            this.profileImage = itemView.findViewById(R.id.profile_image);
        }
    }

    private Bitmap getBitMapFromByteArray(byte[] imageBytes, UserListDetails user) {
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
        if (bitmap == null) {
            Log.w(TAG, "unable to decode profile image for the user " + user.getUserName());
            return null;
        }
        return bitmap;
    }
}
