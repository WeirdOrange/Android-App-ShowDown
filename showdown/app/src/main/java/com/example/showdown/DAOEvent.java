package com.example.showdown;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.showdown.DBEvent;
import java.util.List;

@Dao
public interface DAOEvent {
    @Insert
    void insert(DBEvent events);

    @Query("SELECT * FROM events")
    List<DBEvent> getAllBlocking();
}
