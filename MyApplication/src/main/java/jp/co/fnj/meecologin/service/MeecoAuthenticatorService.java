package jp.co.fnj.meecologin.service;

import android.accounts.AccountManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class MeecoAuthenticatorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        if (intent.getAction().equals(AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            return new MeecoAuthenticator(this).getIBinder();
        }
        return null;
    }
}