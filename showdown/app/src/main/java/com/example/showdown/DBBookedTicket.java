package com.example.showdown;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "bookedtickets",
    foreignKeys = {
        @ForeignKey(
            entity = DBEventTickets.class,           // ← Parent table
            parentColumns = "id",          // ← Parent PK
            childColumns = "ticketsId",       // ← Child FK column
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = DBUser.class,           // ← Parent table
            parentColumns = "id",          // ← Parent PK
            childColumns = "userId",       // ← Child FK column
            onDelete = ForeignKey.CASCADE
            )
    },
    indices = {
        @Index("ticketsId"),
        @Index("userId")
    }
)
public class DBBookedTicket {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public long ticketDateTime;
    public int availableTickets;
    public long dateCreated;

    public int ticketsId;
    public int userId;
}
