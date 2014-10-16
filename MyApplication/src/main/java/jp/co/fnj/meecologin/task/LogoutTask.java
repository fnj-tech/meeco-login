package jp.co.fnj.meecologin.task;

import android.content.Context;

import java.util.List;

import jp.co.fnj.meecologin.R;
import jp.co.fnj.meecologin.api.MeecoApi;
import jp.co.fnj.meecologin.model.LoginJsonModel;


public class LogoutTask extends ModalProgressTask<Void, Void, List<LoginJsonModel>> {

    public LogoutTask(Context context) {
        super(context, context.getString(R.string.progress_logout));
    }

    @Override
    protected List<LoginJsonModel> doInBackground(Void... params) {
        return MeecoApi.logout();
    }
}
