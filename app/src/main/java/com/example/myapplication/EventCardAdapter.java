package com.example.myapplication;
import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.myapplication.fragments.EventViewFragment;

import java.util.DoubleSummaryStatistics;
import java.util.List;
public class EventCardAdapter extends RecyclerView.Adapter<EventCardAdapter.EventCardViewHolder> {
    Context mContext;
    List<EventCard> eventsList;
    public EventCardAdapter(Context mContext, List<EventCard> eventList) {
        this.mContext = mContext;
        this.eventsList = eventList;
    }

    @NonNull
    @Override
    public EventCardAdapter.EventCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.event_card, null, false);
        return new EventCardViewHolder(view);

    }

    @SuppressLint({"SimpleDateFormat", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull EventCardViewHolder holder, int position) {
        int eventPosition = position;
        holder.cardView.setOnClickListener(v -> {

            if(GlobalState.isLoggedIn()) {
                EventViewFragment currentEventFragment = new EventViewFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(Constants.EVENT_ID,  eventsList.get(eventPosition).getEventId());
                currentEventFragment.setArguments(bundle);
                changeFragment(currentEventFragment, v);
            }
        });

        EventCard currentEvent = eventsList.get(position);
        holder.eventName.setText(currentEvent.getEventName());

        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd");
        String day = sdf.format(currentEvent.getDate());
        sdf = new SimpleDateFormat("MMM");
        String month = sdf.format(currentEvent.getDate());
        holder.eventLocation.setText(currentEvent.getLocation());
        holder.eventDate.setText(day + '-' + month);
        if(currentEvent.promoted)
        {
            holder.promotedStar.setVisibility(View.VISIBLE);
        }
        else{
            holder.promotedStar.setVisibility(View.INVISIBLE);
        }

        byte[] image = eventsList.get(position).getImage();
        if(image == null) {
            holder.eventImage.setImageBitmap(null);
            return;
        }


        holder.eventImage.setImageBitmap(getBitMapFromByteArray(image, currentEvent));
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }

    private void changeFragment(Fragment fragment, View view){
        FragmentManager fragmentManager = ((FragmentActivity) view.getContext()).getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.LayouttoChange, fragment);
        fragmentTransaction.addToBackStack("null");
        fragmentTransaction.commit();
    }

    public void setEventList(List<EventCard> filteredEvents) {
        this.eventsList = filteredEvents;
        notifyDataSetChanged();
    }

    static public class EventCardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView eventName;
        ImageView eventImage;
        TextView eventDate;

        ImageView promotedStar;
        TextView eventLocation;
        public EventCardViewHolder(@NonNull View itemView) {
            super(itemView);
            eventName = itemView.findViewById(R.id.event_title);
            cardView = itemView.findViewById(R.id.card_view);
            eventImage = itemView.findViewById(R.id.event_image);
            eventDate = itemView.findViewById(R.id.event_date);
            eventLocation = itemView.findViewById(R.id.event_location);
            promotedStar = itemView.findViewById(R.id.promo_star);
        }
    }

    private Bitmap getBitMapFromByteArray(byte[] imageBytes, EventCard event) {
        Bitmap bitmap = null;
        bitmap = BitmapFactory.decodeByteArray(imageBytes , 0, imageBytes .length);
        if (bitmap == null) {
            Log.w(TAG, "unable to decode image for event " + event.getEventName());
            return null;
        }
        return bitmap;
    }
}
