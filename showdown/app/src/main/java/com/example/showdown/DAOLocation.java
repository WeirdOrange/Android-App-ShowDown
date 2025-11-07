package com.example.showdown;

import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

public interface DAOLocation {
    @Insert
    void insert(DBLocation locationDetails);

    @Query("SELECT name FROM location WHERE id = :locationId")
    int getLocationName(int locationId);

    @Query("SELECT * FROM location WHERE id = :locationId")
    int getLocation(int locationId);
}
