
package com.codeminders.imageshackdroid.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Authenticator service that returns a subclass of AbstractAccountAuthenticator
 * in onBind()
 * 
 * Based on:
 * http://www.c99.org/2010/01/23/writing-an-android-sync-provider-part-1/
 */
public class ImageshackAuthenticatorService extends Service
{
    @SuppressWarnings("unused")
    private static final String                   TAG                   = "ImageshackAuthenticatorService";

    private static ImageshackAccountAuthenticator sAccountAuthenticator = null;

    public ImageshackAuthenticatorService()
    {
        super();
    }

    public IBinder onBind(Intent intent)
    {
        IBinder ret = null;
        if(intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT))
            ret = getAuthenticator().getIBinder();
        return ret;
    }

    private ImageshackAccountAuthenticator getAuthenticator()
    {
        if(sAccountAuthenticator == null)
            sAccountAuthenticator = new ImageshackAccountAuthenticator(this);
        return sAccountAuthenticator;
    }
}