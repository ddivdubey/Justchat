package com.escalon.JustChat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 80;
    // Widgets
    EditText userET, passET, emailET;
    Button registerBtn;
    CheckBox showpassword;
    // Firebase
    FirebaseAuth auth;
    DatabaseReference myRef;
    ImageButton google_btn;
    GoogleSignInClient googleSignInClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initializing Widgets:
        userET = findViewById(R.id.username);
        passET = findViewById(R.id.password);
        emailET = findViewById(R.id.email);
        registerBtn = findViewById(R.id.btn_register);
        showpassword = findViewById(R.id.show_password);


        // Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Adding Event Listener to Button Register
        registerBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {

                String username_text = userET.getText().toString();
                String email_text    = emailET.getText().toString();
                String pass_text     = passET.getText().toString();

                if (TextUtils.isEmpty(username_text) || TextUtils.isEmpty(email_text) || TextUtils.isEmpty(pass_text)){
                    Toast.makeText(RegisterActivity.this, "Please Fill All Fields", Toast.LENGTH_SHORT).show();
                }else{
                    RegisterNow(username_text, email_text , pass_text );
                }
            }
        });

        showpassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    passET.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else {
                    passET.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });




    }







    private void RegisterNow(final String username, String email, String password){
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if(task.getResult().getSignInMethods().isEmpty()) {
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = auth.getCurrentUser();
                                        String userid = firebaseUser.getUid();

                                        myRef = FirebaseDatabase.getInstance().getReference("MyUsers")
                                                .child(userid);

                                        // HashMaps
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put("id", userid);
                                        hashMap.put("username", username);
                                        hashMap.put("imageURL", "default");
                                        //hashMap.put("status" , "offline");


                                        // Opening the Main Activity after Success Registration
                                        myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {
                                                    auth.getCurrentUser().sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            Intent i = new Intent(RegisterActivity.this, Verify.class);
                                                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                            startActivity(i);
                                                            finish();
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {

                                                        }
                                                    });

                                                }

                                            }
                                        });


                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
                                    }


                                }
                            });
                }
                else{

                Toast.makeText(RegisterActivity.this, "This Email address is already in use please Try to register with another email", Toast.LENGTH_SHORT).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Error in Registering", Toast.LENGTH_SHORT).show();
            }
        });

    }


}

