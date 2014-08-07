package com.GreenLemonMobile.RssReader.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	public static final String DATABASENAME = "rss.db";
	public static final String DATABASENAME2 = "content.db";
	
	public static final String ROOT_FOLDER = "root";
	
	public final static int BUFFER_SIZE = 1024*8;
	
	private static final int dbVersion = 1;
	
	private Context context;
	
	private MyDatabase contentCenter = new ContentCenter();

	private MyDatabase subscribedChannel = new SubscribedChannel();
	
	private MyDatabase feedDatabase = new FeedDatabase();

	private DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
	}
	
	public DatabaseHelper(Context context) {
		this(context, DATABASENAME, null, dbVersion);
	}
	
	public void attach(SQLiteDatabase db) {		
		subscribedChannel.attach(db);
		feedDatabase.attach(db);
		
		SQLiteDatabase db2 = context.openOrCreateDatabase(DATABASENAME2, 0, null);
		contentCenter.attach(db2);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    contentCenter.onCreate(db);
        subscribedChannel.onCreate(db);
        feedDatabase.onCreate(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		contentCenter.onUpgrade(db, oldVersion, newVersion);
		subscribedChannel.onUpgrade(db, oldVersion, newVersion);
		feedDatabase.onUpgrade(db, oldVersion, newVersion);
	}

	public MyDatabase getSubscribedChannel() {
		return subscribedChannel;
	}
	
	public MyDatabase getContentCenter() {
		return contentCenter;
	}
	
	public MyDatabase getFeedDatabase() {
	    return feedDatabase;
	}
}
