package com.example.spammer.Groups;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.spammer.Constants;
import com.example.spammer.Models.Result;
import com.example.spammer.R;
import com.example.spammer.Utils.TinyDB;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

import java.util.ArrayList;

public class GroupService extends Service {
    private ArrayList<String> groupList;
    private String spamText;
    private int delay;
    private static final String CHANNEL_ID = "CANNEL_ID";
    private NotificationManager notificationManager;
    private ArrayList<String> imageList = new ArrayList<>();
    private static final String TAG = "GroupService";
    private Thread postThread;
    private ArrayList<Result> resultList = new ArrayList<>();
    private boolean isInterrupted;
    private TinyDB tinyDB;
    public GroupService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        tinyDB = new TinyDB(getApplicationContext(), "group");
    }

    private void initVariables(){
        groupList = tinyDB.getListString(Constants.GROUP_LIST);
        spamText = tinyDB.getString(Constants.GROUP_MESSAGE);
        delay = tinyDB.getInt(Constants.GROUP_DELAY);
        imageList = tinyDB.getListString(Constants.GROUP_IMAGE_LIST);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        resultList.clear();
        isInterrupted = false;
        initVariables();
        postThread = new Thread(new Runnable() {
            @Override
            public void run() {

                    for (int i = 0; i < groupList.size(); i++) {
                        //Из этой переменной получим id группы
                        final int postNumber = i;

                        //Задает параметры
                        final VKParameters parameters = new VKParameters();
                        parameters.put(VKApiConst.OWNER_ID, "-" + groupList.get(i));
                        parameters.put(VKApiConst.MESSAGE, spamText);

                        StringBuilder photoAttachments = new StringBuilder();
                        for (int j = 0; j < imageList.size(); j++) {
                            photoAttachments.append(imageList.get(j) + ",");
                        }
                        parameters.put(VKApiConst.ATTACHMENTS, photoAttachments.toString());
                        final VKRequest post = VKApi.wall().post(parameters);
                        post.executeWithListener(new VKRequest.VKRequestListener() {
                            @Override
                            public void onComplete(VKResponse response) {
                                // post was added
                                try {
                                    Result result = new Result("Success",
                                            response.responseString.replaceAll("\\D+", ""),
                                            groupList.get(postNumber));
                                    resultList.add(result);
                                    GroupFragment.onResultListUpdate(resultList);
                                } catch (Exception e) {
                                }

                            }

                            @Override
                            public void onError(VKError error) {
                                // error
                                try {
                                    Result result = new Result("Error", "null", "null");
                                    resultList.add(result);
                                    GroupFragment.onResultListUpdate(resultList);
                                } catch (Exception e) {
                                }
                            }

                        });
                        try {
                            Thread.sleep(delay * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    if(!isInterrupted){
                        sendResultNotif("Рассылка завершена");
                    }


            }
        });
        //Стартует поток
        postThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    //Отправляет уведомление с результатом
    private void sendResultNotif(String result) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle("Vk Spammer")
                        .setContentText(result)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent);

        createChannelIfNeeded(notificationManager);
        notificationManager.notify(0, notification.build());
    }


    //Если надо, создает канал уведомлений
    private void createChannelIfNeeded(NotificationManager nm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nc = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(nc);
        }
    }

    @Override
    public void onDestroy() {
        isInterrupted = true;
        groupList.clear();
        sendResultNotif("Рассылка прервана");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

}
