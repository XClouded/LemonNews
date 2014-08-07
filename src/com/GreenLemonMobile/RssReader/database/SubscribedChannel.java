package com.GreenLemonMobile.RssReader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class SubscribedChannel extends MyDatabase {
	
	public static final String TABLENAME = "subscribed_channel";
	
	private SQLiteDatabase db;
	
	public SubscribedChannel() {}
	
	@Override
	public void attach(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		this.db = db;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCommand = "CREATE TABLE IF NOT EXISTS " + TABLENAME + " (name TEXT, url TEXT, image TEXT, channel TEXT, order_index INTEGER, unread_news INTEGER, latest_refresh_date TEXT);";
		db.execSQL(sqlCommand);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		return db.query(TABLENAME, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		long id = db.insert(TABLENAME, null, values);
		return Uri.parse(uri.toString() + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return db.delete(TABLENAME, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return db.update(TABLENAME, values, selection, selectionArgs);
	}
}
