package com.nht.instagram.Utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nht.instagram.Home.HomeActivity;
import com.nht.instagram.Login.LoginActivity;
import com.nht.instagram.Models.Photo;
import com.nht.instagram.Models.User;
import com.nht.instagram.Models.UserAccountSetting;
import com.nht.instagram.Models.UserSettings;
import com.nht.instagram.Profile.AccountSettingActivity;
import com.nht.instagram.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.UUID;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageReference;

    private Context mContext;
    private String userID;
    private ProgressBar mProgressBar;
    private double mPhotoUploadProgress = 0.0;

    public FirebaseMethods(Context mContext) {
        mAuth = FirebaseAuth.getInstance();
        this.mContext = mContext;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference();
        mStorageReference = FirebaseStorage.getInstance().getReference();

        if(mAuth.getCurrentUser() != null){
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    public void uploadNewPhoto(String photoType, final String caption, final String imgUrl, Bitmap bitmap){
        Log.d(TAG, "uploadNewPhoto: attempting to upload new photo.");

        FilePaths filePaths = new FilePaths();
        //case1) new photo
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading NEW photo.");

            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/photo" + UUID.randomUUID().toString());

            //convert image url to bitmap
            if (bitmap == null){
                bitmap = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(!task.isSuccessful()) {
                        try {
                            throw Objects.requireNonNull(task.getException());
                        } catch (Exception e) {
                            Log.e(TAG, "onComplete: Exception" + e.getMessage());
                        }
                    }

            }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    Log.d(TAG, "onSuccess: Photo upload success");
                    Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //add the new photo
                            addPhotoToDatabase(caption, uri.toString());
                        }
                    });
                    //navigate to the main feed so the user can see their photo
                    Intent intent = new Intent(mContext, HomeActivity.class);
                    mContext.startActivity(intent);

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();

                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * (float)taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "Photo upload progress " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });

        }
        //case new profile photo
        else if(photoType.equals(mContext.getString(R.string.profile_photo))){
            Log.d(TAG, "uploadNewPhoto: uploading new PROFILE photo");
            Toast.makeText(mContext, "Uploading...", Toast.LENGTH_SHORT).show();
            String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
            final StorageReference storageReference = mStorageReference
                    .child(filePaths.FIREBASE_IMAGE_STORAGE + "/" + user_id + "/profile_photo");

            //convert image url to bitmap
            if (bitmap == null){
                bitmap = ImageManager.getBitmap(imgUrl);
            }

            byte[] bytes = ImageManager.getBytesFromBitmap(bitmap, 100);

            UploadTask uploadTask = null;
            uploadTask = storageReference.putBytes(bytes);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //insert into 'user_account_settings' node
                            setProfilePhoto(uri.toString());
                        }
                    });

                    Toast.makeText(mContext, "Photo upload success", Toast.LENGTH_SHORT).show();
                    ((AccountSettingActivity)mContext).setViewPager(
                            ((AccountSettingActivity)mContext).pagerAdapter
                                    .getFragmentNumber(mContext.getString(R.string.edit_profile_fragment))
                    );

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: Photo upload failed.");
                    Toast.makeText(mContext, "Photo upload failed ", Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100 * (float)taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                    if(progress - 15 > mPhotoUploadProgress){
                        Toast.makeText(mContext, "Photo upload progress: " + String.format("%.0f", progress) + "%", Toast.LENGTH_SHORT).show();
                        mPhotoUploadProgress = progress;
                    }

                    Log.d(TAG, "onProgress: upload progress: " + progress + "% done");
                }
            });
        }

    }

    private void setProfilePhoto(String url){
        Log.d(TAG, "setProfilePhoto: setting new profile image: " + url);

        myRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child(mContext.getString(R.string.profile_photo))
                .setValue(url);
    }

    private String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.CANADA);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        return sdf.format(new Date());
    }

    private void addPhotoToDatabase(String caption, String url){
        Log.d(TAG, "addPhotoToDatabase: adding photo to database");

        String tags = StringManipulation.getTags(caption);
        String userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String newPhotoKey = myRef.child(mContext.getString(R.string.db_photos)).push().getKey();
        Photo photo = new Photo();
        photo.setCaption(caption);
        photo.setDate_created(getTimestamp());
        photo.setImage_path(url);
        photo.setTags(tags);
        photo.setUser_id(userID);
        photo.setPhoto_id(newPhotoKey);

        //insert to database
        myRef.child(mContext.getString(R.string.db_users_photo)).child(userID).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.db_photos)).child(newPhotoKey).setValue(photo);
    }

    public void updateUsername(final String username){
        Log.d(TAG, "updateUsername: updating username to " +  username);

        myRef.child(mContext.getString(R.string.db_users))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
        myRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_username))
                .setValue(username);
    }

    public void updateDisplayName(String displayname){
        Log.d(TAG, "updateDisplayName: updating displayname to " + displayname);

        myRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_displayname))
                .setValue(displayname);
    }

    public void updateDescriptions(String descriptions){
        Log.d(TAG, "updateDisplayName: updating displayname to " + descriptions);

        myRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.field_descriptions))
                .setValue(descriptions);
    }

    public void updatePhoneNumber(String phone_number){
        Log.d(TAG, "updateDisplayName: updating displayname to " + phone_number);

        myRef.child(mContext.getString(R.string.db_users))
                .child(userID)
                .child(mContext.getString(R.string.field_phone_number))
                .setValue(phone_number);

    }

    private void sendVerificationEmail(){
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null){
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){

                            }else{
                                Toast.makeText(mContext, "could't send verification email", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    public void registerNewEmail(final String email, String password, final String username ){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");

                            userID = mAuth.getCurrentUser().getUid();
                            sendVerificationEmail();
                            Log.d(TAG, "onComplete: Please verify email");
                            Toast.makeText(mContext, "Please verify email", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            mContext.startActivity(intent);
                        } else {
                            try
                            {
                                throw Objects.requireNonNull(task.getException());
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
                "",
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
                StringManipulation.condenseUsername(username),
                userID
        );

        myRef.child(mContext.getString(R.string.db_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    public UserSettings getUserSettings(DataSnapshot dataSnapshot){
        Log.d(TAG, "getUserAccountSettings: retrieving user account settings from firebase.");


        UserAccountSetting settings  = new UserAccountSetting();
        User user = new User();

        for(DataSnapshot ds: dataSnapshot.getChildren()){

            // user_account_settings node
            if(ds.getKey().equals(mContext.getString(R.string.db_user_account_settings))){
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                try{

                    settings.setDisplay_name(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getDisplay_name()
                    );
                    settings.setUsername(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getUsername()
                    );
                    settings.setDescriptions(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getDescriptions()
                    );
                    settings.setProfile_photo(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getProfile_photo()
                    );
                    settings.setPosts(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getPosts()
                    );
                    settings.setFollowing(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getFollowing()
                    );
                    settings.setFollowers(
                            ds.child(userID)
                                    .getValue(UserAccountSetting.class)
                                    .getFollowers()
                    );

                    Log.d(TAG, "getUserAccountSettings: retrieved user_account_settings information: " + settings.toString());
                }catch (NullPointerException e){
                    Log.e(TAG, "getUserAccountSettings: NullPointerException: " + e.getMessage() );
                }
            }
            // users node
            if(ds.getKey().equals(mContext.getString(R.string.db_users))) {
                Log.d(TAG, "getUserAccountSettings: datasnapshot: " + ds);

                user.setUsername(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUsername()
                );
                user.setEmail(
                        ds.child(userID)
                                .getValue(User.class)
                                .getEmail()
                );
                user.setPhone_number(
                        ds.child(userID)
                                .getValue(User.class)
                                .getPhone_number()
                );
                user.setUser_id(
                        ds.child(userID)
                                .getValue(User.class)
                                .getUser_id()
                );

                Log.d(TAG, "getUserAccountSettings: retrieved users information: " + user.toString());
            }
        }
        return new UserSettings(settings, user);

    }

}
