package com.nht.instagram.Utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class ImageManager {
    private static final String TAG = "ImageManager";

    public static Bitmap getBitmap(String imgURL){
        File imageFile = new File(imgURL);
        InputStream fileInputStream = null;
        Bitmap bitmap = null;

        try{
            fileInputStream = new FileInputStream(imageFile);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
        }
        catch (FileNotFoundException e){
            Log.e(TAG, "getBitmap: FileNotFoundException" + e.getMessage());
        }finally {
            try{
                assert fileInputStream != null;
                fileInputStream.close();
            }catch (IOException e){
                Log.e(TAG, "getBitmap: IOException" + e.getMessage());
            }
        }
        return bitmap;
    }

    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);

        return stream.toByteArray();
    }
}
