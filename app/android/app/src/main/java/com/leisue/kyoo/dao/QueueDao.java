package com.leisue.kyoo.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.leisue.kyoo.model.Queue;

import java.util.List;

/**
 * Entity dao
 */
@Dao
public interface QueueDao extends IDao<Queue> {

    @Query("SELECT * FROM queue LIMIT 1 OFFSET :rowId")
    Queue getRow(long rowId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Queue queue);

    @Delete
    void delete(Queue queue);

    @Update
    void update(Queue queue);

    @Query("SELECT COUNT(*) FROM queue")
    int count();

}
