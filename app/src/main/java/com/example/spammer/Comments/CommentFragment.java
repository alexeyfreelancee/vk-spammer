package com.example.spammer.Comments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spammer.Constants;
import com.example.spammer.Groups.GroupResultListAdapter;
import com.example.spammer.Models.Result;
import com.example.spammer.R;
import com.example.spammer.Utils.BusHolder;
import com.example.spammer.Utils.ResultListUpdateEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Scanner;

import static android.view.View.GONE;
import static com.example.spammer.MainActivity.MainActivity.hasConnection;


public class CommentFragment extends Fragment{
    private EditText et_spamgroups;
    private EditText et_spamtext;
    private Button b_start_spam;
    private EditText et_amount;
    private EditText et_delay;
    private Button b_stop_spam;
    private EditText et_photos;
    private int delay;
    private int amount;
    private String spamText;
    private ArrayList<Integer> groupList;
    private static ArrayList<String> imageList = new ArrayList<>();
    private RecyclerView rv_results;
    private CommentResultListAdapter groupResultListAdapter;
    private static ArrayList<Result> resultList = new ArrayList<>();
    private LinearLayout rv_layout;
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
        initResultList();
        return v;
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

                //Добавление айди групп в массив
                Scanner scannerPhotos = new Scanner(et_photos.getText().toString());
                while (scannerPhotos.hasNext()) {
                    String resultPhotoLink = scannerPhotos.nextLine();

                    if (!resultPhotoLink.equals("")) {
                        imageList.add(resultPhotoLink);
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

    private void initResultList(){
        groupResultListAdapter = new CommentResultListAdapter(resultList, getContext());
        rv_results.setAdapter(groupResultListAdapter);
        if(resultList.isEmpty()){
            rv_layout.setVisibility(GONE);
        } else {
            rv_layout.setVisibility(View.VISIBLE);
        }
    }

    @Subscribe
    public void onResultListUpdate(ResultListUpdateEvent event) {
        //Update RecyclerView
        resultList = event.getResultList();
        initResultList();
    }
    private void initViews(View v){
        b_start_spam = v.findViewById(R.id.b_start);
        b_start_spam.setOnClickListener(startListener);

        b_stop_spam = v.findViewById(R.id.b_stop_spam);
        b_stop_spam.setOnClickListener(stopListener);

        et_photos = v.findViewById(R.id.et_photos);
        et_delay = v.findViewById(R.id.delay);
        et_spamtext = v.findViewById(R.id.et_spamtext);
        et_spamgroups = v.findViewById(R.id.et_spamgroups);
        et_amount = v.findViewById(R.id.et_amount_com);

        rv_layout = v.findViewById(R.id.linearLayout);
        rv_results = v.findViewById(R.id.rv_comment_results);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        rv_results.setLayoutManager(linearLayoutManager);
    }

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

    public static void setResultList(ArrayList<Result> resultList) {
       CommentFragment.resultList = resultList;
    }

    public static ArrayList<String> getImageList() {
        return imageList;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusHolder.getInstnace().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusHolder.getInstnace().unregister(this);
    }
}
