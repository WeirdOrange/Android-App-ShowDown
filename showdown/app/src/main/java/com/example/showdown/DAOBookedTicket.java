package com.example.showdown;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.showdown.DBBookedTicket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Dao
public interface DAOBookedTicket {
    @Insert
    void insert(DBBookedTicket bookedTicket);

    @Query("SELECT * FROM bookedtickets")
    List<DBBookedTicket> getAllBlocking();

    @Query("SELECT * FROM bookedtickets WHERE userId = :userId")
    List<DBBookedTicket> getBookingsByUser(int userId);

    // Count booked tickets for a specific ticket type
    @Query("SELECT COUNT(*) FROM bookedtickets WHERE ticketsId = :ticketId")
    int getBookedCount(int ticketId);

    // Get booked tickets by event (through tickets relationship)
    @Query("SELECT COUNT(*) FROM bookedtickets bt " +
            "INNER JOIN tickets t ON bt.ticketsId = t.id " +
            "WHERE t.eventsID = :eventId")
    int getBookedCountByEvent(int eventId);

    // get event id from user bookings
    @Query("SELECT DISTINCT t.eventsID FROM bookedtickets bt " +
            "INNER JOIN tickets t ON bt.ticketsId = t.id " +
            "WHERE bt.userId = :userId")
    List<Integer> getBookedEventIds(int userId);
}
