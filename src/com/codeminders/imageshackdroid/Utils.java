package com.codeminders.imageshackdroid;

import android.content.Context;
import android.content.Intent;

/**
 * @author Igor Giziy <linsalion@gmail.com>
 */
public class Utils {

    public static String getFileSize(long size) {
        long kb = 1024;
        long mb = 1024 * kb;
        long gb = 1024 * mb;

        if (size < kb) {
            return size + " bytes";
        } else if (size < mb) {
            return round(size / kb, 2) + " Kb";
        } else if (size < gb) {
            return round(size / mb, 2) + " Mb";
        } else {
            return round(size / gb, 2) + " Gb";
        }
    }

    private static int round(float Rval, int Rpl) {
        float p = (float) Math.pow(10, Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);

        return (int) (tmp / p);
    }

    public static void share(Context context, String text) {
        text = text.concat("\n\n\n" + context.getString(R.string.share_signature));
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share) + ":"));
    }

}
