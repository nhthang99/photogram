package com.nht.instagram.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nht.instagram.R;
import com.nht.instagram.Utils.BottomNavigationViewHelper;
import com.nht.instagram.Utils.UniversalImageLoader;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private static final byte ACTIVITY_NUM = 4;

    Context mContext = ProfileActivity.this;

    private ProgressBar mProgressBar;
    private ImageView mProfilePhoto;

    private TextView mEditProfile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Log.d(TAG, "onCreate: starting.");

        setupBottomNavigationView();
        setupToolbar();
        setupActivityWidgets();
        setProfilePhoto();

        mEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to EditProfileFragment");

            }
        });
    }

    private void setProfilePhoto(){
        Log.d(TAG, "setProfilePhoto: setting profile photo");
        String imgURL = "thehappypuppysite.com/wp-content/uploads/2017/09/cute4.jpg";
        UniversalImageLoader.setImage(imgURL, mProfilePhoto, mProgressBar, "http://");
    }

    private void setupActivityWidgets(){
        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        mProfilePhoto = (ImageView) findViewById(R.id.profile_photo);
        mEditProfile = (TextView)findViewById(R.id.editProfile);
    }

    private void setupToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.profileToolbar);
        setSupportActionBar(toolbar);

        ImageView profileMenu = (ImageView) findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: navigating to account setting");
                Intent intent = new Intent(mContext, AccountSettingActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx) findViewById(R.id.bottomNavigationView);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableBottomNavigationView(mContext, bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_account_full));
        menuItem.setChecked(true);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.profile_menu, menu);
//        return true;
//    }
}
