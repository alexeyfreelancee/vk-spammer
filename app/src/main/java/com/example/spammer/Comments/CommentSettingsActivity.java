package com.example.spammer.Comments;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.spammer.Constants;
import com.example.spammer.MainActivity.MainActivity;
import com.example.spammer.R;
import com.example.spammer.Utils.TinyDB;

import java.util.ArrayList;
import java.util.Scanner;

import static com.example.spammer.MainActivity.MainActivity.hasConnection;

public class CommentSettingsActivity extends AppCompatActivity {
    private EditText et_amount;
    private EditText et_delay;
    private EditText et_spamgroups;
    private EditText et_spamtext;
    private EditText et_photos;
    private Button b_save;

    private int delay;
    private int amount;
    private String spamText;
    private ArrayList<Integer> groupList;
    private static ArrayList<String> imageList;

    private TinyDB tinyDB;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_settings);
        tinyDB = new TinyDB(getApplicationContext(), "comment");
        initViews();
    }

    private void initViews() {
        b_save = findViewById(R.id.b_save);
        b_save.setOnClickListener(saveListener);
        et_photos = findViewById(R.id.et_photos);
        et_delay = findViewById(R.id.delay);
        et_spamtext = findViewById(R.id.et_spamtext);
        et_spamgroups = findViewById(R.id.et_spamgroups);
        et_amount = findViewById(R.id.et_amount_com);

        if (tinyDB.getListString(Constants.COMMENT_IMAGE_LIST) != null) {
            ArrayList<String> list = tinyDB.getListString(Constants.COMMENT_IMAGE_LIST);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                stringBuilder.append(list.get(i) + "\n");
            }
            et_photos.setText(stringBuilder.toString());
        }

        if (tinyDB.getString(Constants.COMMENT_MESSAGE) != null) {
            et_spamtext.setText(tinyDB.getString(Constants.COMMENT_MESSAGE));
        }

        if (tinyDB.getListString(Constants.COMMENT_GROUP_LIST) != null) {
            ArrayList<String> list = tinyDB.getListString(Constants.COMMENT_GROUP_LIST);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                stringBuilder.append(list.get(i) + "\n");
            }
            et_spamgroups.setText(stringBuilder.toString());
        }

        if(tinyDB.getInt(Constants.COMMENT_NEEDED_AMOUNT) != 0){
            et_amount.setText(tinyDB.getInt(Constants.COMMENT_NEEDED_AMOUNT) + "");
        }


        if(tinyDB.getInt(Constants.COMMENT_DELAY) != 0){
            et_delay.setText(tinyDB.getInt(Constants.COMMENT_DELAY) + "");
        }

    }

    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(checkErrors()){
                initVariables();
                saveVariables();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }

        }
    };

    private void initVariables() {
        groupList = new ArrayList<>();
        imageList = new ArrayList<>();
        delay = Integer.parseInt(et_delay.getText().toString());
        spamText = et_spamtext.getText().toString();
        amount = Integer.parseInt(et_amount.getText().toString());

        //Добавление айди групп в массив
        Scanner scanner = new Scanner(et_spamgroups.getText().toString());
        while (scanner.hasNext()) {
            String stringRes = scanner.nextLine().replaceAll("\\D+", "");
            if (!stringRes.equals("")) {
                int res = Integer.parseInt(stringRes);
                groupList.add(res);
            }
        }

        //Добавление айди групп в массив
        Scanner scannerPhotos = new Scanner(et_photos.getText().toString());
        while (scannerPhotos.hasNext()) {
            String resultPhotoLink = scannerPhotos.nextLine();

            if (!resultPhotoLink.equals("")) {
                imageList.add(resultPhotoLink);
            }
        }

    }


    private void saveVariables() {
        tinyDB.clear();

        tinyDB.putInt(Constants.COMMENT_DELAY, delay);
        tinyDB.putString(Constants.COMMENT_MESSAGE, spamText);
        tinyDB.putListInt(Constants.COMMENT_GROUP_LIST, groupList);
        tinyDB.putListString(Constants.COMMENT_IMAGE_LIST, imageList);
        tinyDB.putInt(Constants.COMMENT_NEEDED_AMOUNT, amount);
    }


    private boolean checkErrors() {
        //Проверяет подключение к интернету
        if (!hasConnection(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "Нет подключения к интернету!" + "\n" + "Попробуйте позже", Toast.LENGTH_LONG).show();
            return false;
        }

        //Проверка пустые ли поля
        if (et_delay.getText().toString().equals("") ||
                et_amount.getText().toString().equals("") ||
                et_spamgroups.getText().toString().equals("") ||
                et_spamtext.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Вы ввели неверные данные", Toast.LENGTH_LONG).show();
            return false;
        }

        if (et_delay.getText().toString().equals("0")) {
            Toast.makeText(getApplicationContext(), "Задержка не может быть равна нулю", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
