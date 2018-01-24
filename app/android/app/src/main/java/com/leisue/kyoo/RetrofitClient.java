package com.leisue.kyoo;

import com.leisue.kyoo.security.FirebaseUserIdTokenInterceptor;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;

    public static Retrofit getClient(String baseUrl) {

        if (retrofit == null) {

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new FirebaseUserIdTokenInterceptor())
                .build();

            retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                //.client(okHttpClient)  // Uncomment to add the authorization header
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        }
        return retrofit;
    }
}
