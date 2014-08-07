
package com.GreenLemonMobile.RssReader.receiver;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.GreenLemonMobile.RssReader.service.SubscribedFeedService;

public class SubscribedFeedWidget extends AppWidgetProvider {   
    public final static int WIDGET_TOTAL_FEED_COUNT = 20;
    // five minutes interval
    public final static long WIDGET_UPDATE_INTERVAL = 60000L * 5;
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {        
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        ComponentName component = new ComponentName(context, SubscribedFeedService.class);
        Intent intent = new Intent(SubscribedFeedService.APP_UPDATE_SERVICE);
        intent.setComponent(component);
        context.startService(intent);
    }
    
//    private PendingIntent buildAlarmOperation(Context context) {
//        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), getClass().getName());
//        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//        int ids[] = appWidgetManager.getAppWidgetIds(thisAppWidget);
//        Bundle extras = new Bundle();
//        extras.putIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
//        intent.putExtras(extras);
//        
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        
//        return pendingIntent;
//    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTimeInMillis(System.currentTimeMillis());
//        calendar.set(Calendar.SECOND, (int)(WIDGET_UPDATE_INTERVAL / 1000));
//        
//        am.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), WIDGET_UPDATE_INTERVAL, buildAlarmOperation(context));
        
        ComponentName component = new ComponentName(context, SubscribedFeedService.class);
        Intent intent = new Intent(SubscribedFeedService.APP_WIDGET_RELOAD);
        intent.setComponent(component);
        context.startService(intent);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        am.cancel(buildAlarmOperation(context));
        
        Intent service = new Intent(context, SubscribedFeedService.class);
        context.stopService(service);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

}
