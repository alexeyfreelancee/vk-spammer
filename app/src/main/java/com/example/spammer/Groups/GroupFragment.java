package com.example.spammer.Groups;

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
import com.example.spammer.Models.Result;
import com.example.spammer.R;
import com.example.spammer.Utils.BusHolder;
import com.example.spammer.Utils.ResultListUpdateEvent;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Scanner;

import static android.view.View.GONE;
import static com.example.spammer.MainActivity.MainActivity.hasConnection;


public class GroupFragment extends Fragment {
    private ArrayList<String> groupList;
    private String spamText;
    private int delay;
    private String spamGroups;
    private Button b_stop_spam;
    private EditText et_delay;
    private EditText et_spamgroups;
    private EditText et_spamtext;
    private Button b_start;
    private RecyclerView rv_results;
    private GroupResultListAdapter groupResultListAdapter;
    private EditText et_photos;
    private static ArrayList<String> imageList = new ArrayList<>();
    private static final String TAG = "GroupFragment";
    private static ArrayList<Result> postResultList = new ArrayList<>();
    private LinearLayout rv_layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group, container, false);
        initViews(v);
        initResultList();
        return v;
    }

    private void startGroupService() {
        Intent intent = new Intent(getContext(), GroupService.class);
        intent.putExtra(Constants.DELAY, delay);
        intent.putExtra(Constants.GROUP_LIST, groupList);
        intent.putExtra(Constants.MESSAGE, spamText);

        getActivity().startService(intent);

    }

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (checkErrors()) {
                groupList = new ArrayList<>();
                delay = Integer.parseInt(et_delay.getText().toString());
                spamText = et_spamtext.getText().toString();

                //Добавление айди групп в массив
                Scanner scanner = new Scanner(et_spamgroups.getText().toString());
                while (scanner.hasNext()) {
                    String resultGroupLink = scanner.nextLine().replaceAll("\\D+", "");

                    if (!resultGroupLink.equals("")) {
                        groupList.add(resultGroupLink);
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

                startGroupService();
            }
        }
    };

    private View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), GroupService.class);
            getActivity().stopService(intent);
        }
    };

    private void initViews(View v) {
        b_start = v.findViewById(R.id.b_start);
        b_start.setOnClickListener(startListener);

        b_stop_spam = v.findViewById(R.id.b_stop_spam);
        b_stop_spam.setOnClickListener(stopListener);

        rv_results = v.findViewById(R.id.rv_group_results);
        rv_layout = v.findViewById(R.id.linearLayout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        rv_results.setLayoutManager(linearLayoutManager);


        et_photos = v.findViewById(R.id.et_photos);
        et_delay = v.findViewById(R.id.delay);
        et_spamtext = v.findViewById(R.id.et_spamtext);
        et_spamgroups = v.findViewById(R.id.et_spamgroups);
    }

    private void initResultList(){
        groupResultListAdapter = new GroupResultListAdapter(postResultList, getContext());
        if(postResultList.isEmpty()){
            rv_layout.setVisibility(GONE);
        } else {
            rv_layout.setVisibility(View.VISIBLE);
        }
        rv_results.setAdapter(groupResultListAdapter);
    }

    @Subscribe
    public void onResultListUpdate(ResultListUpdateEvent event) {
        //Update RecyclerView
        postResultList = event.getResultList();
        initResultList();
    }

    private boolean checkErrors() {
        //Проверяет подключение к интернету
        if (!hasConnection(getContext())) {
            Toast.makeText(getContext(), "Нет подключения к интернету!" + "\n" + "Попробуйте позже", Toast.LENGTH_LONG).show();
            return false;
        }


        //Проверка пустые ли поля
        if (et_delay.getText().toString().equals("") ||
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

    public static ArrayList<Result> getPostResultList() {
        return postResultList;
    }

    public static void setPostResultList(ArrayList<Result> postResultList) {
        GroupFragment.postResultList = postResultList;
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
