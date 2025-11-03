package com.example.showdown;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.showdown.DBEventTickets;

import java.util.List;

@Dao
public interface DAOEventTickets {
    @Insert
    void insert(DBEventTickets tickets);

    @Query("SELECT * FROM tickets")
    List<DBEventTickets> getAllBlocking();
}