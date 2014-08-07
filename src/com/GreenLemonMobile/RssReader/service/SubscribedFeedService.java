
package com.GreenLemonMobile.RssReader.service;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import com.GreenLemonMobile.RssReader.Application;
import com.GreenLemonMobile.RssReader.ChannelActivity;
import com.GreenLemonMobile.RssReader.ContentCenterActivity;
import com.GreenLemonMobile.RssReader.ILoadListener;
import com.GreenLemonMobile.RssReader.ItemDetailActivity;
import com.GreenLemonMobile.RssReader.MainActivity;
import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.RssReader.entity.FeedItemEntity;
import com.GreenLemonMobile.RssReader.entity.SubscribedChannelEntity;
import com.GreenLemonMobile.RssReader.receiver.SubscribedFeedWidget;
import com.GreenLemonMobile.concurrent.Flag;
import com.GreenLemonMobile.util.ITransKey;
import com.GreenLemonMobile.util.NetUtils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class SubscribedFeedService extends Service {
    public final static String APP_WIDGET_NEXT = "com.greenlemonmobile.widget.action.APP_WIDGET_NEXT";
    public final static String APP_WIDGET_PREV = "com.greenlemonmobile.widget.action.APP_WIDGET_PREV";
    public final static String APP_UPDATE_SERVICE = "com.greenlemonmobile.widget.action.UPDATE_SERVICE";
    public final static String APP_WIDGET_RELOAD = "com.greenlemonmobile.widget.action.APP_WIDGET_RELOAD";
    public final static String APP_WIDGET_STOP = "com.greenlemonmobile.widget.action.APP_WIDGET_STOP";
    public final static String IMM_UPDATE_SERVICE = "com.greenlemonmobile.widget.action.IMM_UPDATE_SERVICE";

    private Intent detailpage = null;
    private PendingIntent detailpendingIntent;
    private RemoteViews updateViews;
    private ComponentName widget;

    private int feedIndex;
    private ArrayList<FeedItemEntity> latestFeeds = new ArrayList<FeedItemEntity>(SubscribedFeedWidget.WIDGET_TOTAL_FEED_COUNT);
    private ArrayList<SubscribedChannelEntity> subscribedChannelLists = new ArrayList<SubscribedChannelEntity>();
    private AppWidgetManager manager;
    private boolean threadflag = true;
    private UpdateThread updateThread = null;
    private boolean hasSubscribedSource = true;
    
    private int iconWidth;
    private int iconHeight;
    
    @Override
    public IBinder onBind(Intent paramIntent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter localIntentFilter = new IntentFilter();
        localIntentFilter.addAction(APP_WIDGET_RELOAD);
        registerReceiver(mIntentReceiver, localIntentFilter);
        
        iconWidth = getResources().getDimensionPixelSize(R.dimen.widget_logo_size);
        iconHeight = getResources().getDimensionPixelSize(R.dimen.widget_logo_size);
        
        synchronized (subscribedChannelLists) {
            SubscribedChannelEntity.getSubscribedChannelList(subscribedChannelLists);
        }
        synchronized (latestFeeds) {
            FeedItemEntity.getLatestSubscribedItems(latestFeeds, SubscribedFeedWidget.WIDGET_TOTAL_FEED_COUNT);
        }
        if (!latestFeeds.isEmpty() || !subscribedChannelLists.isEmpty())
            hasSubscribedSource = true;
        else
            hasSubscribedSource = false;
    }
    
    @Override
    public void onDestroy() {
        threadflag = false;
        unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    public void onStart(Intent intent, int paramInt) {
        super.onStart(intent, paramInt);
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(APP_WIDGET_PREV))
                    doPrev();
                else if (action.equals(APP_WIDGET_NEXT))
                    doNext();
                else if (action.equals(APP_UPDATE_SERVICE))
                    notifyWidget();
                else if (action.equals(APP_WIDGET_RELOAD))
                    doReload();
                else if (action.equals(APP_WIDGET_STOP))
                    stop();
                else if (action.equals(IMM_UPDATE_SERVICE)) {
                    synchronized(subscribedChannelLists) {
                        SubscribedChannelEntity.getSubscribedChannelList(subscribedChannelLists);
                    }
                    synchronized(latestFeeds) {
                        FeedItemEntity.getLatestSubscribedItems(latestFeeds, SubscribedFeedWidget.WIDGET_TOTAL_FEED_COUNT);
                    }
                    if (!latestFeeds.isEmpty() || !subscribedChannelLists.isEmpty())
                        hasSubscribedSource = true;
                    else
                        hasSubscribedSource = false;
                    notifyWidget();
                }
            }
        }
    }
    
    private void stop() {
        stopSelf();
    }
    
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(APP_WIDGET_RELOAD))
                doReload();
        }
    };
    
    private void updateWidgetInfo(Context context, RemoteViews views, FeedItemEntity entity) {
        views.setTextViewText(R.id.widget_feed_content, entity.title);

        String widgetTime = "";

        Date currentDate = new Date(System.currentTimeMillis());

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        inputFormat.setTimeZone(TimeZone.getDefault());
        Date publishDate = null;
        try {
            publishDate = inputFormat.parse(entity.pubDate);
        } catch (ParseException e) {
            e.printStackTrace();
            publishDate = new Date();
        }

        if (currentDate.getYear() == publishDate.getYear()
                && currentDate.getMonth() == publishDate.getMonth()
                && currentDate.getDate() == publishDate.getDate()) {
            if (currentDate.getHours() == publishDate.getHours()) {
                if (currentDate.getMinutes() == publishDate.getMinutes()) {
                    widgetTime = String.format(context.getResources().getString(R.string.same_minute_but_not_the_same_second_format_text),
                            Math.abs(currentDate.getSeconds() - publishDate.getSeconds()));
                } else {
                    widgetTime = String.format(context.getResources().getString(R.string.during_one_hour_format_text),
                            Math.abs(currentDate.getMinutes() - publishDate.getMinutes()));
                }
            } else {
                SimpleDateFormat df = new SimpleDateFormat(context.getResources().getString(R.string.one_hour_before_to_time_zero_format_text));
                df.setTimeZone(TimeZone.getDefault());
                widgetTime = df.format(publishDate);
            }
        } else if (currentDate.getYear() == publishDate.getYear()
                && currentDate.getMonth() == publishDate.getMonth()
                && (currentDate.getDate() == publishDate.getDate() + 1)) {
            SimpleDateFormat df = new SimpleDateFormat(context.getResources().getString(R.string.yesterday_time_format_text));
            df.setTimeZone(TimeZone.getDefault());
            widgetTime = df.format(publishDate);
        } else {
            SimpleDateFormat df = new SimpleDateFormat(context.getResources().getString(R.string.yy_mm_dd_hh_mm_format_text));
            df.setTimeZone(TimeZone.getDefault());
            widgetTime = df.format(publishDate);
        }

        views.setTextViewText(R.id.app_widget_time, widgetTime);
    }
    
    private RemoteViews buildUpdate(Context context) {
        ComponentName component = new ComponentName(context, SubscribedFeedService.class);
        updateViews = new RemoteViews(context.getPackageName(), R.layout.feedwidget);
        
        updateViews.setImageViewResource(R.id.app_widget_img, R.drawable.widget_logo);
        if (latestFeeds.isEmpty()) {
            // Create an Intent to launch MainActivity
            detailpage = new Intent(context, MainActivity.class);
            detailpage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            detailpendingIntent = PendingIntent.getActivity(context, 0, detailpage, PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.app_widget_img, detailpendingIntent);
            
            // Create an Intent to launch ContentCenterActivity
            if (hasSubscribedSource)
                detailpage = new Intent(context, MainActivity.class);
            else
                detailpage = new Intent(context, ContentCenterActivity.class);
            detailpage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            detailpendingIntent = PendingIntent.getActivity(context, 0, detailpage, PendingIntent.FLAG_UPDATE_CURRENT);
            
            // Get the layout for the App Widget and attach an on-click listener
            // to the button            
            updateViews.setOnClickPendingIntent(R.id.widget_feed_content, detailpendingIntent);
            
            updateViews.setViewVisibility(R.id.app_widget_time, View.GONE);
            updateViews.setTextViewText(R.id.widget_feed_content, context.getResources().getText(hasSubscribedSource ?  R.string.subscribed_source_updating : R.string.subsource_unsubscribed_widget_tip_text));
            
            updateViews.setTextViewText(R.id.app_widget_currnetindex, ""); 
        } else {
            FeedItemEntity entity = latestFeeds.get(feedIndex);
            
            SubscribedChannelEntity channel = null;
            for (SubscribedChannelEntity tmp : subscribedChannelLists) {
                String channelName = tmp.getChannel() + "\\" + tmp.getName();
                if (channelName.equals(entity.channelName)) {
                    channel = tmp;
                    break;
                }
            }
            
            if (channel == null || entity == null)
                return null;
            
            // Create an Intent to launch MainActivity
            detailpage = new Intent(context, ChannelActivity.class);
            detailpage.putExtra(ITransKey.KEY1, channel.getName());
            detailpage.putExtra(ITransKey.KEY2, channel.getChannel());
            detailpage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            detailpendingIntent = PendingIntent.getActivity(context, 0, detailpage, PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.app_widget_img, detailpendingIntent);
            
            // Create an Intent to launch ItemDetailActivity
            detailpage = new Intent(context, ItemDetailActivity.class);
            detailpage.putExtra(ITransKey.KEY1, entity.channelName);
            detailpage.putExtra(ITransKey.KEY2, entity.linkMD5);
            detailpage.putExtra(ITransKey.KEY3, channel.getName());
            detailpage.putExtra(ITransKey.KEY4, channel.getChannel()); 
            detailpage.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            detailpendingIntent = PendingIntent.getActivity(context, 0, detailpage, PendingIntent.FLAG_UPDATE_CURRENT);            
            // Get the layout for the App Widget and attach an on-click listener
            // to the button            
            updateViews.setOnClickPendingIntent(R.id.widget_feed_content, detailpendingIntent);
            
            detailpage = new Intent(APP_WIDGET_PREV);
            detailpage.setComponent(component);
            detailpendingIntent = PendingIntent.getService(context, 0, detailpage, PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.widget_btn_up, detailpendingIntent);
            
            detailpage = new Intent(APP_WIDGET_NEXT);
            detailpage.setComponent(component);
            detailpendingIntent = PendingIntent.getService(context, 0, detailpage, PendingIntent.FLAG_UPDATE_CURRENT);
            updateViews.setOnClickPendingIntent(R.id.widget_btn_down, detailpendingIntent);
            
            updateViews.setViewVisibility(R.id.app_widget_time, View.VISIBLE);
            
            String indexString = Integer.toString(feedIndex + 1) + "/" + Integer.toString(SubscribedFeedWidget.WIDGET_TOTAL_FEED_COUNT);
            updateViews.setTextViewText(R.id.app_widget_currnetindex, indexString);

            if (channel != null) {
                if (!TextUtils.isEmpty(channel.getImage())) {
                    final long imageID = Application.imageCache.getNewID();
                    Drawable drawable = null;
                    try {
                        drawable = Application.imageCache.loadImage(imageID, Uri.parse(channel.getImage()), iconWidth, iconHeight);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (drawable != null && (drawable instanceof BitmapDrawable)) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
                        if (bitmapDrawable.getBitmap() != null && !bitmapDrawable.getBitmap().isRecycled())
                            updateViews.setImageViewBitmap(R.id.app_widget_img, bitmapDrawable.getBitmap());
                    }
                }
            }
            
            updateWidgetInfo(context, updateViews, entity);
        }
        
        return updateViews;
    }

    private void doNext() {
        ++feedIndex;
        if (feedIndex > latestFeeds.size() - 1)
            feedIndex = 0;
        notifyWidget();
    }

    private void doPrev() {
        --feedIndex;
        if (feedIndex < 0)
            feedIndex = latestFeeds.size() - 1;
        if (feedIndex < 0)
            feedIndex = 0;
        notifyWidget();
    }

    private void doReload() {
        if (updateThread == null) {
            boolean autoRefresh = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("system_setting_auto_refresh", true);
            if (autoRefresh) {
                updateThread = new UpdateThread();
                updateThread.start();
            }
        }
    }

    private void notifyWidget() {
        // Tell the AppWidgetManager to perform an update on the current app
        // widget
        widget = new ComponentName(this, SubscribedFeedWidget.class);
        updateViews = buildUpdate(this);
        if (updateViews != null) {
            manager = AppWidgetManager.getInstance(this);
            manager.updateAppWidget(widget, updateViews);
        }
        doReload();
    }
    
    class UpdateThread extends Thread {
        public void run() {
            while (true) {
                if (!threadflag)
                    return;
                ArrayList<SubscribedChannelEntity> lists = new ArrayList<SubscribedChannelEntity>();
                SubscribedChannelEntity.getSubscribedChannelList(lists);
                boolean temp = lists.isEmpty() ? true : false;
                if (hasSubscribedSource != temp) {
                    hasSubscribedSource = temp;
                    notifyWidget();
                }
                if (NetUtils.getType() != NetUtils.NO_NET) {
                    ArrayList<Flag> httpRequestList = new ArrayList<Flag>();
                    for (SubscribedChannelEntity entity : lists) {
                        final Flag flag = new Flag();
                        httpRequestList.add(flag);
                        entity.refresh(SubscribedFeedService.this, new ILoadListener() {
    
                            @Override
                            public void onStart() {
                            }
    
                            @Override
                            public void onEnd() {
                                flag.set();
                            }
                            
                        }, false);
                    }
                    boolean refreshFinished = false;
                    while (!refreshFinished) {
                        refreshFinished = true;
                        for (Flag flag : httpRequestList) {
                            if (!flag.get()) {
                                refreshFinished = false;
                                break;
                            }
                        }
                        
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                synchronized(latestFeeds) {
                    FeedItemEntity.getLatestSubscribedItems(latestFeeds, SubscribedFeedWidget.WIDGET_TOTAL_FEED_COUNT);
                }
                notifyWidget();
                try {
                    Thread.sleep(SubscribedFeedWidget.WIDGET_UPDATE_INTERVAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
}
