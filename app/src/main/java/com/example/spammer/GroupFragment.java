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

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKWallPostResult;

import java.util.ArrayList;
import java.util.Scanner;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.spammer.MainActivity.hasConnection;


public class GroupFragment extends Fragment implements CaptchaDialog.CapthaDialogListener {
    private String captcha_answer;
    private FragmentManager fragmentManager;
    private ProgressDialog progressDialog;

    private EditText et_delay;

    private ArrayList<String> groupList;
    private String spamText;
    private int delay;

    private EditText et_spamgroups;
    private EditText et_spamtext;


    private NotificationManager notificationManager;
    private static final String CHANNEL_ID = "CANNEL_ID";
    private int index=0;


    private Handler resHandler;
    private Button b_start;
    private boolean running;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentManager = getFragmentManager();

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_group, container, false);

        //Получает менеджер уведомлений
        notificationManager = (NotificationManager) getContext().getSystemService(NOTIFICATION_SERVICE);

        //Хендлер для работы с UI потоком
        resHandler = new Handler();



        //Инициализация переменных
        et_delay = v.findViewById(R.id.delay);
        et_spamtext = v.findViewById(R.id.et_spamtext);
        et_spamgroups = v.findViewById(R.id.et_spamgroups);



        b_start = v.findViewById(R.id.b_start);
        b_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = true;
                groupList = new ArrayList<>();

                //Проверяет подключение к интернету
                if(!hasConnection(getContext())){
                    Toast.makeText(getContext(),"Нет подключения к интернету!" + "\n"+ "Попробуйте позже",Toast.LENGTH_LONG).show();
                    return;
                }




                //Проверка пустые ли поля
                if(et_delay.getText().toString().equals("") ||
                        et_spamgroups.getText().toString().equals("") ||
                        et_spamtext.getText().toString().equals("")){
                    Toast.makeText(getContext(),"Вы ввели неверные данные", Toast.LENGTH_LONG).show();
                    return;
                }

                if(et_delay.getText().toString().equals("0")){
                    Toast.makeText(getContext(),"Задержка не может быть равна нулю", Toast.LENGTH_SHORT).show();
                    return;
                }

                delay = Integer.parseInt(et_delay.getText().toString());
                spamText = et_spamtext.getText().toString();

                //Добавление айди групп в массив
                Scanner scanner = new Scanner(et_spamgroups.getText().toString());
                while (scanner.hasNext()){
                    String res = scanner.nextLine().replaceAll("\\D+","");

                    if(!res.equals("")) {
                        groupList.add(res);
                    }

                }


                //Задает параметры прогресс диалогу
                progressDialog = new ProgressDialog(getContext());
                progressDialog.setMessage("Начинаем постить...");
                progressDialog.setCancelable(false);
                progressDialog.setTitle("Vk Spammer");

                //Делает посты в отдельном потоке
                groupPost(groupList, spamText, delay);

            }
        });

        return v;
    }

    //Делает пост
    public void groupPost(final ArrayList<String> groupId, final String msg, final int delay){
        //Отображает прогресс диалог
        progressDialog.show();

        //Создает новый поток, чтобы не морозить главный задержкой
        Thread postThread = new Thread(new Runnable() {
            @Override
            public void run() {

                    for (int i = 0; i < groupId.size(); i++) {

                        //Из этой переменной получим id группы
                        final int groupNumber = i;

                        //Уникальный номер уведомления
                        final int notifNumber = i + index;

                        //Задает параметры
                        final VKParameters parameters = new VKParameters();
                        parameters.put(VKApiConst.OWNER_ID, "-" + groupId.get(i));
                        parameters.put(VKApiConst.MESSAGE, msg);

                        final VKRequest post = VKApi.wall().post(parameters);

                        post.setModelClass(VKWallPostResult.class);


                        post.executeWithListener(new VKRequest.VKRequestListener() {

                            @Override
                            public void onComplete(VKResponse response) {
                                // post was added
                                sendResultNotif("Success", Integer.parseInt(groupId.get(groupNumber)), notifNumber);
                                System.out.println("yea");


                            }

                            @Override
                            public void onError(VKError error) {
                                // error
                                System.out.println("no");
                                if (error.apiError.errorCode == 14) {
                                    //Создает диалог чтобы получать captcha answer
                                    try {
                                        Thread.currentThread().wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    createDialogCaptcha(error.captchaImg);
                                    error.apiError.answerCaptcha(captcha_answer);
                                    Thread.currentThread().notify();

                                } else if (error.apiError.errorCode == -101) {   //Ошибка не значительна
                                    sendResultNotif("Success", Integer.parseInt(groupId.get(groupNumber)), notifNumber);
                                } else {
                                    sendResultNotif("Error: " + error.apiError.errorMessage, Integer.parseInt(groupId.get(groupNumber)), notifNumber);
                                }

                            }

                        });
                        try {
                            Thread.sleep(delay * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
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


        //Стартует новый поток
        postThread.start();


        //Хуй знает зачем это, но пускай будет
        index++;

    }



    //Отправляет уведомление с результатом
    public void sendResultNotif(String result, int groupId, int notifId) {
        Intent notificationIntent = new Intent(Intent.ACTION_VIEW);

        notificationIntent.setData(Uri.parse("http://vk.com/public" + groupId));
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, notificationIntent, 0);

        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setGroup("gripkoi" + Math.random())      //Делаем уведомления в разных группах, чтобы увеличить их максимальное количество
                        .setContentTitle("ID группы: " + groupId)
                        .setContentText( "Результат: " + result)
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
}
