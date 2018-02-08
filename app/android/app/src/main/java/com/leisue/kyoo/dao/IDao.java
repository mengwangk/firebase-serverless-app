package com.leisue.kyoo.dao;

import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Update;

/**
 * For testing
 */

public interface IDao<T> {

    T getRow(long rowId);

    void insert(T row);

    @Delete
    void delete(T row);

    @Update
    void update(T row);

    int count();

}
