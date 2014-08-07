package com.GreenLemonMobile.util;

import android.database.Cursor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class CursorUtils {
    private CursorUtils()
    {
    }

    public static byte[] getBlob(Cursor cursor, String s)
    {
        return cursor.getBlob(cursor.getColumnIndexOrThrow(s));
    }

    public static boolean getBoolean(Cursor cursor, int i)
    {
        boolean flag;
        if(cursor.getInt(i) != 0)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public static boolean getBoolean(Cursor cursor, String s)
    {
        boolean flag;
        if(cursor.getInt(cursor.getColumnIndexOrThrow(s)) != 0)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public static double getDouble(Cursor cursor, String s)
    {
        return cursor.getDouble(cursor.getColumnIndexOrThrow(s));
    }

    public static Double getDoubleObject(Cursor cursor, int i)
    {
        Double double1;
        if(cursor.isNull(i))
            double1 = null;
        else
            double1 = Double.valueOf(cursor.getDouble(i));
        return double1;
    }

    public static Double getDoubleObject(Cursor cursor, String s)
    {
        return getDoubleObject(cursor, cursor.getColumnIndexOrThrow(s));
    }

    public static int getInt(Cursor cursor, String s)
    {
        return cursor.getInt(cursor.getColumnIndexOrThrow(s));
    }

    public static Integer getInteger(Cursor cursor, int i)
    {
        Integer integer;
        if(cursor.isNull(i))
            integer = null;
        else
            integer = Integer.valueOf(cursor.getInt(i));
        return integer;
    }

    public static Integer getInteger(Cursor cursor, String s)
    {
        return getInteger(cursor, cursor.getColumnIndexOrThrow(s));
    }

    public static long getLong(Cursor cursor, String s)
    {
        return cursor.getLong(cursor.getColumnIndexOrThrow(s));
    }

    public static Long getLongObject(Cursor cursor, int i)
    {
        Long long1;
        if(cursor.isNull(i))
            long1 = null;
        else
            long1 = Long.valueOf(cursor.getLong(i));
        return long1;
    }

    public static Long getLongObject(Cursor cursor, String s)
    {
        return getLongObject(cursor, cursor.getColumnIndexOrThrow(s));
    }

    public static String getString(Cursor cursor, String s)
    {
        return cursor.getString(cursor.getColumnIndexOrThrow(s));
    }

    public static List getStringValues(Cursor cursor, int i)
    {
        java.util.ArrayList arraylist = new ArrayList();
        cursor.moveToPosition(-1);
        for(; cursor.moveToNext(); arraylist.add(cursor.getString(i)));
        cursor.moveToPosition(-1);
        return arraylist;
    }

    public static List getValuesAtPositions(Cursor cursor, int i, List list)
    {
        java.util.ArrayList arraylist = new ArrayList();
        Integer integer;
        Iterator iterator = list.iterator();
        do
        {
            if(!iterator.hasNext())
                return arraylist;
            integer = (Integer)iterator.next();
            if(!cursor.moveToPosition(integer.intValue()))
                break;
            arraylist.add(cursor.getString(i));
        } while(true);
        
        return arraylist;
    }
}
