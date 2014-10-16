package jp.co.fnj.meecologin.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import jp.co.fnj.meecologin.BuildConfig;
import jp.co.fnj.meecologin.R;
import jp.co.fnj.meecologin.api.MeecoApi;
import jp.co.fnj.meecologin.model.LoginJsonModel;
import jp.co.fnj.meecologin.model.Response;
import jp.co.fnj.meecologin.task.AuthTokenTask;
import jp.co.fnj.meecologin.task.LogoutTask;
import jp.co.fnj.meecologin.utils.PreferenceUtils;
import jp.co.fnj.meecologin.utils.Utils;

public class ContentsActivity extends Activity {

    public static final String EXTRA_URL = "url";
    public static final String EXTRA_ERROR = "error";

    @InjectView(R.id.web_view)
    WebView mWebView;
    @InjectView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @InjectView(R.id.menu)
    RelativeLayout mMenu;

    private Context mContext;
    private ActionBar mActionBar;
    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contents);
        ButterKnife.inject(this);
        mContext = this;

        mActionBar = getActionBar();
        assert mActionBar != null;
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setLogo(R.drawable.logo);

        if (BuildConfig.INSIDE_BROWSER_HORIZONTAL) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM, ActionBar.DISPLAY_SHOW_CUSTOM);
            View view = getLayoutInflater().inflate(R.layout.action_bar_contents, null);
            view.findViewById(R.id.hide_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mHandler.removeCallbacks(mRunnable);
                    mActionBar.hide();
                }
            });
            mActionBar.setCustomView(view, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.MATCH_PARENT));
            mActionBar.setDisplayShowCustomEnabled(true);
            mActionBar.hide();

            mMenu.setVisibility(View.VISIBLE);
        }

        final Intent intent = getIntent();
        if (intent.getBooleanExtra(EXTRA_ERROR, false)) {
            displayError();
            return;
        }

        String authToken = intent.getStringExtra(EXTRA_URL);
        if (!TextUtils.isEmpty(authToken)) {
            // ログイン画面から遷移してきたとき
            displayContents(authToken);
        } else {
            // authToken を取得する
            new AuthTokenTask(this) {
                @Override
                protected void onPostExecute(Response response) {
                    super.onPostExecute(response);
                    response.visit(new Response.Visitor() {
                        @Override
                        public void onSuccess(String token) {
                            displayContents(token);
                        }

                        @Override
                        public void onUrlNotFound() {
                            displayError();
                        }

                        @Override
                        public void onAuthenticationError() {
                            AccountManager manager = AccountManager.get(ContentsActivity.this);
                            String tokenType = getString(R.string.account_type);
                            Account[] accounts = manager.getAccountsByType(tokenType);
                            manager.removeAccount(accounts[0], null, null);

                            Intent intent = new Intent(mContext, LoginActivity.class);
                            TaskStackBuilder builder = TaskStackBuilder.create(mContext);
                            builder.addNextIntent(intent);
                            builder.startActivities();
                            finish();
                        }

                        @Override
                        public void onNetworkError() {
                            Utils.showToast(ContentsActivity.this, getString(R.string.error_network_connection_failure));
                        }

                        @Override
                        public void onError() {
                            Utils.showToast(mContext, getString(R.string.error_login_failure));
                        }
                    });
                }
            }.execute();
        }
    }

    /**
     * WebView にコンテンツを表示する
     *
     * @param url 表示する URL
     */
    private void displayContents(String url) {

        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);

        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                mProgressBar.setIndeterminate(true);
                mProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                mProgressBar.setVisibility(View.GONE);
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                mProgressBar.setIndeterminate(false);
                mProgressBar.setProgress(newProgress);
            }
        });

        mWebView.loadUrl(url);
    }

    /**
     * WebView にエラーメッセージを表示
     */
    private void displayError() {
        mProgressBar.setVisibility(View.GONE);
        mWebView.loadData(getString(R.string.not_available_html), "text/html; charset=utf-8", "utf-8");
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

    private void logout() {
        PreferenceUtils.setKeepLoginChecked(this, false);
        new LogoutTask(this) {
            @Override
            protected void onPostExecute(List<LoginJsonModel> resultList) {
                super.onPostExecute(resultList);

                if (resultList == null) {
                    Utils.showToast(mContext, getString(R.string.error_network_connection_failure));
                    return;
                }
                LoginJsonModel result = resultList.get(0);
                switch (result.getStatus()) {
                    case MeecoApi.API_STATUS_SUCCESS:
                        Intent intent = new Intent(mContext, LoginActivity.class);
                        TaskStackBuilder builder = TaskStackBuilder.create(mContext);
                        builder.addNextIntent(intent);
                        builder.startActivities();
                        finish();
                        break;
                }
            }
        }.execute();
    }

    private Runnable mRunnable = new Runnable() {
        public void run() {
            mActionBar.hide();
        }
    };

    @OnClick(R.id.menu)
    void OpenActionBar() {
        mActionBar.show();

        // 5 秒後にアクションバーを隠す
        mHandler.postDelayed(mRunnable, 5000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contents, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                logout();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
