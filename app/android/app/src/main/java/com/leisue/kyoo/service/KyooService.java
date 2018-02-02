package com.leisue.kyoo.service;

import com.leisue.kyoo.model.Booking;
import com.leisue.kyoo.model.BookingRequest;
import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.model.EntityRequest;
import com.leisue.kyoo.model.Lookup;
import com.leisue.kyoo.model.Queue;
import com.leisue.kyoo.model.QueueRequest;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Kyoo REST API service.
 */

public interface KyooService {

    // -------- BEGIN entity related operation ---------------------- //
    @GET("/entity")
    Call<List<Entity>> getEntityByEmail(@Query("email") String email);

    @GET("/entity/{entityId}")
    Call<Entity> getEntity(@Path("entityId") String entityId);

    @POST("/entity")
    Call<Entity> createEntity(@Body EntityRequest entityRequest);

    @Multipart
    @POST("/entity/user")
    Call<Entity> createUserAndEntity(@Part("entityRequest") RequestBody entityRequest, @Part MultipartBody.Part photo);

    @PUT("/entity/{entityId}")
    Call<Entity> updateEntity(@Path("entityId") String entityId, @Body EntityRequest entityRequest);

    @GET("/entity/{entityId}/queue")
    Call<List<Queue>> getQueues(@Path("entityId") String entityId);

    @DELETE("/entity/{entityId}/queue/{queueId}")
    Call<String> deleteQueue(@Path("entityId") String entityId, @Path("queueId") String queueId);

    @POST("/entity/{entityId}/queue")
    Call<Queue> createQueue(@Path("entityId") String entityId, @Body QueueRequest queueRequest);

    @PUT("/entity/{entityId}/queue/{queueId}")
    Call<Queue> updateQueue(@Path("entityId") String entityId, @Path("queueId") String queueId, @Body QueueRequest queueRequest);

    // ---------- END entity related operation -----------------------//


    // -------- BEGIN queue related operation ---------------------- //
    @DELETE("/queue/{entityId}/{queueId}")
    Call<String> clearQueue(@Path("entityId") String entityId, @Path("queueId") String queueId);

    @POST("/queue/{entityId}/{queueId}")
    Call<Booking> createBooking(@Path("entityId") String entityId, @Path("queueId") String queueId, @Body BookingRequest bookingRequest);

    @PUT("/queue/{entityId}/{queueId}/{bookingId}")
    Call<Booking> updateBooking(@Path("entityId") String entityId, @Path("queueId") String queueId, @Path("bookingId") String bookingId, @Body BookingRequest bookingRequest);

    @DELETE("/queue/{entityId}/{queueId}/{bookingId}/{action}")
    Call<String> deleteBooking(@Path("entityId") String entityId, @Path("queueId") String queueId, @Path("bookingId") String bookingId,  @Path("action") String action);

    @GET("/queue/{entityId}/{queueId}")
    Call<List<Booking>> getBookings(@Path("entityId") String entityId, @Path("queueId") String queueId);

    @GET("/queue/{entityId}/{queueId}/count")
    Call<Integer> getBookingsCount(@Path("entityId") String entityId, @Path("queueId") String queueId);
    // ---------- END queue related operation -----------------------//



    // -------- BEGIN History operation ---------------------- //
    @DELETE("/history/{entityId}/{queueId}/{bookingId}/{action}")
    Call<String> returnHistory(@Path("entityId") String entityId, @Path("queueId") String queueId, @Path("bookingId") String bookingId,  @Path("action") String action);


    @DELETE("/history/{entityId}/{action}")
    Call<String> archiveHistory(@Path("entityId") String entityId, @Path("action") String action);
    // -------- END History operation ---------------------- //




    // -------- BEGIN Lookup operation ---------------------- //

    @GET("/lookup/{lookupType}")
    Call<Lookup> getLookup(@Path("lookupType") String lookupType);

    // -------- END Lookup operation ---------------------- //

}
