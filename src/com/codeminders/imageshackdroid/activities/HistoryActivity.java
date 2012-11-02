package com.codeminders.imageshackdroid.activities;

import android.app.*;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.text.format.DateFormat;
import com.codeminders.imageshackdroid.Constants;
import com.codeminders.imageshackdroid.R;
import com.codeminders.imageshackdroid.UploadService;
import com.codeminders.imageshackdroid.Utils;
import com.codeminders.imageshackdroid.db.DataHelper;
import com.codeminders.imageshackdroid.model.Links;

import java.util.*;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class HistoryActivity extends Activity implements OnItemClickListener, OnItemLongClickListener {
    private final static String TIME_FORMAT12 = "MM/dd/yy h:mmaa";
    private final static String TIME_FORMAT24 = "MM/dd/yy kk:mm";
    private boolean multiselect = false;
    private boolean update = true;
    private boolean longclick = false;
    private int position;
    private DataHelper dataHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.history);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.sendEmptyMessage(0);
        update = true;
        dataHelper = new DataHelper(this);
        UploadService.setLinks(dataHelper.getUserLinks(MainActivity.getUserName()));

        startUpdateThread();
    }

    public void startUpdateThread() {
        new Thread(new Runnable() {
            public void run() {
                int count = UploadService.getLinks().size();
                if (count == 0) {
                    handler.sendEmptyMessage(1);
                }
                while (update) {
                    if (count != dataHelper.getUserLinks(MainActivity.getUserName()).size()) {
                        UploadService.setLinks(dataHelper.getUserLinks(MainActivity.getUserName()));
                        if (UploadService.getLinks().size() == 0) {
                            handler.sendEmptyMessage(1);
                        }
                        handler.sendEmptyMessage(0);
                        count = UploadService.getLinks().size();
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                ((LinearLayout) findViewById(R.id.history_ll)).removeView(findViewById(777));
                updateHistory();
            }
            if (msg.what == 1) {
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.history_ll);
                if (linearLayout.getChildCount() == 0) {
                    TextView emptyHistory = new TextView(HistoryActivity.this);
                    emptyHistory.setText(getString(R.string.history_empty));
                    emptyHistory.setId(777);

                    linearLayout.addView(emptyHistory);
                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        update = false;
        synchronized (this) {
            notify();
        }
        multiselect = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.history_menu, menu);
        return true;
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        if (!multiselect) {
            update = true;
            startUpdateThread();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        update = false;
        synchronized (this) {
            notify();
        }
        if (multiselect) {
            menu.findItem(R.id.clearall_history).setVisible(false);
            menu.findItem(R.id.multiselect).setVisible(false);
            menu.findItem(R.id.clearselect_history).setVisible(true);
            menu.findItem(R.id.copy_history).setVisible(true);
            menu.findItem(R.id.share_history).setVisible(true);
        } else {
            menu.findItem(R.id.clearall_history).setVisible(true);
            menu.findItem(R.id.multiselect).setVisible(true);
            menu.findItem(R.id.clearselect_history).setVisible(false);
            menu.findItem(R.id.copy_history).setVisible(false);
            menu.findItem(R.id.share_history).setVisible(false);
            if (UploadService.getLinks().size() > 0) {
                menu.findItem(R.id.clearall_history).setEnabled(true);
                menu.findItem(R.id.multiselect).setEnabled(true);
            } else {
                menu.findItem(R.id.clearall_history).setEnabled(false);
                menu.findItem(R.id.multiselect).setEnabled(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        ListView list = (ListView) findViewById(R.id.history_list);
        int count;
        switch (item.getItemId()) {
            case R.id.switch_account:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setAction(Constants.INTENT_CHANGE_ACCOUNT);
                startActivity(intent);
                return true;
            case R.id.clearall_history:
                notificationManager.cancelAll();
                UploadService.removeLinks();
                updateHistory();

                return true;
            case R.id.multiselect:
                list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                count = list.getChildCount();
                for (int i = 0; i < count; i++) {
                    CheckBox cb = (CheckBox) ((RelativeLayout) ((LinearLayout) list.getChildAt(i)).getChildAt(0)).getChildAt(3);
                    cb.setVisibility(View.VISIBLE);
                    cb.setChecked(false);
                }
                multiselect = true;

                return true;
            case R.id.clearselect_history:
                list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
                count = list.getChildCount();
                int shift = 0;
                for (int i = 0; i < count; i++) {
                    CheckBox cb = (CheckBox) ((RelativeLayout) ((LinearLayout) list.getChildAt(i)).getChildAt(0)).getChildAt(3);
                    if (cb.isChecked()) {
                        notificationManager.cancel(UploadService.getLink(i - shift).getId());
                        UploadService.removeLink(i - shift++);
                    }
                    cb.setVisibility(View.INVISIBLE);
                }
                multiselect = false;
                updateHistory();

                return true;
            case R.id.copy_history:
                copy(buildString());
                multiselect = false;

                return true;
            case R.id.share_history:
                Utils.share(this, buildString());
                multiselect = false;

                return true;
            case R.id.sync_settings:
                Intent prefs = new Intent();
                prefs.setClassName("com.codeminders.imageshackdroid", "com.codeminders.imageshackdroid.activities.PreferencesActivity");
                startActivity(prefs);

                return true;
        }
        return true;
    }

    private String buildString() {
        ListView list = (ListView) findViewById(R.id.history_list);
        int count = list.getChildCount();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            CheckBox cb = (CheckBox) ((RelativeLayout) ((LinearLayout) list.getChildAt(i)).getChildAt(0)).getChildAt(3);
            if (cb.isChecked()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("\n");
                }
                stringBuilder.append(UploadService.getLink(i).getYfrog_link());
            }
            cb.setVisibility(View.INVISIBLE);
        }
        return stringBuilder.toString();
    }

    private void copy(String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        clipboard.setText(text);
        Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_LONG).show();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        super.onCreateDialog(id);

        switch (id) {
            case 0:
                final CharSequence[] items = {
                        getString(R.string.hd_open),
                        getString(R.string.hd_remove)
                };
                return new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.hd_title))
                        .setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                if (item == 0) {
                                    Intent intent = new Intent(HistoryActivity.this, LinksActivity.class);
                                    intent.putExtra("position", UploadService.getLink(position).getId());
                                    startActivity(intent);
                                } else {
                                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                    notificationManager.cancel(UploadService.getLink(position).getId());
                                    UploadService.removeLink(position);
                                    updateHistory();
                                }
                            }
                        }).create();

        }
        return null;
    }

    private List<Map<String, ?>> getData() {
        List<Map<String, ?>> items = new ArrayList<Map<String, ?>>();
        LinkedList<Links> links = UploadService.getLinks();

        for (Links link : links) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("name", link.getName());
            map.put("status", getString(R.string.size) + " " + link.getSize());
            if (DateFormat.is24HourFormat(this)) {
                map.put("time", DateFormat.format(TIME_FORMAT24, link.getDate()));
            } else {
                map.put("time", DateFormat.format(TIME_FORMAT12, link.getDate()));
            }

            items.add(map);
        }

        return items;
    }

    private void updateHistory() {
        SimpleAdapter adapter = new SimpleAdapter(this, getData(), R.layout.history_item,
                new String[]{"name", "status", "time"},
                new int[]{R.id.hi_name, R.id.hi_status, R.id.hi_time}
        );
        ListView list = (ListView) findViewById(R.id.history_list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
        adapter.notifyDataSetChanged();
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (!longclick) {
            Intent intent = new Intent(this, LinksActivity.class);
            intent.putExtra("position", UploadService.getLink(position).getId());
            startActivity(intent);
        } else {
            longclick = false;
        }
    }

    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
        longclick = true;

        this.position = position;
        showDialog(0);

        return false;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ((TabActivity) getParent()).getTabHost().setCurrentTab(0);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

