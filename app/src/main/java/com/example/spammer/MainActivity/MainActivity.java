package com.example.spammer.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.example.spammer.Constants;
import com.example.spammer.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VkAudioArray;
import com.vk.sdk.util.VKUtil;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {
    private String[] scope = new String[]{VKScope.GROUPS, VKScope.WALL, VKScope.FRIENDS, VKScope.PHOTOS, VKScope.GROUPS};
    private static final String TAG = "MainActivity";
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Авторизация
        if (!VKSdk.isLoggedIn()) {
            VKSdk.login(this, scope);
        }

        ViewPagerAdapter sectionsPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setCurrentItem(getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).getInt("pos", 0));

      //  checkToken();


        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        tabs.setTabTextColors(ContextCompat.getColorStateList(this, R.color.vk_white));
        tabs.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.vk_white));
    }


    //Проверка авторизации
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Успешная авторизация
               // checkToken();
                Toast.makeText(MainActivity.this, "Вы успешно авторизовались", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(VKError error) {
                // Не успешная авторизация
                Toast.makeText(MainActivity.this, "Что-то пошло не так  :(", Toast.LENGTH_SHORT).show();
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("pos", viewPager.getCurrentItem());
        editor.apply();
    }


    //Проверяет подключение к интернету
    public static boolean hasConnection(final Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected()) {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }

    private void checkToken(){
        String currentToken = VKSdk.getAccessToken().accessToken;

        Log.d(TAG, "checkToken: " + currentToken);
        if(!(currentToken.equals("073936c0dcb69d7086c44410691fb06984917f34498257aa9f5ba02ccb75f787d651343c7cebcf091bacd")
                || currentToken.equals("fe9ca1edab17e6262394a374e60218a9265be3215928e308c151d347d4c0ddc8bdb3db6f9a3edb40385b5")
                || currentToken.equals("7b1a638c4cee9034dd79168bab9818e80febd44e62ccb95a67c132218b378d898e09d5ab5a0d2081e7827")
                || currentToken.equals("fcd0e89c998d1303e79f0975bede5b47ef44718d3fc2cd1c3275b0c75e874a0fc2e7dc2bc8d23208dbc82"))){
            Toast.makeText(getApplicationContext(), "Вы украли приложение :(", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}

