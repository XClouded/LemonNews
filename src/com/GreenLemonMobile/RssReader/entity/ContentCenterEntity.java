package com.GreenLemonMobile.RssReader.entity;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import com.GreenLemonMobile.RssReader.Application;
import com.GreenLemonMobile.RssReader.provider.DataProvider;
import com.GreenLemonMobile.RssReader.service.SubscribedFeedService;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class ContentCenterEntity implements Serializable {
	
	/**
     * 
     */
    private static final long serialVersionUID = 100002L;
    private String name;
	private String subName;
	private String url;
	private String image;
	private String folder;
	private long tag; // 用来标识第几级目录，例如：0、1、2....n
	private long subtag; // 用来标识是否包含子目录，0不包含子目录，可以直接被订阅，1包含子目录，不可订阅
	private long subscribed;
	
	public static void getContentCenterList(ArrayList<ContentCenterEntity> lists, int tag, String folder) {
	    lists.clear();
		String selection = "tag=? AND folder=?";
		String[] selectionArg ={Integer.toString(tag), folder};
		Cursor cursor = Application.getInstance().getApplicationContext().getContentResolver().query(DataProvider.CONTENT_URI_CONTENTCENTER, null, selection, selectionArg, null);
		
		if (cursor != null && cursor.getCount() > 0) {
			int nameIndex = cursor.getColumnIndex("name");
			int subNameIndex = cursor.getColumnIndex("subname");
			int urlIndex = cursor.getColumnIndex("url");
			int imageIndex = cursor.getColumnIndex("image");
			int tagIndex = cursor.getColumnIndex("tag");
			int subtagIndex = cursor.getColumnIndex("subtag");
			int subscribedIndex = cursor.getColumnIndex("subscribed");
			int folderIndex = cursor.getColumnIndex("folder");
			while (cursor.moveToNext()) {
				ContentCenterEntity entity = new ContentCenterEntity();
				
				entity.setName(cursor.getString(nameIndex));
				entity.setSubName(cursor.getString(subNameIndex));
				entity.setUrl(cursor.getString(urlIndex));
				entity.setImage(cursor.getString(imageIndex));
				entity.setTag(cursor.getLong(tagIndex));
				entity.setSubtag(cursor.getLong(subtagIndex));
				entity.setSubscribed(cursor.getLong(subscribedIndex));
				entity.setFolder(cursor.getString(folderIndex));
				lists.add(entity);
			}
		}
		
		if (cursor != null)
			cursor.close();
		
		return;
	}	

	public ContentCenterEntity() {
		super();
	}

	public long getSubtag() {
		return subtag;
	}

	public void setSubtag(long subtag) {
		this.subtag = subtag;
	}

	public long getSubscribed() {
		return subscribed;
	}

	public void setSubscribed(long subscribed) {
		this.subscribed = subscribed;
	}

	public long getTag() {
		return tag;
	}

	public void setTag(long tag) {
		this.tag = tag;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }	
	
    public static void subscribe(Context context, final ContentCenterEntity entity) {
        String channel = entity.getFolder() + "\\" + entity.getName();
        ContentValues values = new ContentValues();
        values.put("name", entity.getName());
        values.put("url", entity.getUrl());
        values.put("image", entity.getImage());
        values.put("channel", channel);
        values.put("unread_news", 0);
        
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getDefault());
        values.put("latest_refresh_date", df.format(date));
        String[] projection = {"count(*)"};
        int orderIndex = 0;
        Cursor cursor = Application.getInstance().getContentResolver().query(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, projection, null, null, null);
        if (cursor != null && cursor.getCount() > 0 && cursor.moveToNext())
            orderIndex = cursor.getInt(0);
        if (cursor != null)
            cursor.close();
        values.put("order_index", orderIndex);
        Application.getInstance().getContentResolver().insert(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, values);
        entity.setSubscribed(1);
        
        values.clear();
        values.put("subscribed", entity.getSubscribed());        
        String where = "name=? AND folder=?";
        String[] selectionArgs = {entity.getName(), entity.getFolder()};
        Application.getInstance().getContentResolver().update(DataProvider.CONTENT_URI_CONTENTCENTER, values, where, selectionArgs);
        
        ComponentName component = new ComponentName(context, SubscribedFeedService.class);
        Intent intent = new Intent(SubscribedFeedService.IMM_UPDATE_SERVICE);
        intent.setComponent(component);
        context.startService(intent);
    }
    
    public static void cancelSubscribe(Context context, final ContentCenterEntity entity) {
        String channel = entity.getFolder() + "\\" + entity.getName();
        String selection = "channel=?";
        String[] selectionArg = {channel};
        Application.getInstance().getContentResolver().delete(DataProvider.CONTENT_URI_SUBSCRIBEDCHANNEL, selection, selectionArg);
        entity.setSubscribed(0);
        
        {
            String channel2 = entity.getFolder() + "\\" + entity.getName() + "\\" + entity.getName();
            String where = "channelName=?";
            String[] selectionArg2 = {channel2};
            int row = Application.getInstance().getContentResolver().delete(DataProvider.CONTENT_URI_FEEDDATABSE, where, selectionArg2);
            if (row != 0) {
                ComponentName component = new ComponentName(context, SubscribedFeedService.class);
                Intent intent = new Intent(SubscribedFeedService.IMM_UPDATE_SERVICE);
                intent.setComponent(component);
                context.startService(intent);
            }
        }
        
        ContentValues values = new ContentValues();
        values.clear();
        values.put("subscribed", entity.getSubscribed());
        String where = "name=? AND folder=?";
        String[] selectionArgs = {entity.getName(), entity.getFolder()};
        Application.getInstance().getContentResolver().update(DataProvider.CONTENT_URI_CONTENTCENTER, values, where, selectionArgs);
    }
}
