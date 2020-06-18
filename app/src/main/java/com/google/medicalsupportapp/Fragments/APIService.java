package com.google.medicalsupportapp.Fragments;

import com.google.medicalsupportapp.Notifications.MyResponse;
import com.google.medicalsupportapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA1-rve8w:APA91bF1ickQ9_DmTxd83S1h7PSRvJUKIUrczDziiwAmZM4mdUJx26ZIwaooWO5zC032gUWc16vqXusVBLbWkh-8f9QYPUqxeP1UraJRe4oLi6pyaYMmmsSd1Y7F4a2zfLH0w2O4OA61"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
