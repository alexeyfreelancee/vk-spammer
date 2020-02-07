package com.example.spammer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.spammer.Comments.CommentFragment;
import com.example.spammer.Groups.GroupFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;


public class MainActivity extends AppCompatActivity{
    BottomNavigationView bottomNav;
    private String[] scope = new String[]{VKScope.GROUPS, VKScope.WALL, VKScope.FRIENDS};
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            //Авторизация
            if (!VKSdk.isLoggedIn()){
                VKSdk.login(this,scope);
            }

            //Добавляем меню снизу
            bottomNav = findViewById(R.id.bottom_navigation);
            bottomNav.setOnNavigationItemSelectedListener(navListener);
    }

    //Меню снизу, обработка нажатий
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()) {
                        case R.id.groups:
                            selectedFragment = new GroupFragment();
                            break;
                        case R.id.comments:
                            selectedFragment = new CommentFragment();
                            break;
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.main_container,selectedFragment).commit();

                    return true;
                }
            };


    //Проверка авторизации
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Успешная авторизация
                Toast.makeText(MainActivity.this,"Вы успешно авторизовались",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(VKError error) {
                // Не успешная авторизация
                Toast.makeText(MainActivity.this,"Что-то пошло не так  :(",Toast.LENGTH_SHORT).show();
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //Проверяет подключение к интернету
    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        return wifiInfo != null && wifiInfo.isConnected();
    }
}

