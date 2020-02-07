package com.example.spammer.Groups;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.spammer.Models.Result;
import com.example.spammer.R;

import java.util.ArrayList;

public class GroupResultListAdapter extends RecyclerView.Adapter<GroupResultListAdapter.ViewHolder> {
    private ArrayList<Result> result_list = new ArrayList<>();
    private Context context;

    public GroupResultListAdapter(ArrayList<Result> result_list, Context context) {
        this.result_list = result_list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String link = "https://vk.com/public"+result_list.get(position).getGroupId()
                +"?w=wall-"
                +result_list.get(position).getGroupId()
                +"_" + result_list.get(position).getPostNumber();

        holder.tv_result.setText(result_list.get(position).getResult());
        holder.tv_link.setText(link);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent notificationIntent = new Intent(Intent.ACTION_VIEW);
                notificationIntent.setData(Uri.parse(link));
                context.startActivity(notificationIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return result_list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView tv_result;
        private TextView tv_link;

        private ViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_link = itemView.findViewById(R.id.tv_post);
            tv_result = itemView.findViewById(R.id.tv_result);
        }
    }
}
