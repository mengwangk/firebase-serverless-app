package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * POJO for REST API error.
 */

public class ApiError {

    @SerializedName("statusCode")
    @Expose
    private Integer statusCode;

    @SerializedName("error")
    @Expose
    private String error;

    @SerializedName("source")
    @Expose
    private String source;

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
