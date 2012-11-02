
package com.codeminders.imageshackdroid.auth;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.codeminders.imageshackdroid.Constants;

import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Authenticator class for Imageshack.us accounts
 * 
 * Based on http://bit.ly/goefWv
 * 
 * @author lord
 * 
 */
public class ImageshackAccountAuthenticator extends AbstractAccountAuthenticator
{
    private final Context a_context;
    private static final Pattern pattern = Pattern.compile(".*\"username\":\"([^\"]*)");

    public ImageshackAccountAuthenticator(Context context)
    {
        super(context);
        a_context = context;
    }

    /*
     * The user has requested to add a new account to the system. We return an
     * intent that will launch our login screen if the user has not logged in
     * yet, otherwise our activity will just pass the user's credentials on to
     * the account manager.
     */
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
            String[] requiredFeatures, Bundle options) throws NetworkErrorException
    {
        Bundle reply = new Bundle();

        Intent i = new Intent(a_context, AuthenticatorActivity.class);
        i.setAction(Constants.INTENT_LOGIN);
        i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        reply.putParcelable(AccountManager.KEY_INTENT, i);

        return reply;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options)
    {
        if(options != null && options.containsKey(AccountManager.KEY_PASSWORD))
        {
            final String password = options.getString(AccountManager.KEY_PASSWORD);
            boolean verified;
            try
            {
                verified = onlineConfirmPassword(account.name, password) != null;
            } catch(IOException e)
            {
                Log.e(Constants.TAG, "Verification error", e);
                verified = false;
            }
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, verified);
            return result;
        }
        // Launch AuthenticatorActivity to confirm credentials
        final Intent intent = new Intent(a_context, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_CONFIRM_CREDENTIALS, true);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType)
    {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType,
            Bundle options) throws NetworkErrorException
    {
        return null;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType)
    {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
            throws NetworkErrorException
    {
        final Bundle result = new Bundle();
        result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
            Bundle options)
    {
        final Intent intent = new Intent(a_context, AuthenticatorActivity.class);
        intent.putExtra(AuthenticatorActivity.PARAM_USERNAME, account.name);
        intent.putExtra(AuthenticatorActivity.PARAM_CONFIRM_CREDENTIALS, false);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    public static String onlineConfirmPassword(String username, String password) throws IOException
    {
        Log.d(Constants.TAG, "Checking credentials for '" + username + "'");

        Uri.Builder ub = new Uri.Builder();
        ub.scheme(Constants.AUTH_ENDPOINT_SCHEME);
        ub.authority(Constants.AUTH_ENDPOINT_AUTHORITY);
        ub.appendPath(Constants.AUTH_ENDPOINT_PATH);
        ub.appendQueryParameter(Constants.AUTH_USERNAME_FIELD, username);
        ub.appendQueryParameter(Constants.AUTH_PASSWORD_FIELD, password);
        ub.appendQueryParameter("format", "json");
        ub.appendQueryParameter(Constants.AUTH_NOCOOKIE_KEY, Constants.AUTH_NOCOOKIE_VALUE);
        String auth_url = ub.build().toString();

        HttpClient client = new DefaultHttpClient();
        Log.d(Constants.TAG, "Credentials check URL");
        HttpGet get = new HttpGet(auth_url);
        HttpResponse res = client.execute(get);
        if(res.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
        {
            Log.d(Constants.TAG, "Bad check status: " + res.getStatusLine());
            throw new IOException("Bad check HTTP status");
        }

        HttpEntity ent = res.getEntity();
        if(ent != null)
        {
            String rsp = EntityUtils.toString(ent);
            Log.d(Constants.TAG, "Auth response body: '" + rsp + "'");
            Matcher matcher = pattern.matcher(rsp);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return null;
    }
}