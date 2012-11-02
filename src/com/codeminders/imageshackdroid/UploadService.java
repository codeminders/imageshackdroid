package com.codeminders.imageshackdroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import com.codeminders.imageshackdroid.activities.LinksActivity;
import com.codeminders.imageshackdroid.activities.MainActivity;
import com.codeminders.imageshackdroid.db.DataHelper;
import com.codeminders.imageshackdroid.model.CountingMultipartEntity;
import com.codeminders.imageshackdroid.model.Links;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class UploadService extends Service {
    private static Map<String, LinkedList<CountingMultipartEntity>> userEntitys = new HashMap<String, LinkedList<CountingMultipartEntity>>();
    private static LinkedList<CountingMultipartEntity> entitys;
    private static LinkedList<Links> links = new LinkedList<Links>();
    private static boolean running = false;
    private static boolean updating = false;
    private Notification notification;
    private PendingIntent contentIntent;
    private static Context context;
    private static NotificationManager notificationManager;
    private static HttpPost post;
    private static int current;
    private static boolean remove = true, removeUser = true;
    private static boolean connection = true;
    private static DataHelper dataHelper;
    private static CountingMultipartEntity curEntity;
    private static String user;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        context = this;
        dataHelper = new DataHelper(this);
    }

    @Override
    public void onDestroy() {
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    @Override
    public void onLowMemory() {
        onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        if (!running) {
            running = true;
            new UploadPictureTask().execute();
        }
    }

    private class UploadPictureTask extends AsyncTask<Void, String, Object> {
        @Override
        protected synchronized Object doInBackground(Void... params) {
            HttpClient client = new DefaultHttpClient();
            ArrayList<String> users = new ArrayList<String>(userEntitys.keySet());
            for (int currentUser = 0; currentUser < users.size(); currentUser++) {
                user = users.get(currentUser);
                entitys = userEntitys.get(user);
                removeUser = true;
                if (entitys.size() > 0) {
                    if (entitys.get(0).getStatus().equals(Constants.STATUS_PAUSE)) {
                        removeUser = false;
                    }
                }
                for (current = 0; current < entitys.size(); current++) {
                    curEntity = entitys.get(current);
                    remove = true;
                    if (!curEntity.getStatus().equals(Constants.STATUS_PAUSE)) {
                        if (curEntity.getType() == Constants.TYPE_IMAGE) {
                            post = new HttpPost(Constants.IMAGE_UPLOAD_ENDPOINT);
                        } else {
                            post = new HttpPost(Constants.VIDEO_UPLOAD_ENDPOINT);
                        }
                        post.setEntity(curEntity);

                        if (user.equals(MainActivity.getUserName())) {
                            updating = true;
                            new Thread(new Runnable() {
                                public void run() {
                                    while (updating && isCurrent()) {
                                        Notification notification = new Notification(R.drawable.icon, getString(R.string.upload), System.currentTimeMillis());
                                        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.upload_progress);
                                        contentView.setImageViewResource(R.id.status_icon, R.drawable.icon);
                                        contentView.setTextViewText(R.id.status_text, entitys.get(current).getName());
                                        Intent notificationIntent = new Intent(context, MainActivity.class);
                                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                        notification.contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
                                        notification.contentView = contentView;

                                        notification.contentView.setProgressBar(R.id.status_progress,
                                                (int) entitys.get(current).getContentLength(),
                                                (int) entitys.get(current).getTransferred(), false);
                                        try {
                                            notificationManager.notify(entitys.get(current).getId(), notification);
                                        } catch (IllegalArgumentException e) {
                                            Log.e(Constants.TAG, e.toString());
                                        }

                                        try {
                                            synchronized (this) {
                                                wait(1000);
                                            }
                                        } catch (InterruptedException e) {
                                            Log.e(Constants.TAG, e.toString());
                                            updating = false;
                                        }
                                    }
                                }
                            }).start();
                        }

                        try {
                            HttpResponse res = client.execute(post);
                            if (res.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                                throw new IOException("Bad check HTTP status");
                            }
                            updating = false;
                            synchronized (this) {
                                notify();
                            }
                            HttpEntity ent = res.getEntity();

                            if (ent != null) {
                                Links link = new LinkParser(curEntity.getType()).parse(ent.getContent());
                                link.setDate(new Date());
                                link.setName(curEntity.getName());
                                link.setSize(curEntity.getSize());
                                link.setId(curEntity.getId());
                                dataHelper.write(link, user);
                                links.add(link);

                                Intent notificationIntent = new Intent(context, LinksActivity.class);
                                notificationIntent.putExtra("position", curEntity.getId());
                                contentIntent = PendingIntent.getActivity(context, curEntity.getId(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                notification = new Notification(R.drawable.icon, getString(R.string.upload), System.currentTimeMillis());
                                notification.setLatestEventInfo(getApplicationContext(), curEntity.getName(), getString(R.string.finished), contentIntent);
                                notification.flags = Notification.FLAG_AUTO_CANCEL;
                                notificationManager.notify(curEntity.getId(), notification);
                            }
                        } catch (IOException e) {
                            Log.e(UploadService.class.getName(), e.toString());
                            updating = false;
                            synchronized (this) {
                                notify();
                            }
                            notificationManager.cancel(curEntity.getId());
                            if (!remove) {
                                notificationManager.cancelAll();
                            }

                            if (!post.isAborted()) {
                                pauseAll(null);
                                connection = false;
                            }
                        }
                        if (remove) {
                            entitys.remove(current);
                            curEntity = null;
                            current = -1;
                        }
                    }
                }
                if (removeUser) {
                    userEntitys.remove(user);
                    users = new ArrayList<String>(userEntitys.keySet());
                    currentUser = -1;
                }
                user = null;
            }
            current = 0;
            running = false;
            return null;
        }

        @Override
        protected void onPreExecute() {
            Intent notificationIntent = new Intent(context, MainActivity.class);

            contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notification = new Notification(android.R.drawable.stat_sys_upload, getString(R.string.upload), System.currentTimeMillis());
            notification.contentIntent = contentIntent;
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.upload_progress);
            contentView.setImageViewResource(R.id.status_icon, R.drawable.icon);
        }
    }

    public static void addTask(String filePath, String mimeType, String username, String password, boolean sync) throws IOException {
        int type;
        if (mimeType.startsWith("image/")) {
            type = Constants.TYPE_IMAGE;
        } else {
            type = Constants.TYPE_VIDEO;
        }
        SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        CountingMultipartEntity multipart = new CountingMultipartEntity(new File(filePath).getName(), type);
        multipart.setSize(Utils.getFileSize(new File(filePath).length()));

        multipart.addPart(Constants.FIELD_KEY, new StringBody(Constants.DEV_KEY));
        String tags = prefs.getString(Constants.FIELD_TAGS, null);
        if (tags != null) {
            multipart.addPart(Constants.FIELD_TAGS, new StringBody(tags));
        } else {
            multipart.addPart(Constants.FIELD_TAGS, new StringBody(Constants.DEFAULT_TAGS));
        }
        String visibility = prefs.getString(Constants.FIELD_PUBLIC, null);
        if (visibility != null) {
            multipart.addPart(Constants.FIELD_PUBLIC, new StringBody(visibility));
        }
        multipart.addPart(Constants.FIELD_USERNAME, new StringBody(username));
        multipart.addPart(Constants.FIELD_PASSWORD, new StringBody(password));
        multipart.addPart(Constants.FIELD_MEDIA, new FileBody(new File(filePath), mimeType));

        if (!connection) {
            multipart.setStatus(Constants.STATUS_PAUSE);
        }

        LinkedList<CountingMultipartEntity> listEntitys = userEntitys.get(username);
        if (listEntitys == null) {
            listEntitys = new LinkedList<CountingMultipartEntity>();
            listEntitys.add(multipart);
            userEntitys.put(username, listEntitys);
        } else {
            listEntitys.add(multipart);
        }
        if (ContentResolver.getMasterSyncAutomatically() && !sync) {
            DataHelper dataHelper = new DataHelper(context);
            dataHelper.writeHistory(username, filePath, type == Constants.TYPE_IMAGE ? 0 : 1);
        }
    }

    public static void addTask(Context context, String filePath, String mimeType, String username, String password, boolean sync) throws IOException {
        if (UploadService.context == null) {
            UploadService.context = context;
        }
        addTask(filePath, mimeType, username, password, sync);
    }

    public static void abortTask() {
        if (curEntity != null && post != null) {
            post.abort();
        }
    }

    public static void remove(int position) {
        LinkedList<CountingMultipartEntity> entitys = userEntitys.get(MainActivity.getUserName());
        if (position != current || entitys.get(position) != null && entitys.get(position).getStatus().equals(Constants.STATUS_PAUSE)) {
            notificationManager.cancel(entitys.get(position).getId());
            entitys.remove(position);
        } else {
            abortTask();
        }
    }

    public static void removeAll() {
        removeAll(MainActivity.getUserName());
    }

    public static void removeAll(String name) {
        pauseAll(name);
        userEntitys.remove(name);
        notificationManager.cancelAll();
    }

    public static void pauseAll(String username) {
        remove = false;
        removeUser = false;
        ArrayList<String> users;
        if (username == null) {
            users = new ArrayList<String>(userEntitys.keySet());
        } else {
            users = new ArrayList<String>();
            users.add(username);
        }
        for (String user : users) {
            LinkedList<CountingMultipartEntity> entitys = userEntitys.get(user);
            for (CountingMultipartEntity entity : entitys) {
                entity.setStatus(Constants.STATUS_PAUSE);
            }
        }

        abortTask();
        if (curEntity != null) {
            curEntity.setTransferred(0);
        }
    }

    public static void resumeAll() {
        connection = true;
        LinkedList<CountingMultipartEntity> entitys = userEntitys.get(MainActivity.getUserName());
        if (entitys == null) {
            return;
        }
        for (CountingMultipartEntity entity : entitys) {
            if (entity.getStatus().equals(Constants.STATUS_PAUSE)) {
                entity.setStatus(Constants.STATUS_WAIT);
            }
        }
    }

    public static void removeNotifications() {
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    public static CountingMultipartEntity getLastEntity() {
        LinkedList<CountingMultipartEntity> entitys = userEntitys.get(MainActivity.getUserName());
        return entitys.getLast();
    }

    public static ArrayList<String> getUsers() {
        if (userEntitys != null) {
            return new ArrayList<String>(userEntitys.keySet());
        }
        return null;
    }

    public static LinkedList<CountingMultipartEntity> getEntitys() {
        LinkedList<CountingMultipartEntity> entitys = userEntitys.get(MainActivity.getUserName());
        if (entitys == null) {
            entitys = new LinkedList<CountingMultipartEntity>();
        }
        return entitys;
    }

    public static CountingMultipartEntity getCurrentEntity() {
        if (isCurrent()) {
            return userEntitys.get(MainActivity.getUserName()).get(current);
        }
        return null;
    }

    public static boolean isCurrent() {
        LinkedList<CountingMultipartEntity> entitys = userEntitys.get(MainActivity.getUserName());
        return entitys != null && entitys.size() > current;
    }

    public static LinkedList<Links> getLinks() {
        return links;
    }

    public static void setLinks(LinkedList<Links> oldLinks) {
        links = oldLinks;
    }

    public static Links getLink(int num) {
        return links.get(num);
    }

    public static void removeLinks() {
        links.clear();
        dataHelper.removeUserLinks(MainActivity.getUserName());
    }

    public static void removeLink(int num) {
        dataHelper.removeLink(links.get(num).getId());
        links.remove(num);
    }

    public static boolean isConnection() {
        return connection;
    }

    public static String getCurrentUser() {
        if (user != null) {
            return user;
        }
        return "";
    }

}
