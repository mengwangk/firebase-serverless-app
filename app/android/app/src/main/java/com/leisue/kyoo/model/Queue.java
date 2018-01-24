package com.leisue.kyoo.model;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * POJO for queue.
 */
public class Queue implements Serializable, Comparable<Queue> {

    public static final String FIELD_BOOKED_DATE = "bookedDate";

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("minCapacity")
    @Expose
    private Integer minCapacity;
    @SerializedName("maxCapacity")
    @Expose
    private Integer maxCapacity;
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


    public Integer getMinCapacity() {
        return minCapacity;
    }

    public void setMinCapacity(Integer minCapacity) {
        this.minCapacity = minCapacity;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }


    public int compareTo(Queue queue) {
        if (queue == null || TextUtils.isEmpty(queue.getName()) || TextUtils.isEmpty(getName())) return 0;
        return getName().compareTo(queue.getName());
    }

    @Override
    public String toString() {
        return "Queue{" +
            "id='" + id + '\'' +
            ", name='" + name + '\'' +
            ", minCapacity=" + minCapacity +
            ", maxCapacity=" + maxCapacity +
            ", counter=" + counter +
            ", prefix='" + prefix + '\'' +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Queue queue = (Queue) o;

        if (id != null ? !id.equals(queue.id) : queue.id != null) return false;
        if (name != null ? !name.equals(queue.name) : queue.name != null) return false;
        if (minCapacity != null ? !minCapacity.equals(queue.minCapacity) : queue.minCapacity != null)
            return false;
        if (maxCapacity != null ? !maxCapacity.equals(queue.maxCapacity) : queue.maxCapacity != null)
            return false;
        if (counter != null ? !counter.equals(queue.counter) : queue.counter != null) return false;
        return prefix != null ? prefix.equals(queue.prefix) : queue.prefix == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (minCapacity != null ? minCapacity.hashCode() : 0);
        result = 31 * result + (maxCapacity != null ? maxCapacity.hashCode() : 0);
        result = 31 * result + (counter != null ? counter.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        return result;
    }
}
