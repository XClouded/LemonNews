package com.GreenLemonMobile.RssReader.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public abstract class MyDatabase {
	public abstract void attach(SQLiteDatabase db);
	
	/**
	 * SQLiteOpenHelper
	 */
	public abstract void onCreate(SQLiteDatabase db);
	public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);
	
	/**
	 * ContentProvider
	 */
	public abstract Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder);
	public abstract String getType(Uri uri);

	public abstract Uri insert(Uri uri, ContentValues values);

	public abstract int delete(Uri uri, String selection, String[] selectionArgs);

	public abstract int update(Uri uri, ContentValues values, String selection, String[] selectionArgs);
}