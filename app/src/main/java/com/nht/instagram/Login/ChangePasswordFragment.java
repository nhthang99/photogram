package com.nht.instagram.Login;

import android.content.Intent;
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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.nht.instagram.R;

import java.util.Objects;

import static android.view.View.GONE;

public class ChangePasswordFragment extends Fragment {

    private static final String TAG = "ChangePasswordFragment";

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private ProgressBar mProgressBar;
    private EditText mCurrentPass, mNewPass, mRetypeNewPass;
    private Button saveChanges;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_change_password, container, false);

        mAuth = FirebaseAuth.getInstance();

        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        mCurrentPass = (EditText) view.findViewById(R.id.input_password_curr);
        mNewPass = (EditText)view.findViewById(R.id.input_new_password);
        mRetypeNewPass =(EditText) view.findViewById(R.id.input_new_password_verify);
        saveChanges = (Button)view.findViewById(R.id.saveChanges);

        mProgressBar.setVisibility(GONE);

        setupFirebaseAuth();
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                changePassword();
            }
        });

        return view;
    }


    private void changePassword(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String curr_password = mCurrentPass.getText().toString();
        final String new_password = mNewPass.getText().toString();
        final String retype_new_password = mRetypeNewPass.getText().toString();

        if(TextUtils.isEmpty(curr_password)){
            mProgressBar.setVisibility(GONE);
            Toast.makeText(getActivity(), "Please enter current password", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(TextUtils.isEmpty(new_password)){
            mProgressBar.setVisibility(GONE);
            Toast.makeText(getActivity(), "Please enter new password", Toast.LENGTH_SHORT).show();
            return;
        }
        else if(TextUtils.isEmpty(retype_new_password)){
            mProgressBar.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "Re-type new password does not match", Toast.LENGTH_SHORT).show();
            return;
        }
        if (user != null && mCurrentPass != null){
            try{
                String email = mAuth.getCurrentUser().getEmail();
                AuthCredential credential = EmailAuthProvider
                        .getCredential(email, curr_password);
                // Prompt the user to re-provide their sign-in credentials
                user.reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Log.d(TAG, "User re-authenticated. success");
                                    if (new_password.equals(retype_new_password)){
                                        user.updatePassword(new_password)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            Log.d(TAG, "onComplete: Password updated");
                                                            mProgressBar.setVisibility(GONE);
                                                            Toast.makeText(getActivity(), "Your password has been changed", Toast.LENGTH_SHORT).show();
                                                            mAuth.signOut();
                                                            getActivity().finish();
                                                            Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                            startActivity(intent);
                                                            getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                                                        }else{
                                                            Log.d(TAG, "onComplete: Password not updated");
                                                            mProgressBar.setVisibility(GONE);
                                                            try
                                                            {
                                                                throw Objects.requireNonNull(task.getException());
                                                            }
                                                            // if user enters wrong password.
                                                            catch (FirebaseAuthWeakPasswordException weakPassword)
                                                            {
                                                                Log.d(TAG, "onComplete: weak_password");
                                                                Toast.makeText(getActivity(), "Your password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                                                            }
                                                            catch (Exception e)
                                                            {
                                                                Log.d(TAG, "onComplete: " + e.getMessage());
                                                            }
                                                        }
                                                    }
                                                });
                                    }else{
                                        Log.d(TAG, "onComplete: newPassword not equal Retype new Password");
                                        mProgressBar.setVisibility(GONE);
                                        Toast.makeText(getActivity(), "Re-type new password does not match", Toast.LENGTH_SHORT).show();
                                    }
                                }else{
                                    Log.d(TAG, "User re-authenticated. failed");
                                    mProgressBar.setVisibility(GONE);
                                    Toast.makeText(getActivity(), "Current password is invalid", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }catch (NullPointerException e){
                Log.e(TAG, "changePassword: "  + e.getMessage());
            }
        }
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
