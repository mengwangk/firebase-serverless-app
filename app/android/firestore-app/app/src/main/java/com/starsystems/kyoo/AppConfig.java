package com.starsystems.kyoo;

import com.starsystems.kyoo.service.KyooService;

/**
 * Application configurations.
 */
public final class AppConfig {

    public static final String API_SERVER_URL = "https://kyoala-api-dev.firebaseapp.com";

    public static final String ENTITY_ID = "ddbc8cab-2eb6-4eb8-b139-98cc02d2ea0e";

    public static KyooService getApiService() {
        return RetrofitClient.getClient(API_SERVER_URL).create(KyooService.class);
    }
}
