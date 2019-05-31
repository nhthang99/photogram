package com.nht.instagram.Home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nht.instagram.Login.LoginActivity;
import com.nht.instagram.Models.Comment;
import com.nht.instagram.Models.Photo;
import com.nht.instagram.R;
import com.nht.instagram.Utils.BottomNavigationViewHelper;
import com.nht.instagram.Utils.MainfeedListAdapter;
import com.nht.instagram.Utils.UniversalImageLoader;
import com.nht.instagram.Utils.ViewCommentFragment;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements MainfeedListAdapter.OnLoadMoreItemsListener {

    private static final String TAG = "HomeActivity";

    @Override
    public void onLoadMoreItems() {
        displayMorePhotos();
    }

    private static final byte ACTIVITY_NUM = 0;
    private static final int HOME_FRAGMENT = 0;

    private Context mContext = HomeActivity.this;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    //vars
    private ArrayList<Photo> mPhotos;
    private ArrayList<Photo> mPaginatedPhotos;
    private ArrayList<String> mFollowing;
    private ListView mListView;
    private MainfeedListAdapter mAdapter;
    private int mResults;

    //widgets
    private ViewPager mViewPager;
    private FrameLayout mFrameLayout;
    private RelativeLayout mRelativeLayout;
    //vars

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: starting.");

        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);
        mFrameLayout = (FrameLayout) findViewById(R.id.container);
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relLayoutParent);
        mListView = (ListView)findViewById(R.id.listView);
        mFollowing = new ArrayList<>();
        mPhotos = new ArrayList<>();

        setupFirebaseAuth();
        setupBottomNavigationView();
        initImageLoader();
        getFollowing();
    }

    public void onCommentThreadSelected(Photo photo,  String callingActivity){
        Log.d(TAG, "onCommentThreadSelected: selected a comment thread");

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


    private void getFollowing(){
        Log.d(TAG, "getFollowing: searching for following");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        try{
            Query query = reference
                    .child(getString(R.string.dbname_following))
                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){
                        Log.d(TAG, "onDataChange: found user: " +
                                singleSnapshot.child(getString(R.string.field_user_id)).getValue());

                        mFollowing.add(singleSnapshot.child(getString(R.string.field_user_id)).getValue().toString());
                    }
                    mFollowing.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    //get the photos
                    getPhotos();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "getFollowing: NullPointerException" + e.getMessage());
        }
    }

    private void getPhotos(){
        Log.d(TAG, "getPhotos: getting photos");
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        for(int i = 0; i < mFollowing.size(); i++){
            final int count = i;
            Query query = reference
                    .child(getString(R.string.db_users_photo))
                    .child(mFollowing.get(i))
                    .orderByChild(getString(R.string.field_user_id))
                    .equalTo(mFollowing.get(i));
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for(DataSnapshot singleSnapshot : dataSnapshot.getChildren()){

                        Photo photo = new Photo();
                        Map<String, Object> objectMap = (HashMap<String, Object>) singleSnapshot.getValue();

                        photo.setCaption(objectMap.get(getString(R.string.field_caption)).toString());
                        photo.setTags(objectMap.get(getString(R.string.field_tags)).toString());
                        photo.setPhoto_id(objectMap.get(getString(R.string.field_photo_id)).toString());
                        photo.setUser_id(objectMap.get(getString(R.string.field_user_id)).toString());
                        photo.setDate_created(objectMap.get(getString(R.string.field_date_created)).toString());
                        photo.setImage_path(objectMap.get(getString(R.string.field_image_path)).toString());

                        ArrayList<Comment> comments = new ArrayList<Comment>();
                        for (DataSnapshot dSnapshot : singleSnapshot
                                .child(getString(R.string.field_comments)).getChildren()){
                            Comment comment = new Comment();
                            comment.setUser_id(dSnapshot.getValue(Comment.class).getUser_id());
                            comment.setComment(dSnapshot.getValue(Comment.class).getComment());
                            comment.setDate_created(dSnapshot.getValue(Comment.class).getDate_created());
                            comments.add(comment);
                        }

                        photo.setComments(comments);
                        mPhotos.add(photo);
                    }
                    if(count >= mFollowing.size() -1){
                        //display our photos
                        displayPhotos();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void displayPhotos(){
        mPaginatedPhotos = new ArrayList<>();
        if(mPhotos != null){
            try{
                Collections.sort(mPhotos, new Comparator<Photo>() {
                    @Override
                    public int compare(Photo o1, Photo o2) {
                        return o2.getDate_created().compareTo(o1.getDate_created());
                    }
                });

                int iterations = mPhotos.size();

                if(iterations > 10){
                    iterations = 10;
                }

                mResults = 10;
                for(int i = 0; i < iterations; i++) {
                    mPaginatedPhotos.add(mPhotos.get(i));
                }

                mAdapter = new MainfeedListAdapter(mContext, R.layout.layout_mainfeed_listitem, mPhotos);
                mListView.setAdapter(mAdapter);
            }catch (NullPointerException e){
                Log.e(TAG, "displayPhotos: NullPointerException" + e.getMessage());
            }catch (IndexOutOfBoundsException e){
                Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
            }
        }
    }

    public void displayMorePhotos(){
        Log.d(TAG, "displayMorePhotos: displaying more photos");

        try{

            if(mPhotos.size() > mResults && mPhotos.size() > 0){

                int iterations;
                if(mPhotos.size() > (mResults + 10)){
                    Log.d(TAG, "displayMorePhotos: there are greater than 10 more photos");
                    iterations = 10;
                }else{
                    Log.d(TAG, "displayMorePhotos: there is less than 10 more photos");
                    iterations = mPhotos.size() - mResults;
                }

                //add the new photos to the paginated results
                for(int i = mResults; i < mResults + iterations; i++){
                    mPaginatedPhotos.add(mPhotos.get(i));
                }
                mResults = mResults + iterations;
                mAdapter.notifyDataSetChanged();
            }
        }catch (NullPointerException e){
            Log.e(TAG, "displayPhotos: NullPointerException: " + e.getMessage() );
        }catch (IndexOutOfBoundsException e){
            Log.e(TAG, "displayPhotos: IndexOutOfBoundsException: " + e.getMessage() );
        }
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
