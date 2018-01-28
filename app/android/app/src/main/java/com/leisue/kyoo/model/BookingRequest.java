package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Booking request.
 */

public class BookingRequest {

    @SerializedName("booking")
    @Expose
    public final Booking booking;

    public BookingRequest(Booking booking) {
        this.booking = booking;
    }
}

