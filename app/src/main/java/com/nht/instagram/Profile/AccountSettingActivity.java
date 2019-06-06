package com.nht.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nht.instagram.R;
import com.nht.instagram.Utils.FirebaseMethods;
import com.nht.instagram.Utils.SectionsStatePagerAdapter;

import java.util.ArrayList;

public class AccountSettingActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingActivity";
    private static final byte ACTIVITY_NUM = 4;
    private Context mContext;
    public SectionsStatePagerAdapter pagerAdapter;
    private ViewPager mViewPager;
    private RelativeLayout mRelativeLayout;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods mFirebaseMethods;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsetting);
        mContext = AccountSettingActivity.this;
        Log.d(TAG, "onCreate: started");
        mViewPager = (ViewPager)findViewById(R.id.viewpager_container);
        mRelativeLayout = (RelativeLayout)findViewById(R.id.relLayout1);

        setupSettingsList();
        setupFragment();
        getIncomingIntent();
        setupFirebaseAuth();

        //setup back arrow
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating back to 'ProfileActivity'");
                Intent intent = new Intent(AccountSettingActivity.this, ProfileActivity.class);
                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    private void getIncomingIntent(){
        Intent intent = getIntent();

        //if there is an imageUrl attached as an extra, then it was chosen from the gallery/photo fragment
        if (intent.hasExtra(getString(R.string.selected_image)) || intent.hasExtra(getString(R.string.selected_bitmap))){
            if(intent.hasExtra(getString(R.string.selected_image))){
                Log.d(TAG, "getIncomingIntent: New incoming imgUrl");
                if(intent.getStringExtra(getString(R.string.return_to_fragment)).equals(getString(R.string.edit_profile_fragment))){

                    //set the new profile picture
                    FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingActivity.this);
                    firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null,
                            intent.getStringExtra(getString(R.string.selected_image)), null );
                }
            }else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                //set the new profile picture
                FirebaseMethods firebaseMethods = new FirebaseMethods(AccountSettingActivity.this);
                byte[] byteArray = getIntent().getByteArrayExtra(getString(R.string.selected_bitmap));
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                firebaseMethods.uploadNewPhoto(getString(R.string.profile_photo), null,
                        null, bitmap);
            }
        }

        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "getIncomingIntent: received incoming from " + getString(R.string.profile_activity));
            setViewPager(pagerAdapter.getFragmentNumber(getString(R.string.edit_profile_fragment)));
        }
    }

    private void setupFragment(){
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(), getString(R.string.edit_profile_fragment));
        pagerAdapter.addFragment(new ChangePasswordFragment(), getString(R.string.change_password_fragment));
        pagerAdapter.addFragment(new SignOutFragment(), getString(R.string.sign_out_fragment));
        pagerAdapter.addFragment(new RemoveAccount(), getString(R.string.delete_account_fragment));
    }

    public void setViewPager(int fragmentNumber){
        mRelativeLayout.setVisibility(View.GONE);
        Log.d(TAG, "setViewPager: navigating to fragment: " + fragmentNumber);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }

    private  void  setupSettingsList(){
        Log.d(TAG, "setupSettingsList: initializing 'Account Settings' List");
        ListView listView = (ListView)findViewById(R.id.lvAccountSetting);

        ArrayList<String> options = new ArrayList<>();
        options.add(getString(R.string.edit_profile_fragment));
        options.add(getString(R.string.change_password_fragment));
        options.add(getString(R.string.sign_out_fragment));
        options.add(getString(R.string.delete_account_fragment));

        final ArrayAdapter adapter = new ArrayAdapter(mContext, android.R.layout.simple_list_item_1, options);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigating to fragment" + position);
                setViewPager(position);
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
                //setProfileWidgets(mFirebaseMethods.getUserSettings(dataSnapshot));
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
