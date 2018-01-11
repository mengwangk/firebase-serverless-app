package com.leisue.kyoo.service;

import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Kyoo REST API service.
 */

public interface KyooService {

    @GET("/entity/{entityId}")
    Call<Entity> getEntity(@Path("entityId") String entityId);

    @GET("/entity/{entityId}/queue")
    Call<List<Queue>> getQueues(@Path("entityId") String entityId);


    @GET("/queue/{entityId}/{queueId}")
    Call<List<Booking>> getBooking(@Path("entityId") String entityId, @Path("queueId") String queueId);

}
