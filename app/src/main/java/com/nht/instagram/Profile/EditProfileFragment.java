package com.nht.instagram.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.nht.instagram.Models.UserAccountSetting;
import com.nht.instagram.Models.UserSettings;
import com.nht.instagram.R;
import com.nht.instagram.Share.ShareActivity;
import com.nht.instagram.Utils.FirebaseMethods;
import com.nht.instagram.Utils.UniversalImageLoader;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    //Instance
    private CircleImageView mProfilePhoto;
    private EditText mDisplayName, mUsername, mDescription, mEmail, mPhoneNumber;
    private TextView mChangeProfilePhoto;
    private String userID;
    private UserSettings mUserSettings;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (CircleImageView) view.findViewById(R.id.profile_photo);
        mDisplayName = (EditText)view.findViewById(R.id.display_name);
        mUsername = (EditText)view.findViewById(R.id.username);
        mDescription = (EditText)view.findViewById(R.id.description);
        mEmail = (EditText)view.findViewById(R.id.email_address);
        mPhoneNumber = (EditText)view.findViewById(R.id.phone_number);
        mChangeProfilePhoto = (TextView)view.findViewById(R.id.changeProfilePhoto);
        mFirebaseMethods = new FirebaseMethods(getActivity());

        setupFirebaseAuth();

        //setup back arrow
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'ProfileActivity'");
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        
        ImageView saveChange = (ImageView) view.findViewById(R.id.saveChangeUserInfo);
        saveChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: save change user info");
                saveProfileSettings();
            }
        });

        return view;
    }

    private boolean saveProfileSettings(){
        final String displayname = mDisplayName.getText().toString();
        final String username = mUsername.getText().toString();
        final String description = mDescription.getText().toString();
        final String email = mEmail.getText().toString();
        final String phone_numeber = mPhoneNumber.getText().toString();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!mUserSettings.getUser().getUsername().equals(username)){
                    checkIfUserameExist(username);
                }
                if (!mUserSettings.getSettings().getDisplay_name().equals(displayname)){
                    mFirebaseMethods.updateDisplayName(displayname);
                    Toast.makeText(getActivity(), "Saved change name", Toast.LENGTH_SHORT).show();
                }
                if (!mUserSettings.getSettings().getDescriptions().equals(description)){
                    mFirebaseMethods.updateDescriptions(description);
                    Toast.makeText(getActivity(), "Saved change descriptions", Toast.LENGTH_SHORT).show();
                }
                if (!(mUserSettings.getUser().getPhone_number().equals(phone_numeber))){
                    mFirebaseMethods.updatePhoneNumber(phone_numeber);
                    Toast.makeText(getActivity(), "Saved change phone number", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return true;
    }

    private void checkIfUserameExist(final String username) {
        Log.d(TAG, "checkIfUserameExist: Checking if " + username + " already exist");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference
                .child(getString(R.string.db_users))
                .orderByChild(getString(R.string.field_username))
                .equalTo(username);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    //add the username
                    mFirebaseMethods.updateUsername(username);
                    Toast.makeText(getActivity(), "Saved change username.", Toast.LENGTH_SHORT).show();
                }
                for (DataSnapshot signleSnapshot: dataSnapshot.getChildren()){
                    if (signleSnapshot.exists()){
                        Log.d(TAG, "onDataChange: checkIfUserameExist: found a match " + signleSnapshot.getValue(User.class).getUsername());
                        Toast.makeText(getActivity(), "That username already exist", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void setProfileWidgets(UserSettings userSettings){

        mUserSettings = userSettings;
        UserAccountSetting settings = userSettings.getSettings();

        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
//        Glide.with(getActivity()).load(settings.getProfile_photo()).into(mProfilePhoto);
        Log.d(TAG, "setProfileWidgets: "+ settings.getProfile_photo());

        mDisplayName.setText(settings.getDisplay_name());
        mUsername.setText(settings.getUsername());
        mDescription.setText(settings.getDescriptions());
        mEmail.setText(userSettings.getUser().getEmail());
        mPhoneNumber.setText(String.valueOf(userSettings.getUser().getPhone_number()));

        mChangeProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: changing profile photo");
                Intent intent = new Intent(getActivity(), ShareActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivity(intent);
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    /*
    --------------------------------------- Firebase Authentication -------------------------------
     */
    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebase: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        userID = mAuth.getCurrentUser().getUid();


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

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieving user info from database
                setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
