package jp.co.fnj.meecologin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.co.fnj.meecologin.BuildConfig;
import jp.co.fnj.meecologin.R;
import jp.co.fnj.meecologin.model.Response;
import jp.co.fnj.meecologin.task.LoginTask;
import jp.co.fnj.meecologin.utils.PreferenceUtils;
import jp.co.fnj.meecologin.utils.Utils;


public class LoginActivity extends Activity {

    @InjectView(R.id.user_name_text)
    EditText mUserNameText;
    @InjectView(R.id.password_text)
    EditText mPasswordText;
    @InjectView(R.id.keep_login_check)
    CheckBox mKeepLoginCheck;
    @InjectView(R.id.login_button)
    Button mLoginButton;
    @InjectView(R.id.default_button)
    Button mDefaultButton;

    private Context mContext;

    // フィルターを作成
    InputFilter[] inputFilters = new InputFilter[]{
            new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    if (source.toString().matches("^[0-9a-zA-Z@¥.¥_¥¥-]+$")) {
                        return source;
                    } else {
                        return "";
                    }
                }
            },
            new InputFilter.LengthFilter(50)
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        mContext = this;
        ActionBar actionBar = getActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setLogo(R.drawable.logo);

        mUserNameText.setFilters(inputFilters);
        mPasswordText.setFilters(inputFilters);

        TextWatcher t = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                controlLoginButton();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        mUserNameText.addTextChangedListener(t);
        mPasswordText.addTextChangedListener(t);

        AccountManager manager = AccountManager.get(this);
        String tokenType = getString(R.string.account_type);
        Account[] accounts = manager.getAccountsByType(tokenType);
        if (accounts != null && accounts.length > 0) {

            if (PreferenceUtils.keepLoginChecked(this) && BuildConfig.INSIDE_BROWSER) {
                Intent intent = new Intent(this, ContentsActivity.class);
                TaskStackBuilder builder = TaskStackBuilder.create(this);
                builder.addNextIntent(intent);
                builder.startActivities();
                finish();
            }

            // AccountManager にアカウントがある場合はユーザー名・パスワードをプレフィルする
            Account account = accounts[0];
            mUserNameText.setText(account.name);
            mPasswordText.setText(manager.getPassword(account));
        }
        mKeepLoginCheck.setChecked(PreferenceUtils.keepLoginChecked(this));

        if (!BuildConfig.INSIDE_BROWSER) {
            final String defaultApp = Utils.getDefaultBrowserApp(LoginActivity.this);
            if (!TextUtils.isEmpty(defaultApp)) {
                mDefaultButton.setVisibility(View.VISIBLE);
            }
            mKeepLoginCheck.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.login_button)
    void login() {
        final String userName = mUserNameText.getText().toString();
        final String password = mPasswordText.getText().toString();

        new LoginTask(this) {
            @Override
            protected void onPostExecute(Response response) {
                super.onPostExecute(response);
                response.visit(new Response.Visitor() {
                    @Override
                    public void onSuccess(final String token) {
                        final AccountManager manager = AccountManager.get(LoginActivity.this);
                        Account[] accounts = manager.getAccountsByType(getString(R.string.account_type));

                        if (accounts != null && accounts.length > 0) {
                            // 前にログインしたアカウントが残っていたら削除する
                            final Account account = accounts[0];
                            manager.removeAccount(account, new AccountManagerCallback<Boolean>() {
                                public void run(AccountManagerFuture<Boolean> future) {
                                    try {
                                        if (future.getResult()) {
                                            addAccount(userName, password);
                                            if (BuildConfig.INSIDE_BROWSER) {
                                                moveToContents(token);
                                            } else {
                                                openBrowser(token);
                                            }
                                        } else {
                                            Log.d("LOG", "removeAccount　failed.");
                                        }
                                    } catch (OperationCanceledException | AuthenticatorException | IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, null);
                        } else {
                            addAccount(userName, password);
                            if (BuildConfig.INSIDE_BROWSER) {
                                moveToContents(token);
                            } else {
                                openBrowser(token);
                            }
                        }
                    }

                    @Override
                    public void onUrlNotFound() {
                        addAccount(userName, password);

                        Intent intent = new Intent(LoginActivity.this, ContentsActivity.class);
                        intent.putExtra(ContentsActivity.EXTRA_ERROR, true);
                        TaskStackBuilder builder = TaskStackBuilder.create(LoginActivity.this);
                        builder.addNextIntent(intent);
                        builder.startActivities();
                        finish();
                    }

                    @Override
                    public void onAuthenticationError() {
                        Utils.showToast(mContext, getString(R.string.error_login_no_user));
                    }

                    @Override
                    public void onNetworkError() {
                        Utils.showToast(mContext, getString(R.string.error_network_connection_failure));
                    }

                    @Override
                    public void onError() {
                        Utils.showToast(mContext, getString(R.string.error_login_failure));
                    }
                });
            }
        }.execute(userName, password);
    }

    @OnClick(R.id.link_login_method_label)
    void moveAboutLogin() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.url_login_method)));
        startActivity(intent);
    }

    @OnClick(R.id.default_button)
    void openAppSettings() {
        final String defaultApp = Utils.getDefaultBrowserApp(LoginActivity.this);
        if (TextUtils.isEmpty(defaultApp)) {
            Utils.showToast(LoginActivity.this, getString(R.string.toast_no_default_app));
        } else {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + defaultApp));
            startActivity(intent);
        }
    }

    private void controlLoginButton() {
        boolean isEnable = !TextUtils.isEmpty(mUserNameText.getText()) && !TextUtils.isEmpty(mPasswordText.getText());
        mLoginButton.setEnabled(isEnable);
    }

    private void moveToContents(String url) {
        Intent intent = new Intent(this, ContentsActivity.class);
        intent.putExtra(ContentsActivity.EXTRA_URL, url);
        TaskStackBuilder builder = TaskStackBuilder.create(this);
        builder.addNextIntent(intent);
        builder.startActivities();
        finish();
    }

    private void openBrowser(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void addAccount(String userName, String password) {
        // AccountManager にユーザー名とパスワード保存する
        Account account = new Account(userName, getString(R.string.account_type));
        final AccountManager manager = AccountManager.get(this);
        manager.addAccountExplicitly(account, password, null);
        if (mKeepLoginCheck.isChecked()) {
            PreferenceUtils.setKeepLoginChecked(LoginActivity.this, true);
        } else {
            PreferenceUtils.setKeepLoginChecked(LoginActivity.this, false);
        }
    }
}