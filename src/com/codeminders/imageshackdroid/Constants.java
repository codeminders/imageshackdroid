
package com.codeminders.imageshackdroid;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class Constants
{
    public static final String TAG                     = "ImageshackDroid";

    public static final String DEV_KEY                 = "ACHPRTXY1f12898ca175997a85ad83f578157c61";

    public static final String AUTH_NOCOOKIE_VALUE     = "1";
    public static final String AUTH_NOCOOKIE_KEY       = "nocookie";
    public static final String AUTH_PASSWORD_FIELD     = "password";
    public static final String AUTH_USERNAME_FIELD     = "username";
    public static final String AUTH_ENDPOINT_SCHEME    = "http";
    public static final String AUTH_ENDPOINT_PATH      = "auth.php";
    public static final String AUTH_ENDPOINT_AUTHORITY = "www.imageshack.us";

    public static final String IMAGE_UPLOAD_ENDPOINT   = "http://www.imageshack.us/upload_api.php";
    public static final String VIDEO_UPLOAD_ENDPOINT   = "http://render.imageshack.us/upload_api.php";
    public static final String DEFAULT_TAGS            = "ImageshackDroid";

    public static final String FIELD_KEY               = "key";
    public static final String FIELD_MEDIA             = "fileupload";
    public static final String FIELD_TAGS              = "tags";
    public static final String FIELD_USERNAME          = "a_username";
    public static final String FIELD_PASSWORD          = "a_password";
    public static final String FIELD_PUBLIC            = "public";

    public static final String NEXT_EXTRAS_EXTRAS_KEY  = "com.codeminders.imageshackdroid.extras.NEXT_EXTRA_KEY";
    public static final String NEXT_ACTION_EXTRAS_KEY  = "com.codeminders.imageshackdroid.extras.NEXT_ACTION_KEY";

    public static final String INTENT_LOGIN            = "com.codeminders.imageshackdroid.activity.LOGIN";
    public static final String INTENT_LOGOUT           = "com.codeminders.imageshackdroid.activity.LOGOUT";
    public static final String INTENT_NETERR           = "com.codeminders.imageshackdroid.activity.NETERR";

    public static final String INTENT_CHANGE_ACCOUNT   = "com.codeminders.imageshackdroid.activity.CHANGE_ACCOUNT";
    public static final String INTENT_ACCOUNT_SETUP    = "com.codeminders.imageshackdroid.ACCOUNT_SETUP";

    public static final int    TYPE_IMAGE              = 0;
    public static final int    TYPE_VIDEO              = 1;

    public static final String STATUS_WAIT             = "Waiting";
    public static final String STATUS_PAUSE            = "Paused";
    public static final String STATUS_UPLOAD           = "Uploading";

    public static final String PREFS_NAME              = "ImageshackDroidPrefs";
    public static final String PASS_PREF_KEY           = "pass";
    public static final String USER_PREF_KEY           = "user";
    public static final String CURR_USER_PREF_KEY      = "currentUser";

    public static final String SYNC_INTERVAL           = "com.codeminders.imageshackdroid.SYNC_INTERVAL";
    public static final String SYNC_TYPE               = "com.codeminders.imageshackdroid.SYNC_TYPE";


}
