package com.example.showdown;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import android.content.Context;

import com.example.showdown.DBUser;
import com.example.showdown.DBEvent;
import com.example.showdown.DBEventTickets;
import com.example.showdown.DBBookedTicket;

@Database(entities = {DBUser.class, DBEvent.class, DBEventTickets.class, DBBookedTicket.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract DAOUser userDao();
    public abstract DAOEvent eventsDao();
    public abstract DAOEventTickets eventTicketDao();
    public abstract DAOBookedTicket bookedTicketDao();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "showdown-db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}