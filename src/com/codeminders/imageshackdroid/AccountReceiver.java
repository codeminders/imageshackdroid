package com.codeminders.imageshackdroid;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class AccountReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        AccountManager am = AccountManager.get(context);
        Account[] accounts = am.getAccountsByType(context.getString(R.string.ACCOUNT_TYPE));
        ArrayList<String> users = UploadService.getUsers();
        if (users != null && users.size() > 0) {
            Log.d(Constants.TAG, "Found users with tasks " + users.size());
            if (accounts == null || accounts.length == 0) {
                Log.d(Constants.TAG, "No accounts found");
                for (String user : users) {
                    Log.d(Constants.TAG, "Remove entities for deleted account " + user);
                    UploadService.removeAll(user);
                }
            } else {
                for (String user : users) {
                    if (!isInAccounts(accounts, user)) {
                        Log.d(Constants.TAG, "Remove entities for deleted account " + user);
                        UploadService.removeAll(user);
                    }
                }
            }
        }
    }

    private boolean isInAccounts(Account[] accounts, String name) {
        for (Account account : accounts) {
            if (name.equals(account.name)) {
                return true;
            }
        }
        return false;
    }

}
