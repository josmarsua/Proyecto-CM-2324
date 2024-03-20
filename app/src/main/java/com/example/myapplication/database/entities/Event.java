package com.example.myapplication.database.entities;

import android.media.Image;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.sql.Blob;
import java.util.Date;
import android.os.Parcel;
import android.os.Parcelable;


@Entity
public class Event implements Parcelable{

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "owner_user_id")
    public int ownerUserId;

    @ColumnInfo(name = "category")
    public String category;

    @ColumnInfo(name = "location")
    public String location;

    @ColumnInfo(name = "date")
    public String date;

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

    @ColumnInfo(name = "visibility")
    public String visibility;

    @ColumnInfo(name = "promoted")
    public boolean promoted;

    public Event(int ownerUserId, String name, String description, String category, String location,
                 String date, String visibility, byte[] image) {
        this.ownerUserId = ownerUserId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.location = location;
        this.date = date;
        this.visibility = visibility;
        this.image = image;
        this.promoted = false;
    }

    public int getUid() {
        return uid;
    }

    public int getownerUserId() { return ownerUserId;}

    public String getName() {
        return name;
    }

    public void setName( String name) { this.name = name;};
    public void setDescription(String description ) {this.description = description;};
    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String new_category) {
        this.category = new_category;
    }
    public String getLocation() {
        return location;
    }

    public void setLocation(String new_location) {
        this.location = new_location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String new_date) {
        this.date = new_date;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] new_image) {
        this.image = new_image;
    }

    public String getVisibility() {
        return visibility;
    }

    public boolean isPromoted() {
        return promoted;
    }

    public void setPromoted(boolean promoted) {
        this.promoted = promoted;
    }


    // Parcelable implementation
    protected Event(Parcel in) {
        uid = in.readInt();
        name = in.readString();
        description = in.readString();
        ownerUserId = in.readInt();
        category = in.readString();
        location = in.readString();
        date = in.readString();
        image = in.createByteArray();
        visibility = in.readString();
        promoted = in.readBoolean();
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(uid);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(ownerUserId);
        dest.writeString(category);
        dest.writeString(location);
        dest.writeString(date);
        dest.writeByteArray(image);
        dest.writeString(visibility);
        dest.writeBoolean(promoted);

    }
}
