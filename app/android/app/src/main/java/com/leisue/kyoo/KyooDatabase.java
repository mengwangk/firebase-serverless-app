package com.leisue.kyoo;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.leisue.kyoo.dao.QueueDao;
import com.leisue.kyoo.model.Queue;

@Database(entities = {Queue.class}, version = 1)
public abstract class KyooDatabase extends RoomDatabase {
    public abstract QueueDao queueDao();
}
