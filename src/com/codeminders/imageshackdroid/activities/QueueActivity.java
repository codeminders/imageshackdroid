package com.codeminders.imageshackdroid.activities;

import java.util.*;

import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;

import com.codeminders.imageshackdroid.*;
import com.codeminders.imageshackdroid.model.CountingMultipartEntity;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class QueueActivity extends Activity implements OnItemLongClickListener {
    private static final int REMOVE = 1;
    private static final int TRASFER_ERR = 2;
    private boolean update = true;
    private int position;
    private boolean connection = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.queue);
    }

    @Override
    public void onResume() {
        super.onResume();

        handler.sendEmptyMessage(0);
        update = true;

        startUpdateThread();
    }

    @Override
    public void onPause() {
        super.onPause();

        update = false;
        synchronized (this) {
            notify();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.switch_account:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setAction(Constants.INTENT_CHANGE_ACCOUNT);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            case R.id.removeall:
                UploadService.removeAll();
                update();

                return true;
            case R.id.pauseall:
                UploadService.pauseAll(MainActivity.getUserName());
                update();

                return true;
            case R.id.resumeall:
                connection = true;
                UploadService.resumeAll();
                update();
                startService(new Intent(this, UploadService.class));

                return true;
            case R.id.sync_settings:
                Intent prefs = new Intent();
                prefs.setClassName("com.codeminders.imageshackdroid", "com.codeminders.imageshackdroid.activities.PreferencesActivity");
                startActivity(prefs);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void createQueue() {
        TaskAdapter adapter = new TaskAdapter(this);
        adapter.setData(getData());

        ListView list = (ListView) findViewById(R.id.queue_list);
        list.setAdapter(adapter);
        list.setOnItemLongClickListener(this);
    }

    public List<Map<String, ?>> getData() {
        List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
        LinkedList<CountingMultipartEntity> entities = UploadService.getEntitys();

        for (CountingMultipartEntity entity : entities) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", entity.getName());
            if (entity.getId() == UploadService.getCurrentEntity().getId()
                    && MainActivity.getUserName().equals(UploadService.getCurrentUser())) {
                if (!entity.getStatus().equals(Constants.STATUS_PAUSE)) {
                    entity.setStatus(Constants.STATUS_UPLOAD);
                }
            }
            map.put("status", entity.getStatus());
            items.add(map);
        }

        return items;
    }

    public void startUpdateThread() {
        new Thread(new Runnable() {
            public void run() {
                ProgressBar progressBar;
                int count = UploadService.getEntitys().size();
                if (UploadService.getEntitys().size() == 0) {
                    handler.sendEmptyMessage(1);
                }
                String status = Constants.STATUS_WAIT;
                while (update) {
                    progressBar = (ProgressBar) findViewById(9999);
                    if (progressBar != null && UploadService.isCurrent()) {
                        progressBar.setMax((int) UploadService.getCurrentEntity().getContentLength());
                        progressBar.setProgress((int) UploadService.getCurrentEntity().getTransferred());
                    }
                    if (UploadService.getEntitys().size() > 0) {
                        if (!status.equals(UploadService.getCurrentEntity().getStatus())) {
                            update();
                            status = UploadService.getCurrentEntity().getStatus();
                        }
                    }
                    if (count != UploadService.getEntitys().size()) {
                        update();
                        count = UploadService.getEntitys().size();
                    }
                    if (connection != UploadService.isConnection()) {
                        update();
                        connection = UploadService.isConnection();
                        if (!connection && UploadService.getEntitys().size() > 0) {
                            handler.sendEmptyMessage(2);
                        }
                    }
                    try {
                        synchronized (this) {
                            wait(1000);
                        }
                    } catch (InterruptedException e) {
                        Log.d(Constants.TAG, e.toString());
                        update = false;
                    }
                }
            }
        }).start();
    }

    private void update() {
        if (UploadService.getEntitys().size() == 0) {
            handler.sendEmptyMessage(1);
        } else {
            handler.sendEmptyMessage(0);
        }
    }

    public Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                ((LinearLayout) findViewById(R.id.queue_ll)).removeView(findViewById(1777));
                createQueue();
            }
            if (msg.what == 1) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.queue_ll);
                if (linearLayout.getChildCount() == 0) {
                    TextView emptyHistory = new TextView(QueueActivity.this);
                    emptyHistory.setText(getString(R.string.queue_empty));
                    emptyHistory.setId(1777);

                    linearLayout.addView(emptyHistory);
                    createQueue();
                }
            }
            if (msg.what == 2) {
                showDialog(TRASFER_ERR);
            }
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);

        switch (id) {
            case REMOVE:
                final CharSequence[] items = {
                        getString(R.string.hd_remove)
                };
                return new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.hd_title))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                UploadService.remove(position);
                                update();
                            }
                        }).create();
            case TRASFER_ERR:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.network_err))
                        .setCancelable(true)
                        .setPositiveButton(getString(R.string.login_btn_pos), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                return builder.create();
        }
        return null;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (UploadService.getEntitys().size() > 0) {
            CountingMultipartEntity entity = UploadService.getCurrentEntity();
            String status = null;
            if (entity != null) {
                status = entity.getStatus();
            }
            if (status == null || status.equals(Constants.STATUS_PAUSE)) {
                menu.findItem(R.id.pauseall).setEnabled(false);
            } else {
                menu.findItem(R.id.pauseall).setEnabled(true);
            }
            if (status == null || status.equals(Constants.STATUS_UPLOAD)) {
                menu.findItem(R.id.resumeall).setEnabled(false);
            } else {
                menu.findItem(R.id.resumeall).setEnabled(true);
            }
            menu.findItem(R.id.removeall).setEnabled(true);
        } else {
            menu.findItem(R.id.pauseall).setEnabled(false);
            menu.findItem(R.id.resumeall).setEnabled(false);
            menu.findItem(R.id.removeall).setEnabled(false);
        }

        return true;
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        this.position = position;
        showDialog(REMOVE);

        return false;
    }

    public class TaskAdapter extends BaseAdapter {
        List<Map<String, ?>> data = new ArrayList<Map<String, ?>>();
        private Context context;

        public TaskAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            return data.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);

            ((TextView) convertView.findViewById(R.id.mi_name)).setText(data.get(position).get("name").toString());
            String status = data.get(position).get("status").toString();
            if (status.equals(Constants.STATUS_UPLOAD)) {
                ProgressBar progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
                progressBar.setPadding(0, 0, 10, 10);
                progressBar.setId(9999);
                if (UploadService.isCurrent()) {
                    progressBar.setMax((int) UploadService.getCurrentEntity().getContentLength());
                    progressBar.setProgress((int) UploadService.getCurrentEntity().getTransferred());
                }
                ((LinearLayout) ((LinearLayout) convertView).getChildAt(0)).removeViewAt(1);
                ((LinearLayout) ((LinearLayout) convertView).getChildAt(0)).addView(progressBar);
            } else if (status.equals(Constants.STATUS_PAUSE)) {
                ((TextView) convertView.findViewById(R.id.mi_status)).setText(getString(R.string.pause));
            } else if (status.equals(Constants.STATUS_WAIT)) {
                TextView textView = (TextView) convertView.findViewById(R.id.mi_status);
                if (textView == null) {
                    textView = new TextView(context);
                    textView.setId(R.id.mi_status);
                    textView.setPadding(0, 0, 0, 10);
                    ((LinearLayout) ((LinearLayout) convertView).getChildAt(0)).removeViewAt(1);
                    ((LinearLayout) ((LinearLayout) convertView).getChildAt(0)).addView(textView);
                }
                ((TextView) convertView.findViewById(R.id.mi_status)).setText(getString(R.string.wait));
            }

            return convertView;

        }

        public void setData(List<Map<String, ?>> data) {
            this.data = data;
            notifyDataSetChanged();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent("android.intent.action.MAIN");
            i.addCategory(Intent.CATEGORY_HOME);
            startActivity(i);
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
