package com.example.showdown;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.showdown.DBEvent;
import java.util.List;

@Dao
public interface DAOEvent {
    @Insert
    long insert(DBEvent events);

    @Update
    void update(DBEvent event);

    @Query("SELECT * FROM events")
    List<DBEvent> getAllBlocking();

    // Get events that are active on a specific date
    @Query("SELECT * FROM events WHERE :selectedDate >= startDate AND :selectedDate <= endDate ORDER BY startDate ASC")
    List<DBEvent> getEventsByDate(long selectedDate);

    // Get all upcoming events
    @Query("SELECT * FROM events WHERE endDate >= :currentDate ORDER BY startDate ASC")
    List<DBEvent> getUpcomingEvents(long currentDate);
}
