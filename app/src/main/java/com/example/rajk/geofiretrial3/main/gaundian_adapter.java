package com.example.rajk.geofiretrial3.main;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.rajk.geofiretrial3.R;
import com.example.rajk.geofiretrial3.helper.CircleTransform;
import com.example.rajk.geofiretrial3.model.PersonalDetails;
import com.example.rajk.geofiretrial3.model.gaurdians_and_responsibilities;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static com.example.rajk.geofiretrial3.SaferIndia.DBREF;
import static com.example.rajk.geofiretrial3.SaferIndia.emergencyContact;
import static com.example.rajk.geofiretrial3.SaferIndia.users;
import static com.example.rajk.geofiretrial3.main.LoginActivity.session;

/**
 * Created by Soumya on 1/9/2018.
 */

public class gaundian_adapter extends RecyclerView.Adapter<gaundian_adapter.MyViewHolder> {
    List<gaurdians_and_responsibilities> list = new ArrayList<>();
    private Context context;
    phonebook_adapterListener listener;

    public gaundian_adapter(List<gaurdians_and_responsibilities> list, Context context, phonebook_adapterListener listener) {
        this.list = list;
        this.context = context;
        this.listener = listener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gaurdian_row, parent, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        gaurdians_and_responsibilities gar = list.get(position);

        int[] rainbow = context.getResources().getIntArray(R.array.dotinactive);
        holder.backrelative.setBackgroundColor(rainbow[position%4]);
        holder.expandnhide.setBackgroundColor(rainbow[position%4]);

        holder.contact.setText(gar.getPhone());
        if (gar.getId().equals(""))
        {
            holder.Name.setText(gar.getName());
            String caps = gar.getName().toUpperCase();
            holder.icon_text.setText(caps.charAt(0) + "");
            holder.email.setVisibility(View.GONE);
            holder.shareifnotuser.setVisibility(View.VISIBLE);
            holder.textnotuser.setVisibility(View.VISIBLE);
        }
        else
        {
            DBREF.child(users).child(gar.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists())
                    {
                        gaurdians_and_responsibilities gard = dataSnapshot.getValue(gaurdians_and_responsibilities.class);
                        holder.email.setText(gard.getEmail());
                        holder.email.setVisibility(View.VISIBLE);
                        holder.Name.setText(gard.getName());
                        applyProfilePicture(holder, gard.getImgurl());
                        holder.shareifnotuser.setVisibility(View.INVISIBLE);
                        holder.textnotuser.setVisibility(View.INVISIBLE);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        applyClickEvents(holder, position);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private void applyProfilePicture(MyViewHolder holder, String imgurl)
    {
        if(imgurl.equals("")) {
            holder.imgProfile.setImageResource(R.drawable.bg_circle);
            holder.icon_text.setVisibility(View.VISIBLE);
        }
        else
        {
            Glide.with(context).load(imgurl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .transform(new CircleTransform(context))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(holder.imgProfile);
            holder.imgProfile.setColorFilter(null);
            holder.icon_text.setVisibility(View.GONE);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView Name, icon_text, contact, email, textnotuser;
        LinearLayout employee_row;
        ImageButton callme, shareifnotuser, expand;
        ImageView imgProfile;
        RelativeLayout expandnhide, backrelative;

        public MyViewHolder(View itemView) {
            super(itemView);

            Name = (TextView) itemView.findViewById(R.id.Name);
            contact = (TextView) itemView.findViewById(R.id.contact);
            email = (TextView) itemView.findViewById(R.id.email);
            icon_text = (TextView) itemView.findViewById(R.id.icon_text);
            employee_row = (LinearLayout) itemView.findViewById(R.id.employee_row);
            imgProfile = (ImageView) itemView.findViewById(R.id.icon_profile);
            callme = (ImageButton) itemView.findViewById(R.id.callme);
            shareifnotuser = (ImageButton) itemView.findViewById(R.id.shareifnotuser);
            expand = (ImageButton) itemView.findViewById(R.id.expand);
            expandnhide = (RelativeLayout) itemView.findViewById(R.id.expandnhide);
            backrelative = (RelativeLayout) itemView.findViewById(R.id.backrelative);
            textnotuser = (TextView) itemView.findViewById(R.id.textnotuser);
        }
    }

    public interface phonebook_adapterListener {
        void onCALLMEclicked(int position);
        void onshareclicked(int position);
        void onExpandClicked(int position, MyViewHolder holder);
    }

    private void applyClickEvents(final MyViewHolder holder, final int position) {

        holder.callme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCALLMEclicked(position);
            }
        });

        holder.shareifnotuser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onshareclicked(position);
            }
        });

        holder.expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onExpandClicked(position,holder);
            }
        });

    }
}
