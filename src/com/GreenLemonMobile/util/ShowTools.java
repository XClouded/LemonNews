
package com.GreenLemonMobile.util;

import android.widget.Toast;

public class ShowTools {

    public static void toast(String notice) {
        Toast.makeText(MyApplication.getInstance(), notice, Toast.LENGTH_SHORT).show();
    }

    public static void toastInThread(final String notice) {

        HandlerExecutor.getUiThreadExecutor().execute(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(MyApplication.getInstance(), notice, Toast.LENGTH_SHORT).show();
            }

        });
    }

    public static void toastLongInThread(final String notice) {
        HandlerExecutor.getUiThreadExecutor().execute(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(MyApplication.getInstance(), notice, Toast.LENGTH_LONG).show();
            }

        });
    }

    public static void toastLong(String notice) {
        Toast.makeText(MyApplication.getInstance(), notice, Toast.LENGTH_LONG).show();
    }
}
