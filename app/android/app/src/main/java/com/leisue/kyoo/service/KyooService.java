package com.leisue.kyoo.service;

import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.Queue;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Kyoo REST API service.
 */

public interface KyooService {

    @GET("/entity/{entityId}")
    Call<Entity> getEntity(@Path("entityId") String entityId);

    @GET("/entity/{entityId}/queue")
    Call<List<Queue>> getQueues(@Path("entityId") String entityId);

    @POST("/queue/{entityId}/{queueId}")
    @FormUrlEncoded
    Call<Booking> saveBooking(@Path("entityId") String entityId, @Path("queueId") String queueId, @Field("booking") Booking booking);

}
