package com.example.myapplication;

public class UserListDetails {
    private int userId;
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private final byte[] profileImage;

    public UserListDetails(int userId, String firstName, String lastName,
                           String userName, String email, byte[] profileImage) {

        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.email = email;
        this.profileImage = profileImage;
    }

    public int getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public byte[] getProfileImage() {return profileImage;}
}
