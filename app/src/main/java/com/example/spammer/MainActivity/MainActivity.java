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
        Log.d(TAG, "onCreate: " + getSharedPreferences(Constants.SHARED_PREFS, MODE_PRIVATE).getInt("pos", 0));


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
}

