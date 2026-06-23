package com.example.kolokvijum2;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "posts")
public class Post {

    @PrimaryKey(autoGenerate = true)
    public int localId;

    public int id;
    public String title;
    public String body;
    public int userId;
}
