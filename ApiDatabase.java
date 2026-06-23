package com.example.kolokvijum2;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {Post.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract PostDao postDao();

    private static AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "posts_db")
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
