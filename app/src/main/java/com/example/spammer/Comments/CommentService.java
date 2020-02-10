package com.example.spammer.Comments;

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
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;

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
    private ArrayList<String> imageList = new ArrayList<>();
    private ArrayList<Result> resultList = new ArrayList<>();
    private boolean isInterrupted;
    private TinyDB tinyDB;

    public CommentService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) this.getSystemService(NOTIFICATION_SERVICE);
        tinyDB = new TinyDB(getApplicationContext(), "comment");
    }
    private void initVariables(){
        groupList = tinyDB.getListInt(Constants.COMMENT_GROUP_LIST);
        spamText = tinyDB.getString(Constants.COMMENT_MESSAGE);
        delay = tinyDB.getInt(Constants.COMMENT_DELAY);
        imageList = tinyDB.getListString(Constants.COMMENT_IMAGE_LIST);
        neededAmount = tinyDB.getInt(Constants.COMMENT_NEEDED_AMOUNT);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        initVariables();
        resultList.clear();
        isInterrupted = false;

        final Thread threadCom = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Integer> postIds = null;
                try {
                    postIds = getPostIds(groupList, neededAmount);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Переменная, засчет которой группа получает СВОЙ postId
                    globalCommentAmount = groupList.size() * neededAmount;
                    for (int i = 0; i < groupList.size(); i++) {
                        for (int j = 0; j < neededAmount; j++) {
                            final int postId = postIds.get(j + temp);
                            StringBuilder photoAttachments = new StringBuilder();
                            for (int k = 0; k < imageList.size(); k++) {
                                photoAttachments.append(imageList.get(k) + ",");
                            }

                            //Задает параметры
                            VKParameters parameters = new VKParameters();
                            parameters.put(VKApiConst.OWNER_ID, "-" + groupList.get(i));
                            parameters.put(VKApiConst.MESSAGE, spamText);
                            parameters.put(VKApiConst.POST_ID, postIds.get(j + temp));
                            parameters.put(VKApiConst.ATTACHMENTS, photoAttachments.toString());

                            VKRequest createComment = VKApi.wall().addComment(parameters);

                            final int finalI1 = i;
                            createComment.executeWithListener(new VKRequest.VKRequestListener() {
                                @Override
                                public void onComplete(VKResponse response) {
                                    super.onComplete(response);
                                    Result result = new Result("Success",
                                            String.valueOf(postId),
                                            String.valueOf(groupList.get(finalI1)),
                                            response.responseString.replaceAll("\\D+", ""));

                                    resultList.add(result);
                                    CommentFragment.onResultListUpdate(resultList);

                                }

                                @Override
                                public void onError(VKError error) {
                                    super.onError(error);
                                    Result result = new Result("error", "null", "null", "null");
                                    resultList.add(result);
                                    CommentFragment.onResultListUpdate(resultList);


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
                    if(!isInterrupted){
                        sendResultNotif("Рассылка завершена");

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
        isInterrupted = true;
        groupList.clear();
        sendResultNotif("Рассылка прервана");
        super.onDestroy();
    }
}
