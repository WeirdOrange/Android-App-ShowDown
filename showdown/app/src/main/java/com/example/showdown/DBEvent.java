package com.example.showdown;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity(tableName = "events",foreignKeys = @ForeignKey(
        entity = DBUser.class,           // ← Parent table
        parentColumns = "id",          // ← Parent PK
        childColumns = "userId",       // ← Child FK column
        onDelete = ForeignKey.CASCADE  // ← Optional: delete events if user is deleted
),
        indices = {@Index("userId")}
)
public class DBEvent {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String description;
    public long publishedDate;   // timestamp of the day (00:00)
    public String location;
    public long startDate;
    public long endDate;

    public int userID;
}
