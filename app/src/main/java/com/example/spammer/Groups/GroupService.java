package com.example.spammer.Groups;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.example.spammer.Constants;
import com.example.spammer.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKWallPostResult;

import java.util.ArrayList;

public class GroupService extends Service {
    private ArrayList<String> groupList;
    private String spamText;
    private int delay;
    private int globalPostAmount;
    private static final String CHANNEL_ID = "CANNEL_ID";
    private NotificationManager notificationManager;

    public GroupService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        groupList = intent.getStringArrayListExtra(Constants.GROUP_LIST);
        spamText = intent.getStringExtra(Constants.MESSAGE);
        delay = intent.getIntExtra(Constants.DELAY, 0);

        Thread postThread = new Thread(new Runnable() {
            @Override
            public void run() {
                globalPostAmount = groupList.size();
                for (int i = 0; i < groupList.size(); i++) {
                    //Из этой переменной получим id группы
                    final int postNumber = i;

                    //Задает параметры
                    final VKParameters parameters = new VKParameters();
                    parameters.put(VKApiConst.OWNER_ID, "-" + groupList.get(i));
                    parameters.put(VKApiConst.MESSAGE, spamText);

                    final VKRequest post = VKApi.wall().post(parameters);

                    post.setModelClass(VKWallPostResult.class);

                    post.executeWithListener(new VKRequest.VKRequestListener() {

                        @Override
                        public void onComplete(VKResponse response) {
                            // post was added
                            sendResultNotif("Success", Integer.parseInt(groupList.get(postNumber)), postNumber + 1);
                        }

                        @Override
                        public void onError(VKError error) {
                            // error
                           if (error.apiError.errorCode == -101) {   //Ошибка не значительна
                                sendResultNotif("Success", Integer.parseInt(groupList.get(postNumber)), postNumber + 1);
                            } else {
                                sendResultNotif("Error: " + error.apiError.errorMessage, Integer.parseInt(groupList.get(postNumber)), postNumber + 1);
                            }

                        }

                    });
                    try {
                        Thread.sleep(delay * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        });


        //Стартует поток
        postThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    //Отправляет уведомление с результатом
    private void sendResultNotif(String result, int groupId, int postNumber) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);

        notificationIntent.setData(Uri.parse("http://vk.com/public" + groupId));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle("Пост "+ postNumber + "/" +globalPostAmount)
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
}
