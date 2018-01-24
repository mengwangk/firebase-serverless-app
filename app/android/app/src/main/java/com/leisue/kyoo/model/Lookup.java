package com.leisue.kyoo.model;

/**
 * Lookup data model.
 */

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Lookup {

    public enum TYPE {

        INDUSTRY("industry");

        private final String id;

        TYPE(final String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }
    }

    @SerializedName("values")
    @Expose
    private List<String> values = null;

    @SerializedName("id")
    @Expose
    private String id;

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
