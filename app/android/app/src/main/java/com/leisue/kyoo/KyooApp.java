package com.leisue.kyoo;

import android.app.Application;
import android.content.Context;

import com.leisue.kyoo.model.Entity;

/**
 * Kyoo application.
 */
public class KyooApp extends Application {

    private static Context context = null;  // Global application context

    private Entity entity = null;

    public static KyooApp getInstance(Context context) {
        return (KyooApp) context.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static String getAppName() {
        return getContext().getString(R.string.app_name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initApp();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void initApp() {
        if (context == null)
            context = this.getApplicationContext();
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }
}
