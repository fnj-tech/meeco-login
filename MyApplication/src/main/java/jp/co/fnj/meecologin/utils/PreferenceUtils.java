package jp.co.fnj.meecologin.utils;

import android.content.Context;
import android.content.SharedPreferences;


public class PreferenceUtils {

    private static final String PREF_NAME = "meeco_login";
    private static final String PREF_KEEP_LOGIN = "keep_login";

    public static void setKeepLoginChecked(Context context, boolean keepLogin) {
        SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PREF_KEEP_LOGIN, keepLogin);
        editor.apply();
    }

    public static boolean keepLoginChecked(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_KEEP_LOGIN, false);
    }
}
