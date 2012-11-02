package com.codeminders.imageshackdroid.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.codeminders.imageshackdroid.Constants;
import com.codeminders.imageshackdroid.model.ImageLinks;
import com.codeminders.imageshackdroid.model.Links;
import com.codeminders.imageshackdroid.model.VideoLinks;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class DataHelper {
    private SQLiteDatabase db;
    private OpenHelper openHelper;


    public DataHelper(Context context) {
        openHelper = new OpenHelper(context);
    }


    public void write(Links links, String user) {
        long id = 0;
        db = openHelper.getWritableDatabase();
        if (links instanceof ImageLinks) {
            id = writeILink((ImageLinks) links);
        } else if (links instanceof VideoLinks) {
            id = writeVLink((VideoLinks) links);
        }
        ContentValues cv = new ContentValues();
        cv.put("_id", links.getId());
        cv.put("user", user);
        cv.put("name", links.getName());
        cv.put("date", links.getDate().getTime());
        cv.put("size", links.getSize());
        cv.put("image_link", links.getImage_link());
        cv.put("thumb_link", links.getThumb_link());
        cv.put("thumb_html", links.getThumb_html());
        cv.put("thumb_bb", links.getThumb_bb());
        cv.put("thumb_bb2", links.getThumb_bb2());
        cv.put("yfrog_link", links.getYfrog_link());
        cv.put("yfrog_thumb", links.getYfrog_thumb());
        cv.put("ad_link", links.getAd_link());
        if (links instanceof ImageLinks) {
            cv.put("image_id", id);
        } else if (links instanceof VideoLinks) {
            cv.put("video_id", id);
        }

        db.insert("links", null, cv);
        db.close();
    }

    private long writeILink(ImageLinks imageLinks) {
        ContentValues cv = new ContentValues();
        cv.put("image_html", imageLinks.getImage_html());
        cv.put("image_bb", imageLinks.getImage_bb());
        cv.put("image_bb2", imageLinks.getImage_bb2());

        return db.insert("images", null, cv);
    }

    private long writeVLink(VideoLinks videoLinks) {
        ContentValues cv = new ContentValues();
        cv.put("frame_link", videoLinks.getFrame_link());
        cv.put("frame_html", videoLinks.getFrame_html());
        cv.put("frame_bb", videoLinks.getFrame_bb());
        cv.put("frame_bb2", videoLinks.getFrame_bb2());
        cv.put("video_embed", videoLinks.getVideo_embed());

        return db.insert("videos", null, cv);
    }

    public void writeHistory(String user, String path, int type) {
        db = openHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user", user);
        cv.put("path", path);
        cv.put("type", type);
        db.insert("history", null, cv);
        db.close();
    }

    public void removeLink(int id) {
        db = openHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from links where _id = " + id, null);
        cursor.moveToFirst();
        if (cursor.getString(13) != null) {
            db.delete("images", "_id = " + cursor.getString(13), null);
        } else {
            db.delete("videos", "_id = " + cursor.getString(14), null);
        }
        cursor.close();
        cursor.moveToFirst();

        db.delete("links", "_id = " + id, null);
        db.close();
    }

    public void removeLinks() {
        db = openHelper.getWritableDatabase();
        db.delete("links", null, null);
        db.delete("images", null, null);
        db.delete("videos", null, null);
        db.close();
    }

    public void removeUserLinks(String user) {
        db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from links where user = \"" + user + "\"", null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            removeLink(cursor.getInt(0));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
    }

    public void removeUserHistory(String user, int type) {
        db = openHelper.getWritableDatabase();
        db.delete("history", "user = \"" + user + "\" and type = " + type, null);
        db.close();
    }

    public LinkedList<Links> getLinks() {
        db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from links;", null);
        LinkedList<Links> links = new LinkedList<Links>();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            links.add(getLink(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();

        return links;
    }

    public LinkedList<Links> getUserLinks(String user) {
        LinkedList<Links> links = new LinkedList<Links>();
        try {
            db = openHelper.getReadableDatabase();
            Cursor cursor = db.rawQuery("select * from links where user = \"" + user + "\"", null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                links.add(getLink(cursor));
                cursor.moveToNext();
            }
            cursor.close();
            db.close();
        } catch (Exception e) {
            Log.e(Constants.TAG, e.toString());
        }

        return links;
    }

    private Links getLink(Cursor cursor) {
        Links link;
        if (cursor.getString(13) != null) {
            link = new ImageLinks();
            Cursor imageCursor = db.rawQuery("select * from images where _id = " + cursor.getString(13), null);
            imageCursor.moveToFirst();

            ((ImageLinks) link).setImage_html(imageCursor.getString(1));
            ((ImageLinks) link).setImage_bb(imageCursor.getString(2));
            ((ImageLinks) link).setImage_bb2(imageCursor.getString(3));

            imageCursor.close();
        } else {
            link = new VideoLinks();
            Cursor videoCursor = db.rawQuery("select * from videos where _id = " + cursor.getString(14), null);
            videoCursor.moveToFirst();

            ((VideoLinks) link).setFrame_link(videoCursor.getString(1));
            ((VideoLinks) link).setFrame_html(videoCursor.getString(2));
            ((VideoLinks) link).setFrame_bb(videoCursor.getString(3));
            ((VideoLinks) link).setFrame_bb2(videoCursor.getString(4));
            ((VideoLinks) link).setVideo_embed(videoCursor.getString(5));

            videoCursor.close();
        }
        link.setId(cursor.getInt(0));
        link.setName(cursor.getString(2));
        link.setDate(new Date(cursor.getLong(3)));
        link.setSize(cursor.getString(4));
        link.setImage_link(cursor.getString(5));
        link.setThumb_link(cursor.getString(6));
        link.setThumb_html(cursor.getString(7));
        link.setThumb_bb(cursor.getString(8));
        link.setThumb_bb2(cursor.getString(9));
        link.setYfrog_link(cursor.getString(10));
        link.setYfrog_thumb(cursor.getString(11));
        link.setAd_link(cursor.getString(12));

        return link;
    }

    public Links getLink(int id) {
        db = openHelper.getWritableDatabase();

        Cursor cursor = db.rawQuery("select * from links where _id = " + id, null);
        cursor.moveToFirst();
        Links link = getLink(cursor);

        cursor.close();
        db.close();

        return link;
    }

    public int getMaxId() {
        int id;
        db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select max(_id) from links", null);
        cursor.moveToFirst();
        id = cursor.getInt(0);
        cursor.close();
        db.close();

        return id;
    }

    public long getUserLastSyncTime(String name, String type) {
        long date = 0;
        db = openHelper.getWritableDatabase();
        try {
            Cursor cursor = db.rawQuery("select * from sync where user = \"" + name + "\"", null);
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();
                if (type.equals("media.images")) {
                    date = cursor.getLong(2);
                } else {
                    date = cursor.getLong(3);
                }
            }
            cursor.close();
        } catch (SQLiteException e) {
            Log.e(Constants.TAG, e.toString());
        }
        db.close();
        return date;
    }

    public void setUserLastSyncTime(String name, String type, long date) {
        boolean userExist = isSyncUserExist(name);
        db = openHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (type.equals("media.images")) {
            values.put("image_sync", date);
        } else {
            values.put("video_sync", date);
        }
        if (userExist) {
            db.update("sync", values, "user = \"" + name + "\"", null);
        } else {
            values.put("user", name);
            db.insert("sync", null, values);
        }
        db.close();
    }

    private boolean isSyncUserExist(String name) {
        db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from sync where user = \"" + name + "\"", null);
        if (cursor.getCount() > 0) {
            return true;
        }
        db.close();
        return false;
    }

    public ArrayList<String> getUserHistory(String user, int type) {
        ArrayList<String> history = new ArrayList<String>();
        db = openHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from history where user = \"" + user + "\" and type = " + type, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            history.add(cursor.getString(2));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return history;
    }

}
