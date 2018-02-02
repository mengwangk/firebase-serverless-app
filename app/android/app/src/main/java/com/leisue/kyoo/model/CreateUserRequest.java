package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Request to create user.
 */

public class CreateUserRequest {

    @SerializedName("entity")
    @Expose
    public final Entity entity;

    @SerializedName("password")
    @Expose
    public String password;


    public CreateUserRequest(Entity entity, String password) {
        this.entity = entity;
        this.password = password;
    }
}
