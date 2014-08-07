package com.GreenLemonMobile.database;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import com.GreenLemonMobile.entity.CacheFile;
import com.GreenLemonMobile.util.FileService.Directory;
import com.GreenLemonMobile.util.FormatUtils;
import com.GreenLemonMobile.util.Log;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class CacheFileTable { 

	public final static String TABLE_NAME = "cache_file";
	public final static String TB_COLUMN_FIRST_NAME = "first_name";
	public final static String TB_COLUMN_LAST_NAME = "last_name";
	public final static String TB_COLUMN_CLEAN_TIME = "clean_time";
	public final static String TB_COLUMN_DIR_PATH = "dir_path";
	public final static String TB_COLUMN_DIR_SPACE = "dir_space";

	/**
	 * ����
	 */
	public static void create(SQLiteDatabase database) {
		// ������
		final String CREATE_TABLE_CACHE_FILE = "CREATE TABLE "//
				+ TABLE_NAME//
				+ "('id' INTEGER PRIMARY KEY  NOT NULL ,"//
				+ TB_COLUMN_FIRST_NAME + " TEXT,"//
				+ TB_COLUMN_LAST_NAME + " TEXT,"//
				+ TB_COLUMN_CLEAN_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP,"//
				+ TB_COLUMN_DIR_PATH + " TEXT,"//
				+ TB_COLUMN_DIR_SPACE + " INTEGER"//
				+ ")";
		database.execSQL(CREATE_TABLE_CACHE_FILE);

		// ����ʱ������
		final String CREATE_INDEX_CLEAN_TIME = "CREATE INDEX "
				+ "clean_time_index" + //
				" ON " + TABLE_NAME + //
				"(" + TB_COLUMN_CLEAN_TIME + ")";
		database.execSQL(CREATE_INDEX_CLEAN_TIME);

		// ������������
		final String CREATE_INDEX_NAME = "CREATE INDEX " + "name_index" + //
				" ON " + TABLE_NAME + //
				"(" + TB_COLUMN_FIRST_NAME + ", " + TB_COLUMN_LAST_NAME + ")";
		database.execSQL(CREATE_INDEX_NAME);
	}

	/**
	 * ����
	 */
	public static void upgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		// ���ʱ������
		database.execSQL("drop index if exists clean_time_index");

		// �����������
		database.execSQL("drop index if exists name_index");

		// �����
		database.execSQL("drop table if exists " + TABLE_NAME);
	}

	/**
	 * �����������Ҳ����ڲŷ��� false ���������� true
	 */
	public static boolean isExpired(File file) {
		if (Log.D) {
			Log.d("Temp", "CacheFileTable isExpired() -->> ");
		}
		boolean result = true;

		CacheFile cacheFile = new CacheFile(file);

		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = CacheDBHelper.getDatabase();

			// ����
			String selection = TB_COLUMN_FIRST_NAME + " = ? AND "//
					+ TB_COLUMN_LAST_NAME + " = ?";

			// ����
			String[] selectionArgs = new String[] { cacheFile.getFirstName(),//
					cacheFile.getLastName() };

			// ��ѯ
			cursor = database.query(TABLE_NAME, null, selection, selectionArgs,
					null, null, null);
			if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
				String dateStr = cursor.getString(cursor
						.getColumnIndex(TB_COLUMN_CLEAN_TIME));
				Date date = FormatUtils.parseDate(dateStr);
				long fileDateTime = date.getTime();
				long nowDateTime = new Date().getTime();
				if (fileDateTime > nowDateTime) {
					result = false;
				}
			}// ��������������������ڴ���
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			CacheDBHelper.closeDatabase();
		}
		if (Log.D) {
			Log.d("Temp", "CacheFileTable isExpired() -->> " + result);
		}
		return result;
	}

	/**
	 * ��������
	 */
	public static synchronized void insertOrUpdate(CacheFile cacheFile) {
		if (Log.D) {
			Log.d("Temp", "CacheFileTable insertOrUpdate() -->> ");
		}
		SQLiteDatabase database = null;
		Cursor cursor = null;
		try {
			database = CacheDBHelper.getDatabase();

			// �ļ�
			ContentValues contentValues = new ContentValues();
			contentValues.put(TB_COLUMN_FIRST_NAME, cacheFile.getFirstName());
			contentValues.put(TB_COLUMN_LAST_NAME, cacheFile.getLastName());
			contentValues.put(TB_COLUMN_CLEAN_TIME, cacheFile.getCleanTime()
					.toLocaleString());
			// Ŀ¼
			Directory directory = cacheFile.getDirectory();
			contentValues.put(TB_COLUMN_DIR_PATH, directory.getPath());
			contentValues.put(TB_COLUMN_DIR_SPACE, directory.getSpace());

			String selection = TB_COLUMN_FIRST_NAME + " = ? AND "
					+ TB_COLUMN_LAST_NAME + " = ?";
			String[] selectionArgs = new String[] { cacheFile.getFirstName(),
					cacheFile.getLastName() };

			cursor = database.query(TABLE_NAME, null, selection, selectionArgs,
					null, null, null);
			if (cursor != null && cursor.getCount() > 0) {
				database.update(TABLE_NAME, contentValues, selection,
						selectionArgs);
			} else {
				database.insert(TABLE_NAME, null, contentValues);
			}

			if (Log.D) {
				Log.d("Temp", "CacheFileTable insertOrUpdate() -->> ok");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			CacheDBHelper.closeDatabase();
		}
	}

	/**
	 * ɾ��
	 */
	public static synchronized void delete(CacheFile cacheFile) {
		if (Log.D) {
			Log.d("Temp", "CacheFileTable delete() -->> ");
		}
		SQLiteDatabase database = null;
		try {
			database = CacheDBHelper.getDatabase();
			String whereClause = TB_COLUMN_FIRST_NAME + " = ? AND "
					+ TB_COLUMN_LAST_NAME + " = ?";
			database.delete(
					TABLE_NAME,
					whereClause,
					new String[] { cacheFile.getFirstName(),
							cacheFile.getLastName() });
			if (Log.D) {
				Log.d("Temp", "CacheFileTable delete() -->> ok");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			CacheDBHelper.closeDatabase();
		}
	}

	/**
	 * ��ѯ
	 */
	public static ArrayList<CacheFile> getListByClean() {
		if (Log.D) {
			Log.d("Temp", "CacheFileTable getListByClean() -->> ");
		}

		ArrayList<CacheFile> list = new ArrayList<CacheFile>();

		SQLiteDatabase database = null;
		Cursor cursor = null;

		try {

			database = CacheDBHelper.getDatabase();

			String[] columns = { TB_COLUMN_FIRST_NAME, TB_COLUMN_LAST_NAME,
					TB_COLUMN_CLEAN_TIME, TB_COLUMN_DIR_PATH,
					TB_COLUMN_DIR_SPACE };

			String selection = TB_COLUMN_CLEAN_TIME + " < ?";
			String[] selectionArgs = new String[] { new Date().toLocaleString() };

			cursor = database.query(TABLE_NAME, columns, selection,
					selectionArgs, null, null, null);

			if (cursor != null && cursor.moveToFirst()) {
				do {
					// �ļ�
					CacheFile cacheFile = new CacheFile();
					cacheFile.setFirstName(cursor.getString(cursor
							.getColumnIndex(TB_COLUMN_FIRST_NAME)));
					cacheFile.setLastName(cursor.getString(cursor
							.getColumnIndex(TB_COLUMN_LAST_NAME)));
					String dateStr = cursor.getString(cursor
							.getColumnIndex(TB_COLUMN_CLEAN_TIME));
					Date date = FormatUtils.parseDate(dateStr);
					cacheFile.setCleanTime(date);
					// Ŀ¼
					String path = cursor.getString(cursor
							.getColumnIndex(TB_COLUMN_DIR_PATH));
					int space = cursor.getInt(cursor
							.getColumnIndex(TB_COLUMN_DIR_SPACE));
					cacheFile.setDirectory(new Directory(path, space));
					list.add(cacheFile);
				} while ((cursor.moveToNext()));
			}

			if (Log.D) {
				Log.d("Temp", "CacheFileTable getListByClean() -->> ok");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
			CacheDBHelper.closeDatabase();
		}

		return list;
	}
}
