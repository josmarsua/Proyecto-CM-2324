package com.example.myapplication.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import com.example.myapplication.database.entities.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user WHERE uid != :userId")
    List<User> getAllExcept(int userId);

    @Query("SELECT * FROM user")
    List<User> getAllExcept();

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);



    @Query("SELECT * FROM user WHERE uid = :userId")
    User getUserById(int userId);





    @Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
            "last_name LIKE :last LIMIT 1")
    User findByName(String first, String last);

    @Query("SELECT * FROM user WHERE uid =:uid")
    User loadById(int uid);

    @Query("SELECT * FROM User WHERE username =:username LIMIT 1")
    User findByUsername(String username);

    @Query("SELECT * FROM User WHERE username = :username AND password = :password LIMIT 1")
    User login(String username, String password);

    @Query("SELECT * FROM User " +
            "WHERE username LIKE :query " +
            "OR first_name LIKE :query " +
            "OR last_name LIKE :query " +
            "OR email LIKE :query")
    List<User> searchQuery(String query);

    @Insert
    void insertAll(User... users);

    @Insert
    void register(User user);

    @Delete
    void delete(User user);

    @Update
    public void updateUsers(User... users);

    @Update
    public void updateUser(User user);


}
