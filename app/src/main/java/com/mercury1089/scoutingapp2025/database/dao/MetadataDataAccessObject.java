package com.mercury1089.scoutingapp2025.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.mercury1089.scoutingapp2025.database.model.Metadata;

@Dao
public interface MetadataDataAccessObject {
    @Query("SELECT * FROM metadata WHERE key IS (:key)")
    Metadata fetchMetadata(String key);
    @Upsert
    void upsertMetadata(Metadata metadata);
}
