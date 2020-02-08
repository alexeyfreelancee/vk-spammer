package com.example.spammer.Comments;

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


public class CommentFragment extends Fragment{
    private Button b_start_spam;
    private Button b_stop_spam;
    private Button b_settings;
    private static ArrayList<Result> resultList = new ArrayList<>();
    private static RecyclerView rv_results;
    private static CommentResultListAdapter groupResultListAdapter;
    private static LinearLayout rv_layout;
    private static TextView tv_count;
    private static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_comment, container, false);
        context = getContext();
        initViews(v);
        initResultList();
        return v;
    }

    private View.OnClickListener startListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CommentService.class);
            getActivity().startService(intent);
        }
    };

    private View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getContext(), CommentService.class);
            getActivity().stopService(intent);
        }
    };

    private static void initResultList(){
        groupResultListAdapter = new CommentResultListAdapter(resultList, context);
        rv_results.setAdapter(groupResultListAdapter);
        if(resultList.isEmpty()){
            rv_layout.setVisibility(GONE);
            tv_count.setVisibility(GONE);
        } else {
            tv_count.setVisibility(View.VISIBLE);
            tv_count.setText("Комментариев сделано: " + resultList.size());
            rv_layout.setVisibility(View.VISIBLE);
        }
    }

    public static void onResultListUpdate(ArrayList<Result> results) {
        resultList = results;
        initResultList();
    }

    private void initViews(View v){
        tv_count = v.findViewById(R.id.tv_count);
        b_settings = v.findViewById(R.id.b_settings);
        b_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), CommentSettingsActivity.class));
            }
        });
        b_start_spam = v.findViewById(R.id.b_save);
        b_start_spam.setOnClickListener(startListener);

        b_stop_spam = v.findViewById(R.id.b_stop_spam);
        b_stop_spam.setOnClickListener(stopListener);

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

}
