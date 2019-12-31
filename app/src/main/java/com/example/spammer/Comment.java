package com.example.spammer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiComment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Comment extends AppCompatActivity implements CaptchaDialog.CapthaDialogListener{
    private EditText et_spamgroups;
    private EditText et_spamtext;
    private Button b_start;
    private EditText et_amount;
    private EditText et_delay;
    int delay;
    private int amount;
    private String spamText;

    private ProgressDialog progressDialog;
    BottomNavigationView bottomNav;

    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "CANNEL_ID";


    private String captcha_answer;

    private Handler resHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        //Убирает анимацию
        overridePendingTransition(0, 0);

        //Добавляем меню снизу
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.comments);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        //Проверяет подключение к интернету
        if(!hasConnection(this)){
            Toast.makeText(this,"Нет подключения к интернету!" + "\n"+ "Попробуйте позже",Toast.LENGTH_LONG).show();
            finish();
        }


        //Авторизация
        if (!VKSdk.isLoggedIn()){
            VKSdk.login(this, VKScope.AUDIO);
        }

        //Handler для работы с UI потоком
        resHandler = new Handler();

        //Инициализация переменных
        et_delay = findViewById(R.id.delay);
        et_spamtext = findViewById(R.id.et_spamtext);
        et_spamgroups = findViewById(R.id.et_spamgroups);
        et_amount = findViewById(R.id.et_amount_com);

        b_start = findViewById(R.id.b_start);
        b_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Проверяет подключение к интернету
                if(!hasConnection(getApplicationContext())){
                    Toast.makeText(getApplicationContext(),"Нет подключения к интернету!" + "\n"+ "Попробуйте позже",Toast.LENGTH_LONG).show();
                    return;
                }
                final ArrayList<Integer> groupList = new ArrayList<>();
                //Проверка пустые ли поля
                if(et_delay.getText().toString().equals("") ||
                        et_amount.getText().toString().equals("") ||
                        et_spamgroups.getText().toString().equals("") ||
                        et_spamtext.getText().toString().equals("")){
                    Toast.makeText(Comment.this,"Вы ввели неверные данные", Toast.LENGTH_LONG).show();
                    return;
                }

                if(et_delay.getText().toString().equals("0")){
                    Toast.makeText(Comment.this,"Задержка не может быть равна нулю", Toast.LENGTH_SHORT).show();
                }
                //Инициализаця переменных
                delay = Integer.parseInt(et_delay.getText().toString());
                spamText = et_spamtext.getText().toString();
                amount = Integer.parseInt(et_amount.getText().toString());

                //Добавление айди групп в массив
                Scanner scanner = new Scanner(et_spamgroups.getText().toString());
                while (scanner.hasNext()){
                    String stringRes = scanner.nextLine().replaceAll("\\D+","");
                    if(!stringRes.equals("")) {
                        int res = Integer.parseInt(stringRes);
                        groupList.add(res);
                    }

                }

                //Задает параметры прогресс диалогу
                progressDialog = new ProgressDialog(Comment.this);
                progressDialog.setMessage("Начинаем комментить...");
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Vk Spammer");



                makeComments(groupList,amount,spamText,delay);


            }
        });
    }

    //Если надо, создает канал уведомлений
    public void createChannelIfNeeded(NotificationManager nm){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel nc = new NotificationChannel(CHANNEL_ID,CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(nc);
        }
    }

    //Отправляет уведомление с результатом
    public void sendResultNotif(String result, int groupId, int notifId, int postID) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);

        notificationIntent.setData(Uri.parse("http://vk.com/wall-" + groupId + "_" + postID));
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setGroup("gripkoi" + Math.random())      //Делаем уведомления в разных группах, чтобы увеличить их максимальное количество
                        .setContentTitle("ID группы: " + groupId)
                        .setContentText(result)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent);

        createChannelIfNeeded(notificationManager);
        notificationManager.notify(notifId, notification.build());
    }


    //Методом wall get получаем список состоящий из id постов
    public ArrayList<Integer> getPostIds(ArrayList<Integer> groupList,int neededAmount) throws Exception {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < groupList.size(); i++) {
            URL url = new URL("https://api.vk.com/method/wall.get?count=" + neededAmount + "&access_token=" + VKAccessToken.currentToken().accessToken + "&owner_id=-" + groupList.get(i) + "&v=" + "5.103");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            StringBuilder response = new StringBuilder();

            try {
                InputStream in = urlConnection.getInputStream();
                Scanner scanner = new Scanner(in);
                scanner.useDelimiter("\\A");

                if (scanner.hasNext()) {
                    response.append(scanner.next());
                } else {
                    return null;
                }
            } finally {
                urlConnection.disconnect();
            }
            JSONObject json = new JSONObject(response.toString());
            for (int j = 0; j <neededAmount ; j++) {
                list.add(json.getJSONObject("response").getJSONArray("items").getJSONObject(j).getInt("id"));
            }


        }

        return list;

    }


    //Делает коммы
    public void makeComments(final ArrayList<Integer> groupList, final int neededAmount, final String message, final int delay){
        progressDialog.show();
        //Создаем новый тред, для возможности вызова sleep
        final Thread threadCom = new Thread(new Runnable() {
            @Override
            public void run() {
                //Получаем список id постов
                ArrayList<Integer> postIds = new ArrayList<>();
                try {
                    postIds = getPostIds(groupList,neededAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }


               //Переменная, засчет которой группа получает СВОЙ postId
                int cache=0;

                //Делает посты
                for (int i = 0; i <groupList.size() ; i++) {
                    final int finalI = i;


                    for (int j = 0; j <neededAmount ; j++) {
                        Random rand = new Random();
                        //Создает уникальный номер уведомлению
                        final int notifNumber = rand.nextInt(Integer.MAX_VALUE);

                        //Получает id поста
                        final int postId = postIds.get(j+cache);

                        //Задает параметры
                        VKParameters parameters = new VKParameters();
                        parameters.put(VKApiConst.OWNER_ID,"-"+groupList.get(i));
                        parameters.put(VKApiConst.MESSAGE,message);
                        parameters.put(VKApiConst.POST_ID,postIds.get(j+cache));

                        //Выполняет запрос
                        VKRequest createComment = VKApi.wall().addComment(parameters);
                        createComment.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                super.onComplete(response);

                                sendResultNotif("Success", groupList.get(finalI), notifNumber,postId);
                            }

                            @Override
                            public void onError(VKError error) {
                                super.onError(error);

                                if (error.apiError.errorCode == 14) {
                                    //Создает диалог чтобы получать captcha answer
                                    createDialogCaptcha(error.captchaImg);
                                    error.answerCaptcha(captcha_answer);

                                } else if (error.apiError.errorCode == -101) {   //Ошибка не значительна
                                    sendResultNotif("Success", groupList.get(finalI), notifNumber,postId);
                                } else {
                                    sendResultNotif("Error: " + error.apiError.errorMessage, groupList.get(finalI), notifNumber,postId);
                                }
                            }
                        });

                        try {
                            Thread.sleep(delay*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    cache+=neededAmount;

                }

                //Закрывает прогресс диалог и создает алерт диалог, с сообщением об окончании рассылки
                resHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(Comment.this);
                        builder.setTitle("Vk Spammer")
                                .setMessage("\n Рассылка выполнена")
                                .setCancelable(false)
                                .setNegativeButton("Ок",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.cancel();
                                            }
                                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }
        });

        threadCom.start();









    }

    //Передает из диалога в активити ответ пользователя
    @Override
    public void applyInfo(String userAnswer) {
        captcha_answer = userAnswer;
    }


    //Создает диалоговое окно с капчей
    public void createDialogCaptcha(String captchaImg){
        CaptchaDialog captchaDialog = new CaptchaDialog();

        //Передает в диалог ссылку на капчу
        Bundle args = new Bundle();
        args.putString("img", captchaImg);
        captchaDialog.setArguments(args);

        captchaDialog.show(getSupportFragmentManager(), "example dialog");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        bottomNav.setSelectedItemId(R.id.comments);
    }

    //Проверка авторизации
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // Успешная авторизация

                Toast.makeText(Comment.this,"Вы успешно авторизовались",Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onError(VKError error) {
                // Не успешная авторизация
                Toast.makeText(Comment.this,"Что-то пошло не так  :(",Toast.LENGTH_SHORT).show();
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //Меню снизу обработка нажатий
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.groups:
                            Intent intent = new Intent(Comment.this, MainActivity.class);
                            startActivity(intent);
                            break;
                        case R.id.comments:
                            break;

                    }



                    return true;
                }
            };


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
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }




}
