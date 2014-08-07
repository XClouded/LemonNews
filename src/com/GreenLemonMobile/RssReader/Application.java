package com.GreenLemonMobile.RssReader;

import android.graphics.Bitmap.CompressFormat;

import com.GreenLemonMobile.util.MyApplication;

import edu.mit.mobile.android.imagecache.ImageCache;

public class Application extends MyApplication {
    
    public static ImageCache imageCache;

    
    @Override
    public void onCreate() {
        imageCache = ImageCache.getInstance(this, CompressFormat.PNG);
        super.onCreate();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

}
