package com.leisue.kyoo;

import android.app.Application;
import android.content.Context;

import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.service.KyooService;

import static com.leisue.kyoo.KyooConfig.API_SERVER_URL;

/**
 * Kyoo application.
 */
public final class KyooApp extends Application {

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

    public static KyooService getApiService() {
        return RetrofitClient.getClient(API_SERVER_URL).create(KyooService.class);
    }
}
