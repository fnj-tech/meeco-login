package jp.co.fnj.meecologin.service;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;

import jp.co.fnj.meecologin.R;
import jp.co.fnj.meecologin.api.MeecoApi;
import jp.co.fnj.meecologin.model.LoginJsonModel;
import jp.co.fnj.meecologin.utils.Utils;


public class MeecoAuthenticator extends AbstractAccountAuthenticator {

    private Context mContext;

    public MeecoAuthenticator(Context context) {
        super(context);
        mContext = context;
    }

    private Bundle createErrorResult(int errorCode, String errorMessage) {
        Bundle result = new Bundle();
        result.putInt(AccountManager.KEY_ERROR_CODE, errorCode);
        result.putString(AccountManager.KEY_ERROR_MESSAGE, errorMessage);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {

        AccountManager manager = AccountManager.get(mContext);
        assert manager != null;

        final String id = account.name;
        final String pass = manager.getPassword(account);

        List<LoginJsonModel> resultList = MeecoApi.login(id, pass);

        if (resultList == null) {
            throw new NetworkErrorException(mContext.getString(R.string.error_network_connection_failure));
        }
        LoginJsonModel result = resultList.get(0);
        Bundle bundle = new Bundle();
        switch (result.getStatus()) {
            case MeecoApi.API_STATUS_SUCCESS:
                bundle.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                bundle.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                final String s = Utils.getTransitionUrl(result);
                if (!TextUtils.isEmpty(s)) {
                    bundle.putString(AccountManager.KEY_AUTHTOKEN, s);
                }
                return bundle;
            case MeecoApi.API_STATUS_FAILURE:
                return createErrorResult(AccountManager.ERROR_CODE_BAD_REQUEST, mContext.getString(R.string.error_login_no_user));
            case MeecoApi.API_STATUS_ERROR:
                return createErrorResult(AccountManager.ERROR_CODE_BAD_REQUEST, mContext.getString(R.string.error_login_failure));
            default:
                return createErrorResult(AccountManager.ERROR_CODE_BAD_REQUEST, mContext.getString(R.string.error_login_failure));
        }
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws NetworkErrorException {
        return null;
    }
}
