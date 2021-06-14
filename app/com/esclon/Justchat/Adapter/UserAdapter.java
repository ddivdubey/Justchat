package com.escalon.JustChat.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.escalon.JustChat.MessageActivity;
import com.escalon.JustChat.Model.Users;
import com.escalon.JustChat.R;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<Users> mUsers;
    private List<Users> mUsersfilter;
    //private boolean isChat;


    // Constructor
    public UserAdapter(Context context, List<Users> mUsers) {
        this.context = context;
        this.mUsers = mUsers;
        this.mUsersfilter = mUsers;

//replace musers with musersfilter
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,
                parent,
                false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {

        final Users users = mUsersfilter.get(position);
        holder.username.setText(users.getUsername());

        if (users.getImageURL().equals("default")){
            holder.imageView.setImageResource(R.mipmap.ic_launcher_custom_round);
        }else{
            // Adding Glide Library
            Glide.with(context)
                    .load(users.getImageURL())
                    .into(holder.imageView);
        }



        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, MessageActivity.class);
                i.putExtra("userid", users.getId());
                context.startActivity(i);
            }
        });









    }

    @Override
    public int getItemCount() {
        return mUsersfilter.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String sequence = charSequence.toString();
                if (sequence.isEmpty()){
                    mUsersfilter = mUsers;
                }
                else {
                    List<Users> filteredlist = new ArrayList<>();
                    for(Users filteredusers:mUsers){
                        if(filteredusers.getUsername().toLowerCase().contains(sequence.toLowerCase())){
                            filteredlist.add(filteredusers);
                        }
                    }
                    mUsersfilter = filteredlist;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mUsersfilter;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mUsersfilter = (ArrayList<Users>)filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public CircleImageView imageView;
        //public ImageView imageViewON;
        //public ImageView imageViewOFF;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.textView30);
            imageView = itemView.findViewById(R.id.imageView);



        }
    }





}
