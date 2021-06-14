package com.escalon.JustChat.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
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
import com.escalon.JustChat.Model.Chat;
import com.escalon.JustChat.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private Context context;
    private List<Chat> mChat;
    private String imgURL;
    //private boolean isChat;
    FirebaseDatabase firebaseDatabase;

    FirebaseUser firebaseUser;
    private Intent intent;
    //    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    public static final int MSG_TYPE_LEFT = 0;
    public static final int MSG_TYPE_RIGHT = 1;

    //     These can not be implemented in another class and thus has to be safe
    private final byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;
    private String recievername;


    // Constructor
    public MessageAdapter(Context context, List<Chat> mChat, String imgURL, String type, String recivername) {
        this.context = context;
        this.mChat = mChat;
        this.imgURL = imgURL;
        this.recievername = recivername;

    }


    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_TYPE_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,
                    parent,
                    false);
            return new MessageAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,
                    parent,
                    false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        Chat chat = mChat.get(position);
        Log.d("TAG", "{}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}}data Recieved--------------: " + chat.getReciever() + "+++++++" + chat.getSender());
        holder.show_message.setText(decryption(chat.getMessage(), chat.getSender(), chat.getReciever()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context).setTitle("Delete")
                        .setMessage("Delete Message For Everyone")
                        .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
                                    firebaseDatabase.getInstance()
                                            .getReference().child("Chats")
                                            .child(mChat.get(position).getMessageid())
                                            .setValue(null);
                                    System.out.println(mChat.get(position).getMessageid());
                                    Toast.makeText(context, " Message Delete", Toast.LENGTH_SHORT).show();
                                    notifyDataSetChanged();
                                } else
                                    Toast.makeText(context, "Delete Failed", Toast.LENGTH_SHORT).show();
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

            }
        });
        if (imgURL.equals("default")) {
            holder.profile_image.setImageResource(R.drawable.ic_person_24_grey);
//            holder.sendername.setText(recievername);
        } else {
            Glide.with(context).load(imgURL).into(holder.profile_image);
//            holder.sendername.setText(recievername);
        }

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
        } else {
            Log.d("TAG", "onBindViewHolder:-----------------------------------------Text");
            holder.imageview_msg.setVisibility(View.INVISIBLE);
            holder.imageview_msg.setVisibility(View.GONE);
            holder.imageview_docs.setVisibility(View.INVISIBLE);
            holder.imageview_docs.setVisibility(View.GONE);
            holder.show_message.setText(decryption(chat.getMessage(), chat.getSender(), chat.getReciever()));

        }


    }

    private static String getSaltString(String sender, String reciver) {
//								   0   1    2   3   4   5   6    7    8   9  10   11   12   13   14   15
        final byte[] intkey = {9, 115, 51, 86, 105, 4, -31, 13, -47, 87, 17, -20, -11, -105, 119, -53};
        String SALT;
        String charkey = "";
        for (int i = 8; i < 12; i++) {
            charkey = charkey + "" + (char) intkey[i];
        }
        System.out.println("CHARKEY: " + charkey);
        String A1 = sender.substring(23, 27);
        String A2 = reciver.substring(0, 4);
        SALT = A1 + "#^" + charkey + "^#" + A2;
        System.out.println("SALT:" + SALT);
        return SALT;
    }

    private String decryption(String message, String Sender, String Reciever) {
        System.out.println("ADAPTER SENDER:====" + Sender);
        System.out.println("ADAPTER RECIEVER:====" + Reciever);
        System.out.println("ADAPTER MESSAGE:====" + message);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("ChatList").child(firebaseUser.getUid());
        List l = new ArrayList();
        System.out.println("DECRYPTION");
//	    get salt
        String salt = getSaltString(Sender, Reciever);
        byte[] b = salt.getBytes();


//		Print byte array
        System.out.println("INTKEY:");

        for (int i = 0; i < b.length; i++) {
            System.out.print(b[i] + " ");

        }

        System.out.println();

//		Dec Call
        secretKeySpec = new SecretKeySpec(b, "AES");
        byte[] decryption;
        String decryptedString = null;
        try {
            byte[] EncryptedByte = message.getBytes("ISO-8859-1");
            decipher = Cipher.getInstance("AES");
            decipher.init(cipher.DECRYPT_MODE, secretKeySpec);
            decryption = decipher.doFinal(EncryptedByte);
            decryptedString = new String(decryption);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("TAG", "-------------------DECRYPTED MESSAGES--------------------------: " + decryptedString);
        return decryptedString;
    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    private byte[] getEncryptionKeySpec() {
        return encryptionKey;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView show_message, txt_docsfilename, sendername;
        public CircleImageView profile_image;
        public ImageView imageview_msg, imageview_docs;
        private LinearLayout linearLayoutleft;
        RecyclerView recyclerView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            imageview_msg = itemView.findViewById(R.id.imageview_msg);
            imageview_docs = itemView.findViewById(R.id.imageview_docs);
            recyclerView = itemView.findViewById(R.id.recycler_view);
            linearLayoutleft = itemView.findViewById(R.id.linearlayoutleft);
            sendername = itemView.findViewById(R.id.sendername);


            // recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        }
    }

    @Override
    public int getItemViewType(int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mChat.get(position).getSender().equals(firebaseUser.getUid())) {
            return MSG_TYPE_RIGHT;
        } else {
            return MSG_TYPE_LEFT;
        }
    }


}
