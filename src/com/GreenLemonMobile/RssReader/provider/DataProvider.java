package com.GreenLemonMobile.RssReader.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.GreenLemonMobile.RssReader.R;
import com.GreenLemonMobile.RssReader.database.ContentCenter;
import com.GreenLemonMobile.RssReader.database.DatabaseHelper;
import com.GreenLemonMobile.RssReader.database.FeedDatabase;
import com.GreenLemonMobile.RssReader.database.SubscribedChannel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

public class DataProvider extends ContentProvider {
    
	public static final String SHEME = "content://";
	public static final String AUTHORITY = "com.GreenLemonMobile.RssReader.provider.DataProvider";
	
	public static final Uri CONTENT_URI_CONTENTCENTER = Uri.parse(SHEME + AUTHORITY + "/" + ContentCenter.TABLENAME);
	public static final Uri CONTENT_URI_SUBSCRIBEDCHANNEL = Uri.parse(SHEME + AUTHORITY + "/" + SubscribedChannel.TABLENAME);
	public static final Uri CONTENT_URI_FEEDDATABSE = Uri.parse(SHEME + AUTHORITY + "/" + FeedDatabase.TABLENAME);
	private static final int DATABASE_CONTENT_CENTER = 1;
	private static final int DATABASE_SUBSCRIBED_CHANNEL = 2;
	private static final int DATABASE_FEED_DATABASE = 3;
	
	private static final UriMatcher sUriMatcher;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, ContentCenter.TABLENAME, DATABASE_CONTENT_CENTER);
		sUriMatcher.addURI(AUTHORITY, SubscribedChannel.TABLENAME, DATABASE_SUBSCRIBED_CHANNEL);
		sUriMatcher.addURI(AUTHORITY, FeedDatabase.TABLENAME, DATABASE_FEED_DATABASE);
	}
	
	private DatabaseHelper dbHelper;

	@Override
	public boolean onCreate() {
        File file = getContext().getDatabasePath(DatabaseHelper.DATABASENAME2);
        if (!file.exists()) {
            int rawId = R.raw.rss_en;
            if (Locale.getDefault().getCountry().toLowerCase().contains("cn") || Locale.getDefault().getLanguage().toLowerCase().contains("zh")) 
                rawId = R.raw.rss_zh;
            try {
                InputStream in = getContext().getResources().openRawResource(rawId);
                file.getParentFile().mkdirs();
                file.createNewFile();
                OutputStream out = new FileOutputStream(file);
                byte[] buffer = new byte[DatabaseHelper.BUFFER_SIZE];
                int count = 0;
                while ((count = in.read(buffer)) > 0)
                    out.write(buffer, 0, count);
                
                out.close();                
                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }           
        }
        
		dbHelper = new DatabaseHelper(this.getContext());
		SQLiteDatabase db = dbHelper.getReadableDatabase();
		dbHelper.attach(db);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		switch (sUriMatcher.match(uri)) {
		case DATABASE_CONTENT_CENTER:
			return dbHelper.getContentCenter().query(uri, projection, selection, selectionArgs, sortOrder);
		case DATABASE_SUBSCRIBED_CHANNEL:
			return dbHelper.getSubscribedChannel().query(uri, projection, selection, selectionArgs, sortOrder);
		case DATABASE_FEED_DATABASE:
		    return dbHelper.getFeedDatabase().query(uri, projection, selection, selectionArgs, sortOrder);
		}
		return null;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case DATABASE_CONTENT_CENTER:
			return dbHelper.getContentCenter().getType(uri);
		case DATABASE_SUBSCRIBED_CHANNEL:
			return dbHelper.getSubscribedChannel().getType(uri);
	     case DATABASE_FEED_DATABASE:
	         return dbHelper.getFeedDatabase().getType(uri);
		}
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case DATABASE_CONTENT_CENTER:
			return dbHelper.getContentCenter().insert(uri, values);
		case DATABASE_SUBSCRIBED_CHANNEL:
			return dbHelper.getSubscribedChannel().insert(uri, values);
		case DATABASE_FEED_DATABASE:
		    return dbHelper.getFeedDatabase().insert(uri, values);
		}
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case DATABASE_CONTENT_CENTER:
			return dbHelper.getContentCenter().delete(uri, selection, selectionArgs);
		case DATABASE_SUBSCRIBED_CHANNEL:
			return dbHelper.getSubscribedChannel().delete(uri, selection, selectionArgs);
		case DATABASE_FEED_DATABASE:
		    return dbHelper.getFeedDatabase().delete(uri, selection, selectionArgs);
		}
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case DATABASE_CONTENT_CENTER:
			return dbHelper.getContentCenter().update(uri, values, selection, selectionArgs);
		case DATABASE_SUBSCRIBED_CHANNEL:
			return dbHelper.getSubscribedChannel().update(uri, values, selection, selectionArgs);
		case DATABASE_FEED_DATABASE:
		    return dbHelper.getFeedDatabase().update(uri, values, selection, selectionArgs);
		}
		return 0;
	}

}
