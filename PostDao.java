package com.example.kolokvijum2;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostDao {

    @Insert
    void insert(Post post);

    @Query("SELECT * FROM posts")
    List<Post> getAll();

    @Query("DELETE FROM posts WHERE localId = (SELECT localId FROM posts LIMIT 1)")
    void deleteFirst();

    @Query("SELECT COUNT(*) FROM posts")
    int getCount();
}
