package com.codeminders.imageshackdroid.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class OpenHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "ImageshackDroid.db";
    private static final int DATABASE_VERSION = 8;

    private static final String CREATE_TABLE_LINKS =
            "create table links"
                    + " (_id integer primary key, "
                    + "user text,"
                    + "name text, "
                    + "date integer, "
                    + "size text, "
                    + "image_link text, "
                    + "thumb_link text, "
                    + "thumb_html text, "
                    + "thumb_bb text, "
                    + "thumb_bb2 text, "
                    + "yfrog_link text, "
                    + "yfrog_thumb text, "
                    + "ad_link text,"
                    + "image_id integer,"
                    + "video_id integer);";

    private static final String CREATE_TABLE_IMAGES =
            "create table images"
                    + " (_id integer primary key autoincrement, "
                    + "image_html text, "
                    + "image_bb text, "
                    + "image_bb2 text);";


    private static final String CREATE_TABLE_VIDEOS =
            "create table videos"
                    + " (_id integer primary key autoincrement, "
                    + "frame_link text, "
                    + "frame_html text, "
                    + "frame_bb text, "
                    + "frame_bb2 text, "
                    + "video_embed text);";

    private static final String CREATE_TABLE_SYNC =
            "create table sync"
                    + " (_id integer primary key autoincrement, "
                    + "user text,"
                    + "image_sync integer,"
                    + "video_sync integer);";

    private static final String CREATE_TABLE_HISTORY =
            "create table history"
                    + " (_id integer primary key autoincrement, "
                    + "user text,"
                    + "path text,"
                    + "type integer);";


    public OpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_TABLE_LINKS);
        sqLiteDatabase.execSQL(CREATE_TABLE_IMAGES);
        sqLiteDatabase.execSQL(CREATE_TABLE_VIDEOS);
        sqLiteDatabase.execSQL(CREATE_TABLE_SYNC);
        sqLiteDatabase.execSQL(CREATE_TABLE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists images");
        sqLiteDatabase.execSQL("drop table if exists videos");
        sqLiteDatabase.execSQL("drop table if exists links");
        sqLiteDatabase.execSQL("drop table if exists sync");
        sqLiteDatabase.execSQL("drop table if exists history");
        onCreate(sqLiteDatabase);
    }

}
