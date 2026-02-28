package com.mercury1089.scoutingapp2025.api.network;

import com.mercury1089.scoutingapp2025.api.model.ApiMatch;
import com.mercury1089.scoutingapp2025.database.model.Match;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;

public interface MatchService {
    @GET("event/{eventKey}/matches/simple")
    Call<List<ApiMatch>> fetchAllMatchesByEvent(@Header("X-TBA-Auth-Key") String authorization, @Path("eventKey") String eventKey);
}
