package com.escalon.JustChat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.escalon.JustChat.Model.GroupChat;
import com.escalon.JustChat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class GroupMessageAdapter extends RecyclerView.Adapter<GroupMessageAdapter.ViewHolder> {

    private Context context;
    private List<GroupChat> mChat;
    private String imgURL;
    //private boolean isChat;

    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    private Intent intent;
    //    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;
    String sendername;



    // Constructor
    public GroupMessageAdapter(Context context, List<GroupChat> mChat) {
        this.context = context;
        this.mChat = mChat;
//        Log.d("TAG", "GroupMessageAdapter: "+sendername);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,
                    parent,
                    false);
            return new GroupMessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,
                    parent,
                    false);
            return new GroupMessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        GroupChat chat = mChat.get(position);
        holder.show_message.setText(chat.getMessage());
//        Log.d("TAG", "onBindViewHolder: -----------" + chat.getMessage());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(context).setTitle("Delete")
                            .setMessage("Delete Message For Everyone")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    System.out.println(mChat.get(position).getSender()+"===="+firebaseUser.getUid());
                                    if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
                                        firebaseDatabase.getInstance()
                                                .getReference().child("GroupChats")
                                                .child(mChat.get(position).getMessageid())
                                                .setValue(null);
                                        System.out.println(mChat.get(position).getMessageid());
                                        Toast.makeText(context, " Message Delete", Toast.LENGTH_SHORT).show();
                                        notifyDataSetChanged();
                                    } else
                                        Toast.makeText(context, "Delete fail", Toast.LENGTH_SHORT).show();
                                }
                            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();

                }
            });


        boolean url = URLUtil.isValidUrl(chat.getMessage());
        String msg = chat.getMessage();
//

        if (url && msg.toLowerCase().contains("imagefiles")) {

            holder.show_message.setVisibility(View.INVISIBLE);
            holder.show_message.setVisibility(View.GONE);
            holder.imageview_docs.setVisibility(View.INVISIBLE);
            holder.imageview_docs.setVisibility(View.GONE);

            Glide.with(context).load(chat.getMessage()).into(holder.imageview_msg);
            holder.imageview_msg.setLongClickable(true);
            holder.imageview_msg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mChat.get(position).getMessage()));
                    holder.itemView.getContext().startActivity(intent);
                    return true;
                }
            });

        } else if (url && msg.toLowerCase().contains("PdfFiles".toLowerCase())) {
            holder.show_message.setVisibility(View.INVISIBLE);
            holder.show_message.setVisibility(View.GONE);
            holder.imageview_msg.setVisibility(View.INVISIBLE);
            holder.imageview_msg.setVisibility(View.GONE);

            holder.imageview_docs.setBackgroundResource(R.drawable.ic_pdf_file);
            holder.imageview_docs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mChat.get(position).getMessage()));
                    holder.itemView.getContext().startActivity(intent);
                }
            });

        } else if (url && msg.toLowerCase().contains("DocxFiles".toLowerCase())) {
            holder.show_message.setVisibility(View.INVISIBLE);
            holder.show_message.setVisibility(View.GONE);
            holder.imageview_msg.setVisibility(View.INVISIBLE);
            holder.imageview_msg.setVisibility(View.GONE);
            holder.imageview_docs.setBackgroundResource(R.drawable.ic_word);
            holder.imageview_docs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mChat.get(position).getMessage()));
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        } else if (url && msg.toLowerCase().contains("VideosFiles".toLowerCase())) {
            holder.show_message.setVisibility(View.INVISIBLE);
            holder.show_message.setVisibility(View.GONE);
            holder.imageview_docs.setVisibility(View.INVISIBLE);
            holder.imageview_docs.setVisibility(View.GONE);

            RequestOptions requestOptions = new RequestOptions();
            requestOptions.isMemoryCacheable();
            Glide.with(context).setDefaultRequestOptions(requestOptions).load(chat.getMessage()).into(holder.imageview_msg);
            holder.imageview_msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mChat.get(position).getMessage()));
                    holder.itemView.getContext().startActivity(intent);
                }
            });
        }
        else {
//        Log.d("TAG", "onBindViewHolder:-----------------------------------------Text");
            holder.imageview_msg.setVisibility(View.INVISIBLE);
            holder.imageview_msg.setVisibility(View.GONE);
            holder.imageview_docs.setVisibility(View.INVISIBLE);
            holder.imageview_docs.setVisibility(View.GONE);
        }
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String user = firebaseUser.getUid();
        String sender = mChat.get(position).getSender();
        sendername = mChat.get(position).getUsername();
        imgURL = mChat.get(position).getImageurl();
        if (!user.equals(sender)){
//            Log.d("TAG", user+" onBindViewHolder:-----------------------------------------Text "+sender);
            holder.sendername.setText(sendername);
            if(imgURL.equals("default"))
                holder.profile_image.setImageResource(R.drawable.ic_person_24_grey);
            else
                Glide.with(context).load(imgURL).into(holder.profile_image);

        }
        holder.show_message.setText(chat.getMessage());
    }


//    private String decryption(String message) {
//    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, txt_docsfilename,sendername;
        public CircleImageView profile_image;
        public ImageView imageview_msg, imageview_docs;
        private LinearLayout layout_docs;
        RecyclerView recyclerView_group;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            sendername = itemView.findViewById(R.id.sendername);
            profile_image = itemView.findViewById(R.id.profile_image);
            imageview_msg = itemView.findViewById(R.id.imageview_msg);
            imageview_docs = itemView.findViewById(R.id.imageview_docs);
            recyclerView_group = itemView.findViewById(R.id.recycler_view_group);



        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        Log.d("TAG", "getItemViewType: " + firebaseUser.getUid());
        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {

            return MSG_TYPE_LEFT;
        }
    }


}
