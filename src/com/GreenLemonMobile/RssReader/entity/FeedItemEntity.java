package com.GreenLemonMobile.RssReader.entity;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.GreenLemonMobile.RssReader.Application;
import com.GreenLemonMobile.RssReader.provider.DataProvider;
import com.GreenLemonMobile.cipher.MD5Calculator;
import com.GreenLemonMobile.feed4j.bean.FeedItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class FeedItemEntity implements Parcelable {
    private static final int ITEMS_PER_PAGE = 10;
    
    /**
     * The item title.
     */
    public String title = null;

    /**
     * The item author.
     */
    public String author = null;

    /**
     * The item creator.
     */
    public String creator = null;

    /**
     * The item link.
     */
    public String link = null;

    /**
     * The URL for the comments to the item.
     */
    public String comments = null;

    /**
     * The item description as HTML code.
     */
    public String descriptionAsHTML = null;

    /**
     * The item description as plain text.
     */
    public String descriptionAsText = null;

    /**
     * The item publication date.
     */
    public String pubDate = null;
    
    public String channelName;
    
    public String linkMD5;
    
    public boolean hasRead;
    
    public FeedItemEntity() {        
    }
    
    public FeedItemEntity(Parcel in) {
        title = in.readString();
        author = in.readString();
        creator = in.readString();
        link = in.readString();
        comments = in.readString();
        descriptionAsHTML = in.readString();
        descriptionAsText = in.readString();
        pubDate = in.readString();
        channelName = in.readString();
        linkMD5 = in.readString();
        hasRead = in.readInt() == 1 ? true : false;
    }
    
    public static void getFeedsList(ArrayList<FeedItemEntity> list, final String channelName) {
        String selection = "channelName=?";
        String[] selectionArg ={channelName};
        String sortOrder = "pubDate DESC";
        Cursor cursor = Application.getInstance().getApplicationContext().getContentResolver().query(DataProvider.CONTENT_URI_FEEDDATABSE, null, selection, selectionArg, sortOrder);
        
        if (cursor != null && cursor.getCount() > 0) {
            int titleIndex = cursor.getColumnIndex("title");
            int authorIndex = cursor.getColumnIndex("author");
            int creatorIndex = cursor.getColumnIndex("creator");
            int linkIndex = cursor.getColumnIndex("link");
            int commentsIndex = cursor.getColumnIndex("comments");
            int descriptionAsHTMLIndex = cursor.getColumnIndex("descriptionAsHTML");
            int descriptionAsTextIndex = cursor.getColumnIndex("descriptionAsText");
            int pubDateIndex = cursor.getColumnIndex("pubDate");
            int channelNameIndex = cursor.getColumnIndex("channelName");
            int linkMD5Index = cursor.getColumnIndex("linkMD5");
            int hasReadIndex = cursor.getColumnIndex("hasRead");
            while (cursor.moveToNext()) {
                FeedItemEntity entity = new FeedItemEntity();
                entity.title = cursor.getString(titleIndex);
                entity.author = cursor.getString(authorIndex);
                entity.creator = cursor.getString(creatorIndex);
                entity.link = cursor.getString(linkIndex);
                entity.linkMD5 = cursor.getString(linkMD5Index);
                entity.comments = cursor.getString(commentsIndex);
                entity.descriptionAsHTML = cursor.getString(descriptionAsHTMLIndex);
                entity.descriptionAsText = cursor.getString(descriptionAsTextIndex);
                entity.pubDate = cursor.getString(pubDateIndex);
                entity.channelName = cursor.getString(channelNameIndex);
                entity.hasRead = cursor.getInt(hasReadIndex) == 1 ? true : false;
                list.add(entity);
            }
        }
        
        if (cursor != null)
            cursor.close();
    }
    
    public static void clearChannel(final String channelName) {
        String where = "channelName=?";
        String[] selectionArgs = {channelName};
        Application.getInstance().getContentResolver().delete(DataProvider.CONTENT_URI_FEEDDATABSE, where, selectionArgs);
    }
    
    public static void clearAll() {
        Application.getInstance().getContentResolver().delete(DataProvider.CONTENT_URI_FEEDDATABSE, null, null);
    }
    
    public static void getFeedsList(ArrayList<FeedItemEntity> list, final String channelName, final int pageIndex) {
        String selection = "channelName=?";
        String[] selectionArg ={channelName};
        String sortOrder = "pubDate DESC LIMIT " + Integer.toString(pageIndex * ITEMS_PER_PAGE) + "," + Integer.toString(ITEMS_PER_PAGE);
        Cursor cursor = Application.getInstance().getApplicationContext().getContentResolver().query(DataProvider.CONTENT_URI_FEEDDATABSE, null, selection, selectionArg, sortOrder);
        
        if (cursor != null && cursor.getCount() > 0) {
            int titleIndex = cursor.getColumnIndex("title");
            int authorIndex = cursor.getColumnIndex("author");
            int creatorIndex = cursor.getColumnIndex("creator");
            int linkIndex = cursor.getColumnIndex("link");
            int commentsIndex = cursor.getColumnIndex("comments");
            int descriptionAsHTMLIndex = cursor.getColumnIndex("descriptionAsHTML");
            int descriptionAsTextIndex = cursor.getColumnIndex("descriptionAsText");
            int pubDateIndex = cursor.getColumnIndex("pubDate");
            int channelNameIndex = cursor.getColumnIndex("channelName");
            int linkMD5Index = cursor.getColumnIndex("linkMD5");
            int hasReadIndex = cursor.getColumnIndex("hasRead");
            while (cursor.moveToNext()) {
                FeedItemEntity entity = new FeedItemEntity();
                entity.title = cursor.getString(titleIndex);
                entity.author = cursor.getString(authorIndex);
                entity.creator = cursor.getString(creatorIndex);
                entity.link = cursor.getString(linkIndex);
                entity.linkMD5 = cursor.getString(linkMD5Index);
                entity.comments = cursor.getString(commentsIndex);
                entity.descriptionAsHTML = cursor.getString(descriptionAsHTMLIndex);
                entity.descriptionAsText = cursor.getString(descriptionAsTextIndex);
                entity.pubDate = cursor.getString(pubDateIndex);
                entity.channelName = cursor.getString(channelNameIndex);
                entity.hasRead = cursor.getInt(hasReadIndex) == 1 ? true : false;
                list.add(entity);
            }
        }
        
        if (cursor != null)
            cursor.close();
    }
    
    public static boolean save(final String channelName, final FeedItem feedItem) {
        boolean newAdded = false;
        
        if (!TextUtils.isEmpty(feedItem.getLink().toString())) {
            String linkMD5 = MD5Calculator.calculateMD5(feedItem.getLink().toString());
            
            String selection = "linkMD5=?";
            String[] selectionArg ={linkMD5};
            Cursor cursor = Application.getInstance().getApplicationContext().getContentResolver().query(DataProvider.CONTENT_URI_FEEDDATABSE, null, selection, selectionArg, null);
            if (cursor != null && cursor.getCount() > 0) {
                newAdded = false;
            } else {
                newAdded = true;
                FeedItemEntity entity = new FeedItemEntity();
                
                entity.title = feedItem.getTitle();
                entity.author = feedItem.getAuthor();
                entity.creator = feedItem.getCreator();
                entity.link = feedItem.getLink().toString();
                entity.comments = feedItem.getComments() != null ? feedItem.getComments().toString() : "";
                entity.descriptionAsHTML = feedItem.getDescriptionAsHTML();
                entity.descriptionAsText = feedItem.getDescriptionAsText();
                entity.channelName = channelName;
                entity.hasRead = false;
                Date date = null;
                if (feedItem.getPubDate() != null) {
                    date = feedItem.getPubDate();
                } else {
                    date = new Date(System.currentTimeMillis());
                }
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                df.setTimeZone(TimeZone.getDefault());
                entity.pubDate = df.format(date);
                
                entity.linkMD5 = linkMD5;
                //(title TEXT, author TEXT, creator TEXT, link TEXT, comments TEXT, descriptionAsHTML TEXT, descriptionAsText TEXT, pubDate TEXT, channelName TEXT, linkMD5 TEXT, hasRead INTEGER);";
                ContentValues values = new ContentValues();
                values.put("title", entity.title);
                values.put("author", entity.author);
                values.put("creator", entity.creator);
                values.put("link", entity.link);
                values.put("comments", entity.comments);
                values.put("descriptionAsHTML", entity.descriptionAsHTML);
                values.put("descriptionAsText", entity.descriptionAsText);
                values.put("pubDate", entity.pubDate);
                values.put("channelName", entity.channelName);
                values.put("linkMD5", entity.linkMD5);
                values.put("hasRead", entity.hasRead);
                Application.getInstance().getContentResolver().insert(DataProvider.CONTENT_URI_FEEDDATABSE, values);
            }
            
            if (cursor != null)
                cursor.close();
        }
        
        return newAdded;
    }
    
    public static void getLatestSubscribedItems(ArrayList<FeedItemEntity> latestFeeds, int countLimit) {
        latestFeeds.clear();
        String sortOrder = "pubDate DESC LIMIT 0, " + Integer.toString(countLimit);
        Cursor cursor = Application.getInstance().getApplicationContext().getContentResolver().query(DataProvider.CONTENT_URI_FEEDDATABSE, null, null, null, sortOrder);
        
        if (cursor != null && cursor.getCount() > 0) {
            int titleIndex = cursor.getColumnIndex("title");
            int authorIndex = cursor.getColumnIndex("author");
            int creatorIndex = cursor.getColumnIndex("creator");
            int linkIndex = cursor.getColumnIndex("link");
            int commentsIndex = cursor.getColumnIndex("comments");
            int descriptionAsHTMLIndex = cursor.getColumnIndex("descriptionAsHTML");
            int descriptionAsTextIndex = cursor.getColumnIndex("descriptionAsText");
            int pubDateIndex = cursor.getColumnIndex("pubDate");
            int channelNameIndex = cursor.getColumnIndex("channelName");
            int linkMD5Index = cursor.getColumnIndex("linkMD5");
            int hasReadIndex = cursor.getColumnIndex("hasRead");
            while (cursor.moveToNext()) {
                FeedItemEntity entity = new FeedItemEntity();
                entity.title = cursor.getString(titleIndex);
                entity.author = cursor.getString(authorIndex);
                entity.creator = cursor.getString(creatorIndex);
                entity.link = cursor.getString(linkIndex);
                entity.linkMD5 = cursor.getString(linkMD5Index);
                entity.comments = cursor.getString(commentsIndex);
                entity.descriptionAsHTML = cursor.getString(descriptionAsHTMLIndex);
                entity.descriptionAsText = cursor.getString(descriptionAsTextIndex);
                entity.pubDate = cursor.getString(pubDateIndex);
                entity.channelName = cursor.getString(channelNameIndex);
                entity.hasRead = cursor.getInt(hasReadIndex) == 1 ? true : false;
                latestFeeds.add(entity);
            }
        }
        
        if (cursor != null)
            cursor.close();
    }
    
    public void setRead(final boolean hasRead) {
        if (hasRead != this.hasRead) {
            this.hasRead = hasRead;
            ContentValues values = new ContentValues();
            values.put("hasRead", hasRead);
            String where = "linkMD5=? AND channelName = ?";
            String[] selectionArgs = {linkMD5, channelName};
            Application.getInstance().getContentResolver().update(DataProvider.CONTENT_URI_FEEDDATABSE, values, where, selectionArgs);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(creator);
        dest.writeString(link);
        dest.writeString(comments);
        dest.writeString(descriptionAsHTML);
        dest.writeString(descriptionAsText);
        dest.writeString(pubDate);
        dest.writeString(channelName);
        dest.writeString(linkMD5);
        dest.writeInt(hasRead ? 1 : 0);
    }
    
    public static final Parcelable.Creator<FeedItemEntity> CREATOR = new Parcelable.Creator<FeedItemEntity>() {

        @Override
        public FeedItemEntity createFromParcel(Parcel source) {
            return new FeedItemEntity(source);
        }

        @Override
        public FeedItemEntity[] newArray(int size) {
            return new FeedItemEntity[size];
        }
        
    };
}
