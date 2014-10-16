package jp.co.fnj.meecologin.api;

import com.github.kevinsawicki.http.HttpRequest;
import net.vvakame.util.jsonpullparser.JsonFormatException;

import java.io.IOException;
import java.util.List;

import jp.co.fnj.meecologin.model.LoginJsonModel;
import jp.co.fnj.meecologin.model.LoginJsonModelGen;


public class MeecoApi {

    private static final String URL_LOGIN = "https://www.cyberhome.ne.jp/amm/apiLogin.do";
    private static final String URL_LOGOUT = "https://www.cyberhome.ne.jp/amm/apiLogout.do";
    public static final int API_STATUS_SUCCESS = 0;
    public static final int API_STATUS_FAILURE = 1;
    public static final int API_STATUS_ERROR = 900;

    public static List<LoginJsonModel> login(String account, String password) {
        try {
            HttpRequest request = HttpRequest.get(URL_LOGIN, true, "account", account, "password", password);
            request.acceptGzipEncoding().uncompress(true);
            if (!request.ok()) {
                return null;
            }
            return LoginJsonModelGen.getList(request.stream());
        } catch (HttpRequest.HttpRequestException | JsonFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<LoginJsonModel> logout() {
        try {
            HttpRequest request = HttpRequest.get(URL_LOGOUT, true);
            request.acceptGzipEncoding().uncompress(true);
            if (!request.ok()) {
                return null;
            }
            return LoginJsonModelGen.getList(request.stream());
        } catch (HttpRequest.HttpRequestException | JsonFormatException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
