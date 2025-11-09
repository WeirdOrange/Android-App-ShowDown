package com.example.showdown;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.example.showdown.DBEventTickets;

import java.util.List;

@Dao
public interface DAOEventTickets {
    @Insert
    long insert(DBEventTickets tickets);

    @Query("SELECT * FROM tickets")
    List<DBEventTickets> getAllBlocking();

    @Query("SELECT * FROM tickets WHERE eventsID = :eventId")
    List<DBEventTickets> getTicketsByEventId(int eventId);

    // Get all bookings on that date
    @Query("SELECT * FROM tickets WHERE ticketDateTime = :date")
    List<DBEventTickets> getTicketsByDate(long date);

    // Get total available tickets for an event
    @Query("SELECT SUM(availableTickets) FROM tickets WHERE eventsID = :eventId")
    Integer getTotalAvailableTickets(int eventId);
}