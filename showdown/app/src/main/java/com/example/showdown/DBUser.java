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
    public String password;
    public byte[] profile;
    public byte[] backgroundProfile;

    public DBUser(String name, String email, String password, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.profile = null;
        this.backgroundProfile = null;
    }
}
