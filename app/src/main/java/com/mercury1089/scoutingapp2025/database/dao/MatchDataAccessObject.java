package com.mercury1089.scoutingapp2025.database.dao;

import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Upsert;

import com.mercury1089.scoutingapp2025.database.model.Match;

import java.util.List;

@Dao
public interface MatchDataAccessObject {
    @Query("SELECT * FROM matches")
    List<Match> fetchAll();
    @Query("SELECT * FROM matches WHERE matchKey IS (:key)")
    Match fetchByMatchKey(String key);
    @Query("SELECT * FROM matches WHERE matchKey IN (:keys)")
    List<Match> fetchAllByMatchKeys(String[] keys);

    @Upsert()
    void storeMatch(Match match);

    @Upsert()
    void storeMatches(List<Match> matches);
}
