package com.example.showdown;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tickets",
        foreignKeys = @ForeignKey(
            entity = DBEvent.class,           // ← Parent table
            parentColumns = "id",          // ← Parent PK
            childColumns = "eventsID",       // ← Child FK column
            onDelete = ForeignKey.RESTRICT
        ),
        indices = {@Index("eventsID")}
)
public class DBEventTickets {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public long ticketDateTime;
    public int availableTickets;
    public long dateCreated;

    public int eventsID;
}
