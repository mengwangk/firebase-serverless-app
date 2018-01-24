package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Queue request for queue creation and update.
 */

public class QueueRequest {

    @SerializedName("queue")
    @Expose
    public final Queue queue;

    public QueueRequest(Queue queue) {
        this.queue = queue;
    }

}


