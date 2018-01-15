package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Booking request.
 */

public class EntityRequest {

    @SerializedName("entity")
    @Expose
    public final Entity entity;

    public EntityRequest(Entity entity) {
        this.entity = entity;
    }
}

