package com.codeminders.imageshackdroid.activities;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Window;
import android.widget.*;

import com.codeminders.imageshackdroid.*;
import com.codeminders.imageshackdroid.auth.AuthenticatorActivity;
import com.codeminders.imageshackdroid.db.DataHelper;
import com.codeminders.imageshackdroid.model.CountingMultipartEntity;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class MainActivity extends TabActivity {
    private static final int UPLOAD_ERR = 1;
    private static final int UNKNOWN_SCHEMA = 2;
    private static final int NOTHING_TO_SEND = 3;
    private static final int AUTH_RESULT = 0;
    private static final int SWITCH_ACCOUNT = 4;
    private static final int SYNC_IN_PROGRESS = 5;
    private static final int MEDIA_SHARED = 6;
    private String username = null;
    private String password;
    private static Account account;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(Constants.TAG, "Starting Main activity");
        super.onCreate(savedInstanceState);

        checkSharedPreferences();
        createUI();
        DataHelper dataHelper = new DataHelper(this);
        CountingMultipartEntity.setMaxId(dataHelper.getMaxId());
        if (account != null) {
            UploadService.setLinks(dataHelper.getUserLinks(account.name));
        }
        startService(new Intent(this, UploadService.class));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_SHARED)) {
            showDialog(MEDIA_SHARED);
        }
        Log.d(Constants.TAG, "Getting account manager");
        AccountManager am = AccountManager.get(this);

        Log.d(Constants.TAG, "Getting accounts with type " + getString(R.string.ACCOUNT_TYPE));
        Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

        if (accounts == null || accounts.length == 0) {
            username = null;
            updateTitle();
            Log.d(Constants.TAG, "No accounts found with type " + getString(R.string.ACCOUNT_TYPE));
            Intent intent = new Intent(this, AuthenticatorActivity.class);
            startActivityForResult(intent, AUTH_RESULT);
        } else {
            Log.d(Constants.TAG, "" + accounts.length + " accounts found with type " + getString(R.string.ACCOUNT_TYPE));
            if ((accounts.length > 1 && account == null)
                    || Constants.INTENT_CHANGE_ACCOUNT.equals(getIntent().getAction())) {
                Log.d(Constants.TAG, "Found " + accounts.length + " accounts");
                showDialog(SWITCH_ACCOUNT);
            } else {
                if (account == null) {
                    account = accounts[0];
                    DataHelper dataHelper = new DataHelper(this);
                    UploadService.setLinks(dataHelper.getUserLinks(account.name));
                }
                username = account.name;
                Log.d(Constants.TAG, "Found account for user " + username);
                password = am.getPassword(account);
                updateTitle();
                if (Intent.ACTION_SEND.equals(getIntent().getAction())
                        || Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                    doSend();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(Constants.TAG, "onActivityResult(" + requestCode + "," + resultCode + ")");
        if (requestCode == AUTH_RESULT) {
            if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras();
                username = extras.getString(AccountManager.KEY_ACCOUNT_NAME);
                password = extras.getString(AccountManager.KEY_AUTHTOKEN);

                AccountManager am = AccountManager.get(this);
                Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));

                for (Account account : accounts) {
                    if (username.equals(account.name)) {
                        MainActivity.account = account;
                        DataHelper dataHelper = new DataHelper(this);
                        UploadService.setLinks(dataHelper.getUserLinks(account.name));
                        break;
                    }
                }

                updateTitle();
                if (Intent.ACTION_SEND.equals(getIntent().getAction())
                        || Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                    doSend();
                }
                if (Constants.INTENT_CHANGE_ACCOUNT.equals(getIntent().getAction())) {
                    getIntent().setAction("");
                }
            } else {
                checkSharedPreferences();
                if (account == null) {
                    finish();
                } else {
                    getIntent().setAction("");
                    onResume();
                }
            }
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void doSend() {
        final Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras.containsKey(Intent.EXTRA_STREAM)) {
            ArrayList<Uri> uris = new ArrayList<Uri>();
            if (Intent.ACTION_SEND.equals(getIntent().getAction())) {
                uris.add((Uri) extras.getParcelable(Intent.EXTRA_STREAM));
            } else if (Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                uris = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
            }
            for (Uri uri : uris) {
                String scheme = uri.getScheme();
                if (scheme.equals("content")) {
                    try {
                        ContentResolver contentResolver = getContentResolver();
                        Cursor cursor = contentResolver.query(uri, null, null, null, null);
                        cursor.moveToFirst();
                        String mimeType = contentResolver.getType(uri);
                        String filePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                        UploadService.addTask(this, filePath, mimeType, username, password, false);
                    } catch (Exception e) {
                        Log.e(Constants.TAG, getString(R.string.upload_err), e);
                        showDialog(UPLOAD_ERR);
                    }
                } else {
                    Log.e(Constants.TAG, getString(R.string.unknown_schema) + "'" + scheme + "'");
                    showDialog(UNKNOWN_SCHEMA);
                }
            }
            startService(new Intent(this, UploadService.class));
        } else {
        }
        getIntent().setAction("");
    }

    private void createUI() {
        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        setContentView(R.layout.tabs);

        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
        updateTitle();

        Resources res = getResources();
        TabHost tabHost = getTabHost();
        TabHost.TabSpec spec;

        Intent intent = new Intent().setClass(this, QueueActivity.class);

        spec = tabHost.newTabSpec("queue")
                .setIndicator(getString(R.string.main), res.getDrawable(android.R.drawable.sym_action_chat))
                .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, HistoryActivity.class);
        spec = tabHost.newTabSpec("history")
                .setIndicator(getString(R.string.history), res.getDrawable(android.R.drawable.sym_action_email))
                .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(2);
    }

    private void updateTitle() {
        TextView titleText = (TextView) findViewById(R.id.title);
        if (username != null)
            titleText.setText(getString(R.string.loggedin) + " " + username);
        else
            titleText.setText(getString(R.string.notloggedin));
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == UPLOAD_ERR) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.network_err_login)).setCancelable(true)
                    .setPositiveButton(getString(R.string.upload_err), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            finish();
                        }
                    });
            return builder.create();
        } else if (id == UNKNOWN_SCHEMA) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.unknown_schema)).setCancelable(true)
                    .setPositiveButton(getString(R.string.login_btn_pos), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        } else if (id == NOTHING_TO_SEND) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.nothing_to_send)).setCancelable(true)
                    .setPositiveButton(getString(R.string.login_btn_pos), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        } else if (id == SYNC_IN_PROGRESS) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.sync_in_progreess)).setCancelable(true)
                    .setPositiveButton(getString(R.string.login_btn_pos), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        } else if (id == MEDIA_SHARED) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.media_ejected)).setCancelable(true)
                    .setPositiveButton(getString(R.string.login_btn_pos), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            return builder.create();
        } else if (id == SWITCH_ACCOUNT) {
            UploadService.removeNotifications();
            account = null;
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
            CharSequence[] items = new CharSequence[accounts.length + 1];
            items[0] = getString(R.string.add_account);
            for (int i = 1; i < items.length; i++) {
                items[i] = accounts[i - 1].name;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.switch_account));
            builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface dialogInterface) {
                    getIntent().setAction("");
                    checkSharedPreferences();
                    onResume();
                }
            });
            builder.setItems(items, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    if (item == 0) {
                        Intent intent = new Intent(MainActivity.this, AuthenticatorActivity.class);
                        startActivityForResult(intent, AUTH_RESULT);
                        editor.remove(Constants.CURR_USER_PREF_KEY);
                    } else {
                        AccountManager am = AccountManager.get(MainActivity.this);
                        account = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE))[item - 1];
                        username = account.name;
                        Log.d(Constants.TAG, "Choosed user " + username);
                        DataHelper dataHelper = new DataHelper(MainActivity.this);
                        UploadService.setLinks(dataHelper.getUserLinks(account.name));
                        password = am.getPassword(account);
                        editor.putString(Constants.CURR_USER_PREF_KEY, username);
                        editor.commit();
                        Log.d(Constants.TAG, "Saved current user " + username + " in shared preferences");
                        updateTitle();
                        if (Intent.ACTION_SEND.equals(getIntent().getAction())
                                || Intent.ACTION_SEND_MULTIPLE.equals(getIntent().getAction())) {
                            doSend();
                        }
                    }
                    if (Constants.INTENT_CHANGE_ACCOUNT.equals(getIntent().getAction())) {
                        getIntent().setAction("");
                    }
                }
            });
            return builder.create();
        } else {
            return null;
        }
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == SWITCH_ACCOUNT) {
            AlertDialog alert = (AlertDialog) dialog;
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(MainActivity.this, android.R.layout.select_dialog_item);
            AccountManager am = AccountManager.get(this);
            Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
            adapter.add(getString(R.string.add_account));
            for (int i = 1; i < accounts.length + 1; i++) {
                adapter.add(accounts[i - 1].name);
            }
            alert.getListView().setAdapter(adapter);
        }
    }

    private void checkSharedPreferences() {
        AccountManager am = AccountManager.get(this);
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE);
        username = prefs.getString(Constants.USER_PREF_KEY, null);
        if (username != null) {
            Log.d(Constants.TAG, "Found user " + username + " in shared preferences");
            password = prefs.getString(Constants.PASS_PREF_KEY, null);
            Account account = new Account(username, getString(R.string.ACCOUNT_TYPE));
            am.addAccountExplicitly(account, password, null);
            ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
            Log.d(Constants.TAG, "Add new account for user " + username);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(Constants.USER_PREF_KEY);
            editor.remove(Constants.PASS_PREF_KEY);
            editor.commit();
            Log.d(Constants.TAG, "Removed user " + username + " from shared preferences");
        }
        String currentUser = prefs.getString(Constants.CURR_USER_PREF_KEY, null);
        if (currentUser != null) {
            Log.d(Constants.TAG, "Found current user " + currentUser + " in shared preferences");
            Account[] accounts = am.getAccountsByType(getString(R.string.ACCOUNT_TYPE));
            for (Account account : accounts) {
                if (currentUser.equals(account.name)) {
                    Log.d(Constants.TAG, "Found current user " + currentUser + " in accounts");
                    MainActivity.account = account;
                    break;
                }
            }
            if (account == null) {
                Log.d(Constants.TAG, "Current user " + currentUser + " doesn't exist in accounts");
                SharedPreferences.Editor editor = prefs.edit();
                editor.remove(Constants.CURR_USER_PREF_KEY);
                editor.commit();
                Log.d(Constants.TAG, "Removed current user " + currentUser + " from shared preferences");
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public static String getUserName() {
        if (account != null) {
            return account.name;
        }
        return "";
    }

}
