package com.example.rajk.geofiretrial3.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.Settings;
import com.example.rajk.geofiretrial3.model.DistanceUser;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by RajK on 19-05-2017.
 */

public class DistanceUserAdapter extends RecyclerView.Adapter<DistanceUserAdapter.MyViewHolder>
{
    SharedPreferences sharedPreferences ;
    ArrayList<DistanceUser> list = new ArrayList<>();
    private Context context;

    public DistanceUserAdapter(ArrayList<DistanceUser> list, Context context) {
        this.list = list;
        sharedPreferences = context.getSharedPreferences(Settings.Setting,MODE_PRIVATE);
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView name,distance;
        public MyViewHolder(View itemView) {
            super(itemView);
            name =(TextView) itemView.findViewById(R.id.name);
            distance =(TextView) itemView.findViewById(R.id.distance);

        }
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.distance_user_list_row,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DistanceUserAdapter.MyViewHolder holder, int position) {
        DistanceUser distanceUser = list.get(position);
        holder.name.setText(distanceUser.getName());
        holder.distance.setText(distanceUser.getDist()+" away from you");
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}
