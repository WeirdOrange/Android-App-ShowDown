package com.example.showdown;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.showdown.DBUser;
import java.util.List;

@Dao
public interface DAOUser {
    @Insert
    void insert(DBUser user);

    @Query("SELECT * FROM users")
    List<DBUser> getAllBlocking();
}
