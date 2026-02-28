package com.mercury1089.scoutingapp2025.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.mercury1089.scoutingapp2025.database.dao.MatchDataAccessObject;
import com.mercury1089.scoutingapp2025.database.dao.MetadataDataAccessObject;
import com.mercury1089.scoutingapp2025.database.model.Match;
import com.mercury1089.scoutingapp2025.database.model.Metadata;

@Database(entities = {Match.class, Metadata.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract MatchDataAccessObject matchDao();
    public abstract MetadataDataAccessObject metadataDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            // lock if null so only one thread creates an instance
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "app-db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
