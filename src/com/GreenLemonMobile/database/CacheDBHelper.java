package com.GreenLemonMobile.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.GreenLemonMobile.util.Log;
import com.GreenLemonMobile.util.MyApplication;

public class CacheDBHelper {
	private SQLiteDatabase db = null;
	private static int versionCode = 1;
	private Cursor c;

	private static final String DB_NAME = "cache.db";

	private Context mContext;
	private static OpenHelper mOpenHelper;
	
	private static final String TAG="DBHelperUtil";

	public static synchronized SQLiteDatabase getDatabase() {
		if (null == mOpenHelper) {
			try {
				versionCode = MyApplication.getInstance().getPackageManager().getPackageInfo(MyApplication.getInstance().getPackageName(), 0).versionCode;
			} catch (Exception e) {
				e.printStackTrace();
			}
			mOpenHelper = new OpenHelper(MyApplication.getInstance(), DB_NAME, null, versionCode);
		}
		try {
			SQLiteDatabase writableDatabase = mOpenHelper.getWritableDatabase();
			if (Log.D) {
				Log.d("Temp", "writableDatabase -->> " + writableDatabase);
			}
			return writableDatabase;
		} catch (Exception e) {
			e.printStackTrace();
			SQLiteDatabase readableDatabase = mOpenHelper.getReadableDatabase();
			if (Log.D) {
				Log.d("Temp", "readableDatabase -->> " + readableDatabase);
			}

			return readableDatabase;
		}
	}

	public static void closeDatabase() {
		// mOpenHelper.close();
	}

	public CacheDBHelper(Context context) {
		mContext = context;
	}

	private synchronized void openDatabase() {
		db = getDatabase();
	}
}

class OpenHelper extends SQLiteOpenHelper {

	public OpenHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 缓存文件
		CacheFileTable.create(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion < newVersion) {
			if (Log.I) {
				Log.i("onUpgrade", "++++++++++oldVersion:" + oldVersion + "newVersion:" + newVersion);
			}
			// 缓存文件
			CacheFileTable.upgrade(db, oldVersion, newVersion);
			onCreate(db);
		}
	}
}
