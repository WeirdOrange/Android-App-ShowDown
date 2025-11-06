package com.example.showdown;

import androidx.room.Embedded;
import androidx.room.Relation;

public class EventWithDetails {
    @Embedded
    public DBEvent event;
    @Relation(
            parentColumn = "userId",
            entityColumn = "id"
    )
    public DBUser user;
    // Available tickets calculation will be done separately
    public int availableTickets;

    public EventWithDetails() {
        this.availableTickets = 0;
    }
}
