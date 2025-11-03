package com.example.showdown;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class DBUser {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String email;
    public String phoneNumber;
}
