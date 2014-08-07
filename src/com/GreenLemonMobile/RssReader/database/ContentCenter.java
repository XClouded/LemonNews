package com.GreenLemonMobile.RssReader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class ContentCenter extends MyDatabase {
	
	public static final String TABLENAME = "content_center";
	private SQLiteDatabase db;
	
	public ContentCenter() {}
	
	@Override
	public void attach(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		this.db = db;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sqlCommand = "CREATE TABLE IF NOT EXISTS " + TABLENAME + " (name TEXT, subname TEXT, url TEXT, image TEXT, tag INTEGER, subtag INTEGER, folder TEXT, subscribed INTEGER);";
		db.execSQL(sqlCommand);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return db.query(TABLENAME, projection, selection, selectionArgs, null, null, sortOrder);
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		long id = db.insert(TABLENAME, null, values);
		return Uri.parse(uri.toString() + "/" + id);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return db.delete(TABLENAME, selection, selectionArgs);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return db.update(TABLENAME, values, selection, selectionArgs);
	}
}
