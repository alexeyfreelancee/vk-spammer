package com.example.spammer;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Random;
import java.util.Scanner;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.spammer.MainActivity.hasConnection;


public class CommentFragment extends Fragment implements CaptchaDialog.CapthaDialogListener{
    private EditText et_spamgroups;
    private EditText et_spamtext;
    private Button b_start;
    private EditText et_amount;
    private EditText et_delay;
    int delay;
    private int amount;
    private String spamText;
    private ProgressDialog progressDialog;
    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "CANNEL_ID";
    private String captcha_answer;
    private Handler resHandler;
    private FragmentManager fragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentManager = getFragmentManager();
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_comment, container, false);
        notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);


        //Handler для работы с UI потоком
        resHandler = new Handler();

        //Инициализация переменных
        et_delay = v.findViewById(R.id.delay);
        et_spamtext = v.findViewById(R.id.et_spamtext);
        et_spamgroups = v.findViewById(R.id.et_spamgroups);
        et_amount = v.findViewById(R.id.et_amount_com);

        b_start = v.findViewById(R.id.b_start);
        b_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Проверяет подключение к интернету
                if(!hasConnection(getContext())){
                    Toast.makeText(getContext(),"Нет подключения к интернету!" + "\n"+ "Попробуйте позже",Toast.LENGTH_LONG).show();
                    return;
                }
                final ArrayList<Integer> groupList = new ArrayList<>();
                //Проверка пустые ли поля
                if(et_delay.getText().toString().equals("") ||
                        et_amount.getText().toString().equals("") ||
                        et_spamgroups.getText().toString().equals("") ||
                        et_spamtext.getText().toString().equals("")){
                    Toast.makeText(getContext(),"Вы ввели неверные данные", Toast.LENGTH_LONG).show();
                    return;
                }

                if(et_delay.getText().toString().equals("0")){
                    Toast.makeText(getContext(),"Задержка не может быть равна нулю", Toast.LENGTH_SHORT).show();
                    return;
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
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Начинаем комментить...");
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Vk Spammer");



                makeComments(groupList,amount,spamText,delay);


            }
        });
        return v;
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
                                    try {
                                        Thread.currentThread().wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    createDialogCaptcha(error.captchaImg);
                                    error.answerCaptcha(captcha_answer);
                                    Thread.currentThread().notify();

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
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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

    //Создает диалоговое окно с капчей
    public void createDialogCaptcha(String captchaImg){
        CaptchaDialog captchaDialog = new CaptchaDialog();

        //Передает в диалог ссылку на капчу
        Bundle args = new Bundle();
        args.putString("img", captchaImg);
        captchaDialog.setArguments(args);

        captchaDialog.show(fragmentManager, "example dialog");
    }

    //Передает из диалога в активити ответ пользователя
    @Override
    public void applyInfo(String userAnswer) {
        captcha_answer = userAnswer;
    }

    //Отправляет уведомление с результатом
    public void sendResultNotif(String result, int groupId, int notifId, int postID) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);

        notificationIntent.setData(Uri.parse("http://vk.com/wall-" + groupId + "_" + postID));
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(getContext(), CHANNEL_ID)
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

    //Если надо, создает канал уведомлений
    public void createChannelIfNeeded(NotificationManager nm){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel nc = new NotificationChannel(CHANNEL_ID,CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(nc);
        }
    }
}
