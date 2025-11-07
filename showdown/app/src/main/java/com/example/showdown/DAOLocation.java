package com.example.showdown;

import androidx.room.Insert;
import androidx.room.Query;

public interface DAOLocation {
    @Insert
    long insert(DBLocation locationDetails);

    @Query("SELECT name FROM location WHERE id = :locationId")
    int getLocationName(int locationId);

    @Query("SELECT * FROM location WHERE id = :locationId")
    int getLocation(int locationId);
}
