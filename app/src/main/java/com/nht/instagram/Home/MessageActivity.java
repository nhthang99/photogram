package com.nht.instagram.Home;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.nht.instagram.Utils.UniversalImageLoader;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MessageActivity";

    private ImageView back;
    private CircleImageView mProfilePhoto;
    private TextView mUserName;
    private ImageButton btn_send;
    private EditText mText;

    //firebase
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseUser fuser;
    private DatabaseReference myRef;

    private User mUser;
    private UserSettings userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        mUserName = (TextView) findViewById(R.id.username);
        mProfilePhoto = (CircleImageView) findViewById(R.id.profile_photo);
        back = (ImageView) findViewById(R.id.backArrow);
        mText = (EditText) findViewById(R.id.text_message);
        btn_send =  (ImageButton) findViewById(R.id.btn_send);

        try{
            Intent intent = getIntent();
            mUser = intent.getParcelableExtra("user_chat");
            init();
            fuser = FirebaseAuth.getInstance().getCurrentUser();
            btn_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String mgs = mText.getText().toString();
                    if (!mgs.equals("")){
                        sendMessage(fuser.getUid(),userData.getUser().getUser_id(),mgs);
                    }else {
                        Toast.makeText(MessageActivity.this,"You can't send a empty message",Toast.LENGTH_SHORT).show();
                    }
                    mText.setText("");
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, "onCreateView: NullPointerException: "  + e.getMessage() );
        }

        back();

    }

    private void init(){
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference();
        Query query1 = reference1.child(getString(R.string.db_user_account_settings))
                .orderByChild(getString(R.string.field_user_id)).equalTo(mUser.getUser_id());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnapshot :  dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: DIT CON ME found user:" + singleSnapshot.getValue(UserAccountSetting.class).toString());

                    userData = new UserSettings();
                    userData.setUser(mUser);
                    userData.setSettings(singleSnapshot.getValue(UserAccountSetting.class));
                    setProfileWidgets(userData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void setProfileWidgets(UserSettings userSettings){
        //Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.toString());
        //Log.d(TAG, "setProfileWidgets: setting widgets with data retrieving from firebase database: " + userSettings.getSettings().getUsername());


        //User user = userSettings.getUser();
        UserAccountSetting settings = userSettings.getSettings();
        UniversalImageLoader.setImage(settings.getProfile_photo(), mProfilePhoto, null, "");
        mUserName.setText(settings.getUsername());

    }

    private void sendMessage(String  sender, String recerver, String message){
        DatabaseReference mReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",recerver);
        hashMap.put("message",message);

        mReference.child("Chats").push().setValue(hashMap);
    }

    private void back(){
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: closing the activity");
                finish();
            }
        });
    };
}