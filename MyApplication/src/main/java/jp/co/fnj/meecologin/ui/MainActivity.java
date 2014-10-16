package jp.co.fnj.meecologin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;

import jp.co.fnj.meecologin.R;
import jp.co.fnj.meecologin.utils.PreferenceUtils;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountManager manager = AccountManager.get(this);
        String tokenType = getString(R.string.account_type);
        Account[] accounts = manager.getAccountsByType(tokenType);

        if (accounts == null || accounts.length == 0 || !PreferenceUtils.keepLoginChecked(this)) {
            // AccountManager にアカウントがないとき、ログイン状態を保持するにチェックがないとき、外部ブラウザ版はログイン画面に遷移
            Intent intent = new Intent(this, LoginActivity.class);
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntent(intent);
            builder.startActivities();
            finish();
        } else {
            Intent intent = new Intent(this, ContentsActivity.class);
            TaskStackBuilder builder = TaskStackBuilder.create(this);
            builder.addNextIntent(intent);
            builder.startActivities();
            finish();
        }
    }
}