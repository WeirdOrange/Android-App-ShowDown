package com.example.showdown;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.showdown.DBBookedTicket;
import java.util.List;

@Dao
public interface DAOBookedTicket {
    @Insert
    void insert(DBBookedTicket bookedTicket);

    @Query("SELECT * FROM events")
    List<DBBookedTicket> getAllBlocking();
}
