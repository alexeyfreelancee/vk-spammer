package com.example.spammer;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.squareup.picasso.Picasso;

public class CaptchaDialog extends AppCompatDialogFragment {
    private ImageView captcha_img;
    private EditText captcha_text;
    private CapthaDialogListener listener;
    private String captcha_url;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.captcha_dialog, null);

        builder.setView(view)
                .setTitle("Введите капчу")
                .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String userAnswer = captcha_text.getText().toString();
                        listener.applyInfo(userAnswer);
                    }
                });

        captcha_img = view.findViewById(R.id.captcha_img);
        captcha_text = view.findViewById(R.id.captcha_text);

        //Загружает капчу в ImageView
        captcha_url= getArguments().getString("img");
        Picasso.with(getContext()).load(captcha_url).into(captcha_img);

        return builder.create();
    }



    //Задает значение listener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (CapthaDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }


    public interface CapthaDialogListener {
        void applyInfo(String userAnswer);
    }
}
