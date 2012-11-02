package com.codeminders.imageshackdroid.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.*;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;
import com.codeminders.imageshackdroid.Constants;
import com.codeminders.imageshackdroid.UploadService;
import com.codeminders.imageshackdroid.db.DataHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private final AccountManager accountManager;
    private final Context context;
    private final int[] times = new int[]{
            300, 900, 1800, 3600, 7200, 14400, 28800, 43200, 86440
    };

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        this.context = context;
        accountManager = AccountManager.get(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Cursor cursor;
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        int itemPosition = prefs.getInt(Constants.SYNC_INTERVAL, 0);
        ContentResolver.addPeriodicSync(account, authority, new Bundle(), times[itemPosition]);
        if (prefs.getBoolean(Constants.SYNC_TYPE, false)) {
            NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (info == null || info.getType() != 1) {
                return;
            }
        }
        try {
            String[] columns = new String[]{
                    MediaColumns.DATE_ADDED,
                    MediaColumns.DATA,
                    MediaColumns.MIME_TYPE,
                    MediaColumns.DISPLAY_NAME,
                    MediaColumns.SIZE
            };
            DataHelper dataHelper = new DataHelper(context);
            long lastSync = dataHelper.getUserLastSyncTime(account.name, authority);
            if (lastSync == 0) {
                dataHelper.setUserLastSyncTime(account.name, authority, new Date().getTime());
                return;
            }
            if (authority.equals("media.images")) {
                cursor = provider.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
            } else {
                cursor = provider.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, columns, null, null, null);
            }
            if (cursor == null) {
                return;
            }
            ArrayList<String> history = dataHelper.getUserHistory(account.name, authority.equals("media.images") ? 0 : 1);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                if ((cursor.getLong(0) * 1000 > lastSync) && !history.contains(cursor.getString(1))) {
                    UploadService.addTask(context, cursor.getString(1), cursor.getString(2), account.name, accountManager.getPassword(account), true);
                }
                cursor.moveToNext();
            }
            cursor.close();
            dataHelper.setUserLastSyncTime(account.name, authority, new Date().getTime());
            UploadService.resumeAll();
            context.startService(new Intent(context, UploadService.class));
            dataHelper.removeUserHistory(account.name, authority.equals("media.images") ? 0 : 1);
        } catch (RemoteException e) {
            Log.e(Constants.TAG, e.toString());
        } catch (IOException e) {
            Log.e(Constants.TAG, e.toString());
        }
    }

}


