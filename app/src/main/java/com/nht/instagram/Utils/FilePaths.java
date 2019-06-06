package com.nht.instagram.Utils;

import android.os.Environment;

public class FilePaths {
    public String ROOT_DIR = Environment.getExternalStorageDirectory().getPath();
    public String PICTURES = ROOT_DIR + "/Pictures";
    public String DCIM = ROOT_DIR + "/DCIM";
    public String thumnail = ROOT_DIR + "/DCIM/.thumbnails";
    public String FIREBASE_IMAGE_STORAGE = "photos/users";
}
