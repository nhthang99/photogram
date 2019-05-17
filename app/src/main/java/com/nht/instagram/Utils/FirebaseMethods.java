package com.nht.instagram.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nht.instagram.Home.HomeActivity;
import com.nht.instagram.Models.User;
import com.nht.instagram.Models.UserAccountSetting;
import com.nht.instagram.R;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private Context mContext;
    private String userID;

    public FirebaseMethods(Context mContext) {
        mAuth = FirebaseAuth.getInstance();
        this.mContext = mContext;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public boolean checkIfUsernameExist(String username, DataSnapshot dataSnapshot){
        Log.d(TAG, "checkIfUsernameExist: checking if " + username + "already exist");

        User user = new User();
        for (DataSnapshot ds: dataSnapshot.child(userID).getChildren()){
            Log.d(TAG, "checkIfUsernameExist: datasnapshot " + ds);
            user.setUsername(ds.getValue(User.class).getUsername());
            Log.d(TAG, "checkIfUsernameExist: username: " + user.getUsername());

            if (StringManipulation.expandUsername(user.getUsername()).equals(username)) {
                Log.d(TAG, "checkIfUsernameExist: found a match " + user.getUsername());
                return true;
            }
        }
        return false;
    }

    public void registerNewEmail(final String email, String password, final String username){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Toast.makeText(mContext, "Registration successful", Toast.LENGTH_SHORT).show();

                            userID = mAuth.getCurrentUser().getUid();

                            //Send user to LoginActivity
                            Intent intent = new Intent(mContext, HomeActivity.class);
                            mContext.startActivity(intent);
                        } else {
                            /* If sign in fails, display a message to the user. */
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(mContext, "Registration failed! Please try again later",
                                    Toast.LENGTH_SHORT).show();
                            try
                            {
                                throw task.getException();
                            }
                            // if user enters wrong password.
                            catch (FirebaseAuthWeakPasswordException weakPassword)
                            {
                                Log.d(TAG, "onComplete: weak_password");
                                Toast.makeText(mContext, "Your password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                            } catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                            {
                                Log.d(TAG, "onComplete: malformed_email");
                                Toast.makeText(mContext, "This email is malformed. Please try again later", Toast.LENGTH_SHORT).show();
                            }
                            catch (FirebaseAuthUserCollisionException existEmail)
                            {
                                Log.d(TAG, "onComplete: exist_email");
                                Toast.makeText(mContext, "This email is exist. Please try again later", Toast.LENGTH_SHORT).show();
                            }
                            // if user enters wrong email.
                            catch (Exception e)
                            {
                                Log.d(TAG, "onComplete: " + e.getMessage());
                            }
                        }
                    }
                });
    }

    public void addNewUser(String email, String username, String descriptions, String profile_photo){

        User user = new User(userID,
                1,
                StringManipulation.condenseUsername(username),
                email
        );

        myRef.child(mContext.getString(R.string.db_users))
                .child(userID)
                .setValue(user);

        UserAccountSetting settings = new UserAccountSetting(
                descriptions,
                username,
                0,
                0,
                0,
                profile_photo,
                username
        );

        myRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(userID)
                .setValue(settings);
    }
}
