package com.example.showdown;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.showdown.DBUser;
import java.util.List;

@Dao
public interface DAOUser {
    @Insert
    long insert(DBUser user);

    @Update
    void update(DBUser user);

    @Query("SELECT * FROM users")
    List<DBUser> getAllBlocking();

    @Query("SELECT * FROM users WHERE id = :id")
    DBUser getUserById(int id);

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    DBUser login(String email, String password);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    DBUser getUserByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);
}

