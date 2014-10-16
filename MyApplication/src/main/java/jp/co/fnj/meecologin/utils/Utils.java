package jp.co.fnj.meecologin.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.co.fnj.meecologin.model.LoginJsonModel;


public class Utils {

    private static final String REGEN_URL = "start";

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    /**
     * http://www03.me-eco.jp/mini/start/$token → http://www03.me-eco.jp/mini/setup/$token
     *
     * @param s
     * @return 置換した文字列
     */
    public static String replaceUrl(String s) {
        return s.replaceAll(REGEN_URL, "setup");
    }

    /**
     * デフォルト起動に設定されているブラウザのパッケージ名を取得
     *
     * @param context
     * @return デフォルト起動に設定されているブラウザのパッケージ名
     */
    public static String getDefaultBrowserApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);

        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        for (ResolveInfo info : list) {
            List<IntentFilter> filters = new ArrayList<>();
            List<ComponentName> activities = new ArrayList<>();
            pm.getPreferredActivities(filters, activities, info.activityInfo.packageName);
            if (activities.size() > 0) {
                return activities.get(0).getPackageName();
            }
        }

        return null;
    }

    /**
     * 遷移先URLを返す
     *
     * @param result
     * @return meecoUrl があったら meecoUrl 、remocoUrl だけの場合は remocoUrl 、両方共なかった場合は null
     */
    public static String getTransitionUrl(LoginJsonModel result) {
        if (!TextUtils.isEmpty(result.getMeecoUrl())) {
            return replaceUrl(result.getMeecoUrl());
        } else if (!TextUtils.isEmpty(result.getRemocoUrl())) {
            return result.getRemocoUrl();
        } else {
            return null;
        }
    }
}
