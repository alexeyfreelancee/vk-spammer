package com.example.spammer.Groups;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spammer.Models.Result;
import com.example.spammer.R;

import java.util.ArrayList;

import static android.view.View.GONE;


public class GroupFragment extends Fragment {
    private Button b_stop_spam;
    private Button b_start;
    private static RecyclerView rv_results;
    private static GroupResultListAdapter groupResultListAdapter;
    private static final String TAG = "GroupFragment";
    private static ArrayList<Result> postResultList = new ArrayList<>();
    private static LinearLayout rv_layout;
    private Button b_settings;
    private static TextView tv_count;
    private static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_group, container, false);
        context = getContext();
        initViews(v);

        initResultList();
        return v;
    }

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), GroupService.class);
            getActivity().startService(intent);
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
        b_start = v.findViewById(R.id.b_save);
        b_start.setOnClickListener(startListener);

        b_stop_spam = v.findViewById(R.id.b_stop_spam);
        b_stop_spam.setOnClickListener(stopListener);

        b_settings = v.findViewById(R.id.b_settings);
        b_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), GroupSettingsActivity.class));
            }
        });

        tv_count = v.findViewById(R.id.tv_count);
        rv_results = v.findViewById(R.id.rv_group_results);
        rv_layout = v.findViewById(R.id.linearLayout);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext()){
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        rv_results.setLayoutManager(linearLayoutManager);
    }

    private static void initResultList(){
        groupResultListAdapter = new GroupResultListAdapter(postResultList, context);
        if(postResultList.isEmpty()){
            rv_layout.setVisibility(GONE);
            tv_count.setVisibility(GONE);
        } else {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText("Сделано постов: " + postResultList.size());
            rv_layout.setVisibility(View.VISIBLE);
        }
        rv_results.setAdapter(groupResultListAdapter);
    }



    public static void onResultListUpdate(ArrayList<Result> resultList) {
        //Update RecyclerView
        postResultList = resultList;
        initResultList();
    }


}
