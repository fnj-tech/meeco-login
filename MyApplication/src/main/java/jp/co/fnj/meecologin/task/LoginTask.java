package jp.co.fnj.meecologin.task;

import android.content.Context;
import android.text.TextUtils;

import java.util.List;

import jp.co.fnj.meecologin.R;
import jp.co.fnj.meecologin.api.MeecoApi;
import jp.co.fnj.meecologin.model.LoginJsonModel;
import jp.co.fnj.meecologin.model.Response;
import jp.co.fnj.meecologin.utils.Utils;


public class LoginTask extends ModalProgressTask<String, Void, Response> {

    public LoginTask(Context context) {
        super(context, context.getString(R.string.progress_login));
    }

    @Override
    protected Response doInBackground(String... params) {
        final String id = params[0];
        final String pass = params[1];

        final List<LoginJsonModel> results = MeecoApi.login(id, pass);
        if (results == null) {
            return new Response(Response.ErrorType.NetworkError);
        }

        final LoginJsonModel result = results.get(0);


        switch (result.getStatus()) {
            case MeecoApi.API_STATUS_SUCCESS:
                final String s = Utils.getTransitionUrl(result);
                if (!TextUtils.isEmpty(s)) {
                    return new Response(s);
                } else {
                    return new Response(Response.ErrorType.UrlNotFound);
                }
            case MeecoApi.API_STATUS_FAILURE:
                return new Response(Response.ErrorType.AuthenticationError);
            case MeecoApi.API_STATUS_ERROR:
                return new Response(Response.ErrorType.Error);
            default:
                return new Response(Response.ErrorType.Error);
        }
    }
}