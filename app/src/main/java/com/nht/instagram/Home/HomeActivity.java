package com.nht.instagram.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nht.instagram.Login.LoginActivity;
import com.nht.instagram.Models.Photo;
import com.nht.instagram.R;
import com.nht.instagram.Utils.BottomNavigationViewHelper;
import com.nht.instagram.Utils.MainfeedListAdapter;
import com.nht.instagram.Utils.UniversalImageLoader;
import com.nht.instagram.Utils.ViewCommentFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements MainfeedListAdapter.OnLoadMoreItemsListener {

    private static final String TAG = "HomeActivity";

    @Override
    public void onLoadMoreItems() {
        Log.d(TAG, "onLoadMoreItems: displaying more photos");
        HomeFragment fragment = (HomeFragment)getSupportFragmentManager()
                .findFragmentByTag("android:switcher:" + R.id.viewpager_container + ":" + mViewPager.getCurrentItem());
        if(fragment != null){
            fragment.displayMorePhotos();
        }
    }

    private static final byte ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 1;

    private Context mContext = HomeActivity.this;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //vars
    private ArrayList<Photo> mPhotos;

    //widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");

        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);

        mPhotos = new ArrayList<>();


        setupFirebaseAuth();
        setupBottomNavigationView();
        setupViewPager();
        initImageLoader();

    }

    public void onCommentThreadSelected(Photo photo,  String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a coemment thread");

        ViewCommentFragment fragment  = new ViewCommentFragment();
        Bundle args = new Bundle();
        args.putParcelable(getString(R.string.photo), photo);
        args.putString(getString(R.string.home_activity), getString(R.string.home_activity));
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(getString(R.string.view_comments_fragment));
        transaction.commit();

    }

    public void hideLayout(){
        Log.d(TAG, "hideLayout: hiding layout");
        mRelativeLayout.setVisibility(View.GONE);
        mFrameLayout.setVisibility(View.VISIBLE);
    }


    public void showLayout(){
        Log.d(TAG, "hideLayout: showing layout");
        mRelativeLayout.setVisibility(View.VISIBLE);
        mFrameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(mFrameLayout.getVisibility() == View.VISIBLE){
            showLayout();
        }
    }

    private void initImageLoader(){
        UniversalImageLoader universalImageLoader = new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(universalImageLoader.getConfig());
    }

    /*
    Responsibible for adding the 3 tabs: Camera, Home , Message
     */

    private void setupViewPager(){
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new CameraFragment());
        adapter.addFragment(new HomeFragment());
        adapter.addFragment(new MessageFragment());
        mViewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        tabLayout.getTabAt(0).setIcon(R.drawable.ic_camera);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_instagram);
        tabLayout.getTabAt(2).setIcon(R.drawable.ic_arrow);
    }

    /*
    Setup BottomNavigationView
     */

    private void setupBottomNavigationView(){
        Log.d(TAG, "setupBottomNavigationView: setting up BottomNavigationView");
        BottomNavigationViewEx bottomNavigationViewEx = findViewById(R.id.bottomNavigationViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx);
        BottomNavigationViewHelper.enableNavigation(mContext, this,bottomNavigationViewEx);
        Menu menu = bottomNavigationViewEx.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_home_full));
        menuItem.setChecked(true);
    }

    /*
    --------------------------------------- Firebase Authentication -------------------------------
     */
    private void checkCurrenUser(FirebaseUser user){
        Log.d(TAG, "checkCurrenUser: checking user");

        // Not found user
        if (user == null){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void setupFirebaseAuth(){
        Log.d(TAG, "setupFirebase: setting up firebase auth");

        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                checkCurrenUser(user);

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
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
        mViewPager.setCurrentItem(HOME_FRAGMENT);
        checkCurrenUser(mAuth.getCurrentUser());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
