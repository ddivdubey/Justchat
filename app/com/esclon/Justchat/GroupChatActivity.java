package com.escalon.JustChat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.escalon.JustChat.Adapter.GroupMessageAdapter;
import com.escalon.JustChat.Model.GroupChat;
import com.escalon.JustChat.Model.Users;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GroupChatActivity extends AppCompatActivity {
    EditText sendtext_edittext;
    ImageButton selectfile, sendbtn;
    RecyclerView recyclerView_group;
    RelativeLayout bottom_group;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference, reference1;
    String checker = "",myurl;
    private Intent intent;
    private GroupMessageAdapter groupmessageAdapter;
    private List<GroupChat> mChat;
    private String username_toset, imageurl_toset;
    TextView username_Temp;
    Uri fileUri;
    StorageTask uploadTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);
        sendtext_edittext = findViewById(R.id.group_text_send);
        sendbtn = findViewById(R.id.btn_send_group);
        selectfile = findViewById(R.id.btn_sendfile_group);
        //recyclerview
        recyclerView_group = findViewById(R.id.recycler_view_group);
        recyclerView_group.setHasFixedSize(true);
        recyclerView_group.getRecycledViewPool().setMaxRecycledViews(0, 0);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView_group.setLayoutManager(linearLayoutManager);
        username_Temp = findViewById(R.id.sendername);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar3_group);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle("");
        mToolbar.setSubtitle("");
        bottom_group = findViewById(R.id.bottom_group);

        intent = getIntent();
        String userid = intent.getStringExtra("userid");
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        reference = FirebaseDatabase.getInstance().getReference("GroupChat");
        reference1 = FirebaseDatabase.getInstance().getReference("MyUsers");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    final Users user = snapshot.getValue(Users.class);
                    assert user != null;
                    if (user.getId().equals(firebaseUser.getUid())) {
                            username_toset = user.getUsername();
                            imageurl_toset = user.getImageURL();
                    }

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //addition
                System.out.println();
                readMessages(firebaseUser.getUid());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }

        });


        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = sendtext_edittext.getText().toString();
                if (message.isEmpty())
                    Toast.makeText(GroupChatActivity.this, "Please write a message", Toast.LENGTH_SHORT).show();
                else
                    sendMessage(firebaseUser.getUid(), message, "text", username_toset, imageurl_toset);
                sendtext_edittext.setText("");
            }
        });

        selectfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images", "PDF Files", "MS WORD File", "Videos"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(GroupChatActivity.this);
                builder.setTitle("Select The File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if (which == 1) {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select Pdf File"), 438);

                        }
                        if (which == 2) {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MSWord File"), 438);
                        }
                        if (which == 3) {
                            checker = "videos";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("video/*");
                            startActivityForResult(intent.createChooser(intent, "Select Video"), 438);
                        }

                    }
                });

                builder.show();


            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 438 && resultCode==RESULT_OK && data!= null && data.getData()!=null){

            fileUri = data.getData();
            if(!checker.equals("image")){

                if(checker.equals("pdf")){
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Sending");
                    progressDialog.show();
                    final StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child("GroupFiles/PdfFiles"+firebaseUser.getUid()+"/");
                    uploadTask = storageReference1.putFile(fileUri);

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return storageReference1.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful())
                            {
                                Uri downloadURL = task.getResult();
                                myurl = downloadURL.toString();
                                sendMessage(firebaseUser.getUid(),myurl,"pdf", username_toset,imageurl_toset);
                                progressDialog.dismiss();
                            }
                        }
                    });

                }
                else if(checker.equals("docx")){
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Uploading");
                    progressDialog.show();
                    String filename = getfilename(fileUri);

                    Log.d("TAG", "onActivityResult:----------------------------------------filename++++++++++++++++++++= "+filename);
                    final StorageReference storageReference2 = FirebaseStorage.getInstance().getReference().child("GroupFiles/DocxFiles"+firebaseUser.getUid()+"/");
                    uploadTask = storageReference2.putFile(fileUri);

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return storageReference2.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful())
                            {
                                Uri downloadURL = task.getResult();
                                myurl = downloadURL.toString();
                                sendMessage(firebaseUser.getUid(),myurl,"docx", username_toset,imageurl_toset);
                                progressDialog.dismiss();
                            }
                        }
                    });

                }
                else if(checker.equals("videos")){
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Uploading");
                    progressDialog.show();
                    final StorageReference storageReference2 = FirebaseStorage.getInstance().getReference().child("GroupFiles/VideosFiles"+firebaseUser.getUid()+"/");
                    uploadTask = storageReference2.putFile(fileUri);

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if(!task.isSuccessful()){
                                throw task.getException();
                            }
                            return storageReference2.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful())
                            {
                                Uri downloadURL = task.getResult();
                                myurl = downloadURL.toString();
                                sendMessage(firebaseUser.getUid(),myurl,"videos", username_toset,imageurl_toset);
                                progressDialog.dismiss();
                            }
                        }
                    });

                }


            }
            else if(checker.equals("image")){
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("GroupFiles/imagefiles"+firebaseUser.getUid()+"/");
                uploadTask = storageReference.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful()){
                            throw task.getException();
                        }
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful())
                        {
                            Uri downloadURL = task.getResult();
                            myurl = downloadURL.toString();
                            sendMessage(firebaseUser.getUid(),myurl, "image",username_toset,imageurl_toset);

                        }
                    }
                });

            }
            else{
                Toast.makeText(GroupChatActivity.this,"Nothing Selected",Toast.LENGTH_LONG).show();
            }



        }
    }

    private void sendMessage(String sender, String message, String type, String username, String imageurl) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("message", message);
        hashMap.put("type", type);
        hashMap.put("username", username);
        hashMap.put("imageurl", imageurl);


//        reference.child("Chats").push().setValue(hashMap);
        reference.child("GroupChats").push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                groupmessageAdapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(GroupChatActivity.this, "Unable To Send Message ", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void readMessages(final String myid) {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("GroupChats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    GroupChat chat = dataSnapshot.getValue(GroupChat.class);
                    mChat.add(chat);
//                    Log.d("TAG", "onDataChange:============= " + dataSnapshot.getKey());
                    chat.setMessageid(dataSnapshot.getKey());
                    groupmessageAdapter = new GroupMessageAdapter(GroupChatActivity.this, mChat);
                    recyclerView_group.setAdapter(groupmessageAdapter);
                    groupmessageAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private String getfilename(Uri fileUri) {
//        Uri uri = data.getData();
        String uriString = fileUri.toString();
        File myFile = new File(uriString);
        String path = myFile.getAbsolutePath();
        String displayName = null;

        if (uriString.startsWith("content://")) {
            Cursor cursor = null;
            try {
                cursor = this.getContentResolver().query(fileUri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        } else if (uriString.startsWith("file://")) {
            displayName = myFile.getName();
        }
        else {
        }
        return displayName;
    }
}




