package jp.co.fnj.meecologin.task;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.text.TextUtils;

import java.io.IOException;

import jp.co.fnj.meecologin.R;
import jp.co.fnj.meecologin.model.Response;


public class AuthTokenTask extends ModalProgressTask<Void, Void, Response> {

    private Context mContext;

    public AuthTokenTask(Context context) {
        super(context, context.getString(R.string.progress_login));
        mContext = context;
    }

    @Override
    protected Response doInBackground(Void... params) {
        AccountManager manager = AccountManager.get(mContext);
        String tokenType = mContext.getString(R.string.account_type);
        Account[] accounts = manager.getAccountsByType(tokenType);
        try {
            final String s = manager.blockingGetAuthToken(accounts[0], tokenType, true);
            if (TextUtils.isEmpty(s)) {
                return new Response(Response.ErrorType.UrlNotFound);
            } else {
                return new Response(s);
            }
        } catch (OperationCanceledException | AuthenticatorException e) {
            e.printStackTrace();
            return new Response(Response.ErrorType.AuthenticationError);
        } catch (IOException e) {
            e.printStackTrace();
            return new Response(Response.ErrorType.NetworkError);
        }
    }
}

