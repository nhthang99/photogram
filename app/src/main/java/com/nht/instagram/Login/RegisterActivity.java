package com.nht.instagram.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nht.instagram.Models.User;
import com.nht.instagram.R;
import com.nht.instagram.Utils.FirebaseMethods;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mRef;

    private Button btnRegister;
    private String email, password, username;
    private String append = "";
    private Context mContext = RegisterActivity.this;
    private ProgressBar mProgressBar;
    private EditText mEmail, mPassword, mUsername;
    private CheckBox cbShowPassword;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Log.d(TAG, "onCreate: starting.");
        firebaseMethods = new FirebaseMethods(mContext);
        btnRegister = (Button)findViewById(R.id.btn_register);
        mEmail = (EditText)findViewById(R.id.input_email);
        mPassword = (EditText)findViewById(R.id.input_password);
        mUsername = (EditText)findViewById(R.id.input_username);
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        setupFirebaseAuth();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerNewUser();
            }
        });

        cbShowPassword = (CheckBox)findViewById(R.id.showPassword);
        cbShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!isChecked){
                    mPassword.setTransformationMethod(new PasswordTransformationMethod());
                }else{
                    mPassword.setTransformationMethod(null);
                }
            }
        });

    }

    private void registerNewUser(){
        mProgressBar.setVisibility(View.VISIBLE);
        email = mEmail.getText().toString();
        password = mPassword.getText().toString();
        username = mUsername.getText().toString();

        if (checkInputs(email, password, username)){
            firebaseMethods.registerNewEmail(email, password, username);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private boolean checkInputs(String email, String password, String username){
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            return false;
        }
        if(TextUtils.isEmpty(username)){
            Toast.makeText(this, "Please enter your name...", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            return false;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show();
            mProgressBar.setVisibility(View.GONE);
            return false;
        }
        return true;
    }

    private void checkIfUserameExist(final String username) {
        Log.d(TAG, "checkIfUserameExist: Checking if " + username + "already exist");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query = reference
                .child(mContext.getString(R.string.db_users))
                .child(mContext.getString(R.string.field_username))
                .equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    //add the username
                }
                for (DataSnapshot signleSnapshot: dataSnapshot.getChildren()){
                    if (signleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: checkIfUserameExist: found a match " + signleSnapshot.getValue(User.class).getUsername());
                        append = mRef.push().getKey().substring(3, 10);
                        Log.d(TAG, "onDataChange: user already exist. Appending random string to name: " + append);
                    }
                }

                String mUsername;
                mUsername = username + append;
                // Add new user to the database
                firebaseMethods.addNewUser(email, mUsername, "", "");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebase: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mRef = mFirebaseDatabase.getReference();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: sign in: " + user.getUid());

                    mRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            checkIfUserameExist(username);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    Log.d(TAG, "onAuthStateChanged: sign out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}