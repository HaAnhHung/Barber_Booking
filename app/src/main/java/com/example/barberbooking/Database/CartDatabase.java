package com.example.barberbooking.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = CartItem.class, version = 1, exportSchema = false)
public abstract class CartDatabase extends RoomDatabase {
    private static CartDatabase instance;

    public abstract CartDAO cartDAO();

    public static CartDatabase getInstance(Context context) {
        if (instance == null)
            instance = Room.databaseBuilder(context, CartDatabase.class, "MyBarBerDB").build();
        return instance;
    }
}
