package com.example.myapplication.database.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;



@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo(name = "first_name")
    public String firstName;
    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "phone_number")
    public String phoneNumber;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "image", typeAffinity = ColumnInfo.BLOB)
    public byte[] image;

    @ColumnInfo(name ="bio")
    public String bio;
    @Ignore
    public User(String firstName, String lastName, String username, String password, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.image = null;
        this.bio = "";
    }

    public User(String firstName, String lastName, String username, String password, String email,
                byte[] image, String bio, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.image = image;
        this.bio = bio;
        this.phoneNumber = phoneNumber;
    }

    @Ignore
    public User(String username, String password, String email)
    {
        this.username = username;
        this.password = password;
        this.email = email;
        this.firstName = "";
        this.lastName = "";
        this.image = null;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", bio='" + bio + '\'' +
                '}';
    }
}
