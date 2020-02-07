package com.example.spammer.Comments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spammer.Constants;
import com.example.spammer.R;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static android.app.Activity.RESULT_OK;
import static com.example.spammer.MainActivity.MainActivity.hasConnection;


public class CommentFragment extends Fragment{
    private EditText et_spamgroups;
    private EditText et_spamtext;
    private Button b_start_spam;
    private EditText et_amount;
    private EditText et_delay;
    private Button b_add_photo;
    private Button b_stop_spam;
    private Button b_delete_photo;
    private int delay;
    private int amount;
    private String spamText;
    private ArrayList<Integer> groupList;
    private static ArrayList<VKApiPhoto> imageList = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_comment, container, false);
        initViews(v);
        return v;
    }

    @Override
    public void onResume() {
        imageList.clear();
        b_add_photo.setText(imageList.size() + " фото выбрано");
        super.onResume();
    }

    private void startCommentService(){
        Intent intent = new Intent(getContext(), CommentService.class);
        intent.putExtra(Constants.DELAY, delay);
        intent.putExtra(Constants.GROUP_LIST, groupList);
        intent.putExtra(Constants.MESSAGE, spamText);
        intent.putExtra(Constants.NEEDED_AMOUNT, amount);
        getActivity().startService(intent);
    }

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (checkErrors()) {
                groupList = new ArrayList<>();
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

                startCommentService();
            }
        }
    };

    private View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CommentService.class);
            getActivity().stopService(intent);
        }
    };

    private View.OnClickListener addPhotoListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select profile image"), Constants.CODE_CHOSE_IMAGE);
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CODE_CHOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uriProfileImage = data.getData();
            Bitmap photo = null;
            try {
                photo = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uriProfileImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (imageList.size() >= 6) {
                Toast.makeText(getContext(), "Макисмальное кол-во фото 6", Toast.LENGTH_SHORT).show();
            } else {
                VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo, VKImageParameters.jpgImage(0.9f)), 0, 60479154);
                final Bitmap finalPhoto = photo;
                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        finalPhoto.recycle();
                        imageList.add(((VKPhotoArray) response.parsedModel).get(0));
                        b_add_photo.setText(imageList.size() + " фото выбрано");
                    }

                    @Override
                    public void onError(VKError error) {
                        super.onError(error);
                    }
                });
            }


        }
    }

    private void initViews(View v){
        b_start_spam = v.findViewById(R.id.b_start);
        b_start_spam.setOnClickListener(startListener);

        b_stop_spam = v.findViewById(R.id.b_stop_spam);
        b_stop_spam.setOnClickListener(stopListener);

        b_add_photo = v.findViewById(R.id.b_addPhoto);
        b_add_photo.setOnClickListener(addPhotoListener);

        b_delete_photo = v.findViewById(R.id.b_delete_photo);
        b_delete_photo.setOnClickListener(deleteListener);

        et_delay = v.findViewById(R.id.delay);
        et_spamtext = v.findViewById(R.id.et_spamtext);
        et_spamgroups = v.findViewById(R.id.et_spamgroups);
        et_amount = v.findViewById(R.id.et_amount_com);
    }

    private View.OnClickListener deleteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            imageList.clear();
            b_add_photo.setText(R.string.add_photo);
        }
    };

    private boolean checkErrors() {
        //Проверяет подключение к интернету
        if (!hasConnection(getContext())) {
            Toast.makeText(getContext(), "Нет подключения к интернету!" + "\n" + "Попробуйте позже", Toast.LENGTH_LONG).show();
            return false;
        }

        //Проверка пустые ли поля
        if (et_delay.getText().toString().equals("") ||
                et_amount.getText().toString().equals("") ||
                et_spamgroups.getText().toString().equals("") ||
                et_spamtext.getText().toString().equals("")) {
            Toast.makeText(getContext(), "Вы ввели неверные данные", Toast.LENGTH_LONG).show();
            return false;
        }

        if (et_delay.getText().toString().equals("0")) {
            Toast.makeText(getContext(), "Задержка не может быть равна нулю", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public static ArrayList<VKApiPhoto> getImageList() {
        return imageList;
    }
}
