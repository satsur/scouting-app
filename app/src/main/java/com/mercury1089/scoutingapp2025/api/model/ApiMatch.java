package com.mercury1089.scoutingapp2025.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// Model for the automatic GSON converter factory to use
// When grabbing match data from API
public class ApiMatch {
    @SerializedName("key")
    private String key;
    @SerializedName("comp_level")
    private String compLevel;
    @SerializedName("match_number")
    private int matchNumber;
    @SerializedName("alliances")
    private Alliances alliances;
    public String getKey() {
        return key;
    }
    public Alliances getAlliances() { return alliances; }

    public static class Alliances {

        @SerializedName("red")
        private Alliance redAlliance;
        @SerializedName("blue")

        private Alliance blueAlliance;
        public Alliance getRedAlliance() {
            return redAlliance;
        }

        public Alliance getBlueAlliance() {
            return blueAlliance;
        }
    }

    public static class Alliance {
        @SerializedName("team_keys")
        private List<String> teamKeys;
        public List<String> getTeamKeys() {
            return teamKeys;
        }
    }
}
