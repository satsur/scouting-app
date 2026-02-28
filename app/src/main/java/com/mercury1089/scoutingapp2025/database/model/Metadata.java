package com.mercury1089.scoutingapp2025.database.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "metadata")
public class Metadata {
    @PrimaryKey
    @NonNull
    private String key;

    @ColumnInfo(name = "value")
    private String value;

    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }

    public Metadata(@NonNull String key, String value) {
        this.key = key;
        this.value = value;
    }

}
