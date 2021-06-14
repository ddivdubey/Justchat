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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.escalon.JustChat.Adapter.MessageAdapter;
import com.escalon.JustChat.Model.Chat;
import com.escalon.JustChat.Model.Users;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class MessageActivity extends AppCompatActivity {

    TextView userName;
    ImageView imageView;

    private RecyclerView recyclerViewy;
    private EditText msg_editText;
    private ImageButton sendBtn, sendFileBtn;
    private TextView sendername;
    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private Intent intent;
    private MessageAdapter messageAdapter;
    private List<Chat> mChat;

    private String stringMessage;
    private final byte encryptionKey[] = {9, 115, 51, 86, 105, 4, -31, -23, -68, 88, 17, 20, 3, -105, 119, -53};
    private Cipher cipher, decipher;
    private SecretKeySpec secretKeySpec;


    RecyclerView recyclerView;
    String userid, myurl;
    Uri fileUri;
    StorageTask uploadTask;
    private String recivername;


    String checker = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        userName = findViewById(R.id.usernamey);
        imageView = findViewById(R.id.imageview_profile);

        sendBtn = findViewById(R.id.btn_send);
        sendFileBtn = findViewById(R.id.btn_sendfile);
        msg_editText = findViewById(R.id.text_send);
//        sendername = findViewById(R.id.sendername);
//        sendername.setVisibility(View.INVISIBLE);
//        sendername.setVisibility(View.GONE);
        //Recycler View
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.getRecycledViewPool().setMaxRecycledViews(0, 0);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setTitle("");
        mToolbar.setSubtitle("");

//

//        try {
//            cipher = Cipher.getInstance("AES");
//            decipher = Cipher.getInstance("AES");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        //neccesary to be declared here
//        secretKeySpec = new SecretKeySpec(encryptionKey, "AES");


        intent = getIntent();
        userid = intent.getStringExtra("userid");

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                userName.setText(user.getUsername());
                recivername = user.getUsername();

                if (user.getImageURL().equals("default")) {
                    imageView.setImageResource(R.mipmap.ic_launcher);

                } else {
                    Glide.with(MessageActivity.this)
                            .load(user.getImageURL())
                            .into(imageView);
                }

                readMessages(firebaseUser.getUid(), userid, user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = msg_editText.getText().toString();

//                =================
//                String encrypted_message = encrytion(msg);
//              =============================
                if (!msg.equals("")) {
                    String encryptedmessage = encrytion(msg, firebaseUser.getUid(), userid);
                    sendMessage(firebaseUser.getUid(), userid, encryptedmessage, "text", "");
                } else {
                    Toast.makeText(MessageActivity.this, "Empty Message", Toast.LENGTH_SHORT).show();


                }
                msg_editText.setText("");
            }
        });


        sendFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]{
                        "Images", "PDF Files", "MS WORD File", "Videos"
                };
                AlertDialog.Builder builder = new AlertDialog.Builder(MessageActivity.this);
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

        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            fileUri = data.getData();
            if (!checker.equals("image")) {

                if (checker.equals("pdf")) {
                    String filename = getfilename(fileUri);

                    Log.d("TAG", "onActivityResult:----------------------------------------filename++++++++++++++++++++= " + filename);
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Sending");
                    progressDialog.show();
                    final StorageReference storageReference1 = FirebaseStorage.getInstance().getReference().child("PdfFiles/" + firebaseUser.getUid() + "/" + userid);
                    uploadTask = storageReference1.putFile(fileUri);

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storageReference1.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadURL = task.getResult();
                                myurl = downloadURL.toString();
                                sendMessage(firebaseUser.getUid(), userid, myurl, checker, filename);
                                progressDialog.dismiss();
                            }
                        }
                    });

                } else if (checker.equals("docx")) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Uploading");
                    progressDialog.show();
                    String filename = getfilename(fileUri);

                    Log.d("TAG", "onActivityResult:----------------------------------------filename++++++++++++++++++++= " + filename);
                    final StorageReference storageReference2 = FirebaseStorage.getInstance().getReference().child("DocxFiles/" + firebaseUser.getUid() + "/" + userid);
                    uploadTask = storageReference2.putFile(fileUri);

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storageReference2.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadURL = task.getResult();
                                myurl = downloadURL.toString();
                                sendMessage(firebaseUser.getUid(), userid, myurl, checker, filename);
                                progressDialog.dismiss();
                            }
                        }
                    });

                } else if (checker.equals("videos")) {
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Uploading");
                    progressDialog.show();
                    final StorageReference storageReference2 = FirebaseStorage.getInstance().getReference().child("VideosFiles/" + firebaseUser.getUid() + "/" + userid);
                    uploadTask = storageReference2.putFile(fileUri);

                    uploadTask.continueWithTask(new Continuation() {
                        @Override
                        public Object then(@NonNull Task task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            return storageReference2.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                Uri downloadURL = task.getResult();
                                myurl = downloadURL.toString();
                                sendMessage(firebaseUser.getUid(), userid, myurl, checker, "");
                                progressDialog.dismiss();
                            }
                        }
                    });

                }


            } else if (checker.equals("image")) {
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("ImageFiles/" + firebaseUser.getUid() + "/" + userid);
                uploadTask = storageReference.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return storageReference.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadURL = task.getResult();
                            myurl = downloadURL.toString();
                            sendMessage(firebaseUser.getUid(), userid, myurl, "image", "");

                        }
                    }
                });

            } else {
                Toast.makeText(MessageActivity.this, "Nothing Selected", Toast.LENGTH_LONG).show();
            }


        }
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
        } else {
        }
        return displayName;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.message_menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.text_translation:
                startActivity(new Intent(this, TranslateLanguage.class));
                return true;

        }
        return false;
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

    private String encrytion(String msg, String sender, String reciver) {
        String returnEncryptedString = null;
//      Salt creation
        String salt = getSaltString(sender, reciver);
        byte[] b = salt.getBytes();

//		Printing byte array for reference
        System.out.println("INTKEY:");

        for (int i = 0; i < b.length; i++) {
            System.out.print(b[i] + " ");

        }
        System.out.println();
//		Enc Calls
        secretKeySpec = new SecretKeySpec(b, "AES");
        byte[] stringByte = msg.getBytes();
        byte[] encryptedByte = new byte[stringByte.length];
        try {
            cipher = Cipher.getInstance("AES");
//            decipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            encryptedByte = cipher.doFinal(stringByte);

            returnEncryptedString = new String(encryptedByte, "ISO-8859-1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("MESSAGE::::::::::::::::::::::" + returnEncryptedString);
        return returnEncryptedString;
    }

    private void sendMessage(String sender, String reciever, String message, String type, String filename) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("reciever", reciever);
        hashMap.put("message", message);
        hashMap.put("type", type);

        reference.child("Chats").push().setValue(hashMap);


        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(firebaseUser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    chatRef.child("id").setValue(userid);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void readMessages(final String myid, final String userid, final String imageURL) {
        mChat = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Chat chat = dataSnapshot.getValue(Chat.class);
                    if (chat.getReciever().equals(myid) && chat.getSender().equals(userid) || chat.getReciever().equals(userid) && chat.getSender().equals(myid)) {
                        mChat.add(chat);
                    }
//                    String type = chat.getMsgtype();
//                    Log.d("TAG", "onDataChange: ------------------------------------------------in Activity"+type);
                    chat.setMessageid(dataSnapshot.getKey());
                    messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageURL, "", recivername);
//                    recyclerView.smoothScrollToPosition(mChat.size()-1);
                    recyclerView.setAdapter(messageAdapter);
                    messageAdapter.notifyDataSetChanged();
//                    recyclerView.smoothScrollToPosition(mChat.size());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}