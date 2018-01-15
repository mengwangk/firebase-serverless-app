package com.leisue.kyoo.service;

import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.BookingRequest;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.EntityRequest;
import com.leisue.kyoo.model.Queue;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Kyoo REST API service.
 */

public interface KyooService {

    @GET("/entity/{entityId}")
    Call<Entity> getEntity(@Path("entityId") String entityId);

    @POST("/entity")
    Call<Entity> saveEntity(@Body EntityRequest entityRequest);

    @GET("/entity/{entityId}/queue")
    Call<List<Queue>> getQueues(@Path("entityId") String entityId);

    @POST("/queue/{entityId}/{queueId}")
    Call<Booking> saveBooking(@Path("entityId") String entityId, @Path("queueId") String queueId, @Body BookingRequest bookingRequest);

    @DELETE("/queue/{entityId}/{queueId}/{bookingId}")
    Call<String> deleteBooking(@Path("entityId") String entityId, @Path("queueId") String queueId,  @Path("bookingId") String bookingId);

    @DELETE("/queue/{entityId}/{queueId}")
    Call<String> clearQueue(@Path("entityId") String entityId, @Path("queueId") String queueId);

}
