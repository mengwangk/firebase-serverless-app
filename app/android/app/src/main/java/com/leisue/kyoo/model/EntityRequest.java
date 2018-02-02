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

    @SerializedName("password")
    @Expose
    public String password;

    public EntityRequest(Entity entity) {
        this.entity = entity;
    }

    public EntityRequest(Entity entity, String password) {
        this.entity = entity;
        this.password = password;
    }
}

