package com.example.spammer.Comments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.spammer.Constants;
import com.example.spammer.R;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;

import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class CommentService extends Service {
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "CANNEL_ID";
    private int globalCommentAmount;
    private int temp = 0;
    int delay;
    private int neededAmount;
    private String spamText;
    private ArrayList<Integer> groupList;
    private ArrayList<VKApiPhoto> imageList = new ArrayList<>();
    public CommentService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        groupList = intent.getIntegerArrayListExtra(Constants.GROUP_LIST);
        spamText = intent.getStringExtra(Constants.MESSAGE);
        delay = intent.getIntExtra(Constants.DELAY, 0);
        neededAmount = intent.getIntExtra(Constants.NEEDED_AMOUNT, 1);
        imageList = CommentFragment.getImageList();

        final Thread threadCom = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ArrayList<Integer> postIds = getPostIds(groupList, neededAmount);
                    //Переменная, засчет которой группа получает СВОЙ postId
                    globalCommentAmount = groupList.size() * neededAmount;
                    for (int i = 0; i < groupList.size(); i++) {
                        final int finalI = i;
                        for (int j = 0; j < neededAmount; j++) {
                            final int postId = postIds.get(j + temp);
                            StringBuilder photoAttachments = new StringBuilder();
                            for (int k = 0; k <imageList.size() ; k++) {
                                photoAttachments.append("photo").append(imageList.get(k).owner_id).append("_").append(imageList.get(k).id + ",");
                            }

                            //Задает параметры
                            VKParameters parameters = new VKParameters();
                            parameters.put(VKApiConst.OWNER_ID, "-" + groupList.get(i));
                            parameters.put(VKApiConst.MESSAGE, spamText);
                            parameters.put(VKApiConst.POST_ID, postIds.get(j + temp));
                            parameters.put(VKApiConst.ATTACHMENTS, photoAttachments.toString());

                            VKRequest createComment = VKApi.wall().addComment(parameters);
                            final int finalJ = j + 1;
                            createComment.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);
                                    sendResultNotif("Success", groupList.get(finalI), postId, temp + finalJ);
                                }

                                @Override
                                public void onError(VKError error) {
                                    super.onError(error);
                                    if (error.apiError.errorCode == -101) {   //Ошибка не значительна
                                        sendResultNotif("Success", groupList.get(finalI), postId, temp + finalJ);
                                    } else {
                                        sendResultNotif("Error: " + error.apiError.errorMessage, groupList.get(finalI), postId, temp + finalJ);
                                    }
                                }
                            });

                            try {
                                Thread.sleep(delay * 1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                        temp += neededAmount;

                    }
                }   catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                   sendResultNotif(e.toString(), 1, 1, 1);
                }
            }
        });

        threadCom.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //Методом wall get получаем список состоящий из id постов
    private ArrayList<Integer> getPostIds(ArrayList<Integer> groupList, int neededAmount) throws Exception {
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
            for (int j = 0; j < neededAmount; j++) {
                list.add(json.getJSONObject("response").getJSONArray("items").getJSONObject(j).getInt("id"));
            }


        }

        return list;

    }

    //Отправляет уведомление с результатом
    private void sendResultNotif(String result, int groupId, int postID, int commentNumber) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);

        notificationIntent.setData(Uri.parse("http://vk.com/wall-" + groupId + "_" + postID));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentTitle("Комментарий " + commentNumber + "/" + globalCommentAmount)
                        .setContentText(result)
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentIntent(pendingIntent);

        createChannelIfNeeded(notificationManager);
        notificationManager.notify(12, notification.build());
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
        groupList.clear();
        super.onDestroy();
    }
}
