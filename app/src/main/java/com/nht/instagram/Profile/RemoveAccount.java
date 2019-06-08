package com.nht.instagram.Profile;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nht.instagram.Models.UserAccountSetting;
import com.nht.instagram.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class RemoveAccount extends Fragment {
    private static final String TAG = "RemoveAccount";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private ProgressBar mProgressBar;
    private EditText mPassword, mPasswordVerify;
    private TextView mUsername;
    private CircleImageView mProfilePhoto;
    private ImageView mBack;
    private Button mConfirm;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_remove_account, container, false);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mPassword = (EditText)view.findViewById(R.id.input_password);
        mPasswordVerify = (EditText)view.findViewById(R.id.input_password_verify);
        mConfirm = (Button)view.findViewById(R.id.saveChanges);
        mUsername = (TextView)view.findViewById(R.id.username);
        mProfilePhoto = (CircleImageView)view.findViewById(R.id.profile_photo);
        mBack = (ImageView)view.findViewById(R.id.backArrow);
        mProgressBar.setVisibility(GONE);

        setupFirebaseAuth();
        getInfoUser();

        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                removeAccount();
            }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        return view;
    }

    private void getInfoUser(){
//        Log.d(TAG, "getInfoUser: " + mAuth.getCurrentUser().getUid());
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Query query = reference
                .child(getString(R.string.db_user_account_settings))
                .orderByChild(getString(R.string.field_user_id))
                .equalTo(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds: dataSnapshot.getChildren()){
                    try{
                        UserAccountSetting setting = ds.getValue(UserAccountSetting.class);
                        mUsername.setText(setting.getUsername());
                        Glide.with(getContext()).load(setting.getProfile_photo()).into(mProfilePhoto);
                    }catch (NullPointerException e){
                        Log.e(TAG, "onDataChange: NullPointerException" + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeAccount(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String password = mPassword.getText().toString();
        final String password_verify = mPasswordVerify.getText().toString();
        if(TextUtils.isEmpty(password)){
            mProgressBar.setVisibility(GONE);
            Toast.makeText(getActivity(), "Please enter current password", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(TextUtils.isEmpty(password_verify) || !password.equals(password_verify)){
            mProgressBar.setVisibility(GONE);
            Toast.makeText(getActivity(), "Password verify does not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (user != null){
            try{
                String email = mAuth.getCurrentUser().getEmail();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, password);
                // Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d(TAG, "User re-authenticated. success");
                                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    Log.d(TAG, "onComplete: account " + user.getUid() + " deleted");
                                                    mProgressBar.setVisibility(GONE);
                                                    removeUserInfo(user.getUid());
                                                    mAuth.signOut();
                                               }else{
                                                    Log.d(TAG, "onComplete: Something was wrong");
                                                }
                                            }
                                        });
                                }else{
                                    Log.d(TAG, "User re-authenticated. failed");
                                    Toast.makeText(getActivity(), "Your password is invalid", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }catch (NullPointerException e){
                Log.e(TAG, "changePassword: "  + e.getMessage());
            }
        }
    }

    private void removeUserInfo(final String userID){
        final DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final ArrayList<String> mListFollowing = new ArrayList<>();
        final ArrayList<String> mListFollowers = new ArrayList<>();

        reference
                .child(getString(R.string.db_users))
                .child(userID)
                .removeValue();

        reference
                .child(getString(R.string.db_user_account_settings))
                .child(userID)
                .removeValue();

        Query query = reference
                .child(getString(R.string.dbname_following))
                .child(userID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: singleSnapshotParent " + singleSnapshot.getKey());
                    Query query1 = reference
                            .child(getString(R.string.dbname_followers))
                            .child(singleSnapshot.getKey());
                    query1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                if(singleSnapshot.getKey().equals(userID)){
                                    Log.d(TAG, "onDataChange: singleSnapshot " + singleSnapshot.getKey());
                                    singleSnapshot.getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        Query query2 = reference
                .child(getString(R.string.dbname_followers))
                .child(userID);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot: dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: singleSnapshotParent " + singleSnapshot.getKey());
                    Query query3 = reference
                            .child(getString(R.string.dbname_following))
                            .child(singleSnapshot.getKey());
                    query3.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                                if(singleSnapshot.getKey().equals(userID)){
                                    Log.d(TAG, "onDataChange: singleSnapshot " + singleSnapshot.getKey());
                                    singleSnapshot.getRef().removeValue();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        reference
                .child(getString(R.string.dbname_followers))
                .child(userID)
                .removeValue();
        reference
                .child(getString(R.string.dbname_following))
                .child(userID)
                .removeValue();
        reference
                .child(getString(R.string.db_users_photo))
                .child(userID)
                .removeValue();

    }

    /*
    --------------------------------------- Firebase Authentication -------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebase: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if(user != null){
                    Log.d(TAG, "onAuthStateChanged: sign in: " + user.getUid());
                }
                else{
                    Log.d(TAG, "onAuthStateChanged: sign out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
