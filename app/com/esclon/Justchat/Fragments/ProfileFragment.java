package com.escalon.JustChat.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.escalon.JustChat.Model.Users;
import com.escalon.JustChat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import static android.app.Activity.RESULT_OK;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 */
public class ProfileFragment extends Fragment {

    TextView username;
    CircleImageView imageView;
//    ImageView imageView;

    DatabaseReference reference;
    FirebaseUser fuser;


    // Profile Image
    StorageReference storageReference;
    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        imageView = (CircleImageView) view.findViewById(R.id.profile_image2);
        username  = view.findViewById(R.id.usernamer);



        // Profile Image reference in storage
        storageReference = FirebaseStorage.getInstance().getReference("uploads");




        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("MyUsers")
                .child(fuser.getUid());



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Users user = dataSnapshot.getValue(Users.class);
                username.setText(user.getUsername());

                if (user.getImageURL().equals("default")){
                    imageView.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(getContext()).load(user.getImageURL()).into(imageView);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();
            }
        });

        username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText name = new EditText(view.getContext());
                androidx.appcompat.app.AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Change Profile Name");
                builder.setMessage("Enter Your Name");
                builder.setIcon(R.drawable.ic_baseline_perm_identity_24);
//                builder.setView(R.drawable.ic_baseline_contacts_24);
                builder.setView(name);
                builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String new_name = name.getText().toString();
                        DatabaseReference reference1 =FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());
                        if(TextUtils.isEmpty(new_name)){
                            Toast.makeText(getContext(), "Please add a name", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            if(changename(new_name)){
                                username.setText(new_name);
                                Toast.makeText(getContext(), "Username Updated Successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(getContext(), "Error cannot change name", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });


                builder.create().show();
            }
        });


        return view;
    }

    private boolean changename(final String newname){
          reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid()).child("username");
          reference.addValueEventListener(new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                  String username_old = (String) snapshot.getValue();
                  reference.setValue(newname);

              }

              @Override
              public void onCancelled(@NonNull DatabaseError error) {

              }
          });
        return true;
    }


    private void SelectImage() {

        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(i, IMAGE_REQUEST);

    }


    private String getFileExtention(Uri uri){


        ContentResolver contentResolver =getContext().getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }


    private void UploadMyImage(){


        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading");
        progressDialog.show();

        if(imageUri != null) {
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    + "." + getFileExtention(imageUri));


            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation <UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {

                    if (!task.isSuccessful()){

                        throw  task.getException();
                    }

                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()){

                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());


                        HashMap<String, Object> map = new HashMap<>();

                        map.put("imageURL", mUri);
                        reference.updateChildren(map);

                        progressDialog.dismiss();
                    }else{

                        Toast.makeText(getContext(), "Failed!!", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }
            });


        }else
        {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == IMAGE_REQUEST &&  resultCode == RESULT_OK
                && data != null && data.getData() != null){


            imageUri = data.getData();


            if (uploadTask != null && uploadTask.isInProgress()){

                Toast.makeText(getContext(), "Upload in progress..", Toast.LENGTH_SHORT).show();



            }else {

                UploadMyImage();
            }


        }
    }
}

