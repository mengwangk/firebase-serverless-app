package com.leisue.kyoo.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * POJO for queue.
 */
public class Queue implements Serializable {

    public static final String FIELD_BOOKED_DATE = "bookedDate";

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("capacity")
    @Expose
    private String capacity;
    @SerializedName("counter")
    @Expose
    private Integer counter;
    @SerializedName("prefix")
    @Expose
    private String prefix;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCapacity() {
        return capacity;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        return "Queue{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", capacity='" + capacity + '\'' +
            ", counter=" + counter +
            ", prefix='" + prefix + '\'' +
            '}';
    }
}
