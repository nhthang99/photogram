package com.nht.instagram.Share;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nht.instagram.Profile.AccountSettingActivity;
import com.nht.instagram.R;
import com.nht.instagram.Utils.Permissions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private static final int PHOTO_FRAGMENT_NUM = 1;
    private static final int CAMERA_REQUEST_CODE = 5;
    private static final int TASK_SHARE_ACTIVITY = 268435456;

    //vars
    private Button btnLaunchCamera;
    String pathToFile;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        btnLaunchCamera = (Button)view.findViewById(R.id.btnLaunchCamera);
        btnLaunchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Launching Camera");
                if (((ShareActivity)getActivity()).getCurrentTabNumber() == PHOTO_FRAGMENT_NUM){
                    if (((ShareActivity)getActivity()).checkPermissions(Permissions.CAMERA_PERMISSION[0])){
                        Log.d(TAG, "onClick: starting camera");
                        dispatchPictureTakeAction();
                    }
                    else{
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }
                }
            }
        });

        return view;
    }

    private boolean isRootTask(){
        if(((ShareActivity)getActivity()).getTask() == TASK_SHARE_ACTIVITY){
            return false;
        }
        else{
            return true;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: done taking a photo");
            Log.d(TAG, "onActivityResult: attemping to navigate to final share screen");

            Bitmap bitmap = BitmapFactory.decodeFile(pathToFile);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            Log.d(TAG, "onActivityResult: byteArray: " + byteArray);

            if(isRootTask()){
                try{
                    if (bitmap == null){
                        Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                        Intent intent = new Intent(getActivity(), ShareActivity.class);
                        startActivity(intent);
                    }else{
                        Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                        Intent intent = new Intent(getActivity(), NextActivity.class);
//                        intent.putExtra(getString(R.string.selected_bitmap), bitmap);
                        intent.putExtra(getString(R.string.selected_bitmap), byteArray);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }
            }else{
                try{
                    Log.d(TAG, "onActivityResult: received new bitmap from camera: " + bitmap);
                    Intent intent = new Intent(getActivity(), AccountSettingActivity.class);
                    intent.putExtra(getString(R.string.selected_bitmap), byteArray);
                    intent.putExtra(getString(R.string.return_to_fragment), getString(R.string.edit_profile_fragment));
                    startActivity(intent);
                    getActivity().finish();
                }catch (NullPointerException e){
                    Log.d(TAG, "onActivityResult: NullPointerException: " + e.getMessage());
                }
            }
        }
    }

    private void dispatchPictureTakeAction(){
        Intent CameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (CameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            photoFile = createPhotoFile();
            if (photoFile != null) {
                pathToFile = photoFile.getAbsolutePath();
                Uri photoUri = FileProvider.getUriForFile(getActivity(), "com.nht.instagram.fileprovider", photoFile);
                CameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(CameraIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    private File createPhotoFile(){
        String name = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(name, ".jpg" , storageDir);

        }catch (IOException e){
            Log.e(TAG, "createPhotoFile: IOException" + e.getMessage());
        }
        return image;
    }
}