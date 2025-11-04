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

    @Query("SELECT * FROM bookedtickets")
    List<DBBookedTicket> getAllBlocking();

    // Count booked tickets for a specific ticket type
    @Query("SELECT COUNT(*) FROM bookedtickets WHERE ticketsId = :ticketId")
    int getBookedCount(int ticketId);

    // Get booked tickets by event (through tickets relationship)
    @Query("SELECT COUNT(*) FROM bookedtickets bt " +
            "INNER JOIN tickets t ON bt.ticketsId = t.id " +
            "WHERE t.eventsID = :eventId")
    int getBookedCountByEvent(int eventId);
}
