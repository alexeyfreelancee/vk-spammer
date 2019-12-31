package com.example.spammer;

import com.vk.sdk.VKSdk;

public class Initialization extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
