package com.leisue.kyoo;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;

import com.leisue.kyoo.model.Entity;
import com.leisue.kyoo.service.KyooService;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import static com.leisue.kyoo.KyooConfig.API_SERVER_URL;

/**
 * Kyoo application.
 */
public final class KyooApp extends Application {

    private RefWatcher refWatcher;

    static {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO);
    }

    private static Context context = null;  // Global application context

    private Entity entity = null;

    private static KyooDatabase kyooDatabase;

    public static KyooApp getInstance(Context context) {
        return (KyooApp) context.getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

    public static String getAppName() {
        return getContext().getString(R.string.app_name);
    }

    public static RefWatcher getRefWatcher(Context context) {
        return ((KyooApp) context.getApplicationContext()).refWatcher;
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

        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        refWatcher = LeakCanary.install(this);
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

    public static KyooDatabase getDatabase(){
        if (kyooDatabase == null) {
            kyooDatabase = Room.databaseBuilder(getContext(), KyooDatabase.class, "kyoo-db").allowMainThreadQueries().build();
        }
        return kyooDatabase;
    }
}
