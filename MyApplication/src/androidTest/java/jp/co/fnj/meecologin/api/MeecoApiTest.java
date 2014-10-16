package jp.co.fnj.meecologin.api;

import android.content.res.AssetManager;
import android.test.InstrumentationTestCase;

import java.io.InputStream;
import java.util.List;

import jp.co.fnj.meecologin.model.LoginJsonModel;
import jp.co.fnj.meecologin.model.LoginJsonModelGen;
import jp.co.fnj.meecologin.utils.Utils;

public class MeecoApiTest extends InstrumentationTestCase {

    public void testLoginSuccess() throws Exception {
        List<LoginJsonModel> result = MeecoApi.login("daiwa-demo", "demo-daiwa");

        assertEquals(MeecoApi.API_STATUS_SUCCESS, result.get(0).getStatus());
        assertNotNull(result.get(0).getMeecoUrl());
    }

    public void testLoginFailure() throws Exception {
        List<LoginJsonModel> result = MeecoApi.login("abc", "abc");

        assertEquals(MeecoApi.API_STATUS_FAILURE, result.get(0).getStatus());
        assertNull(result.get(0).getMeecoUrl());
    }

    public void testLogoutSuccess() throws Exception {
        List<LoginJsonModel> result = MeecoApi.logout();

        assertEquals(MeecoApi.API_STATUS_SUCCESS, result.get(0).getStatus());
    }

    /**
     * meecoUrl がある場合は meecoUrl を返す
     *
     * @throws Exception
     */
    public void testGetUrlMeeco() throws Exception {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();

        InputStream in = assetManager.open("response_meeco.json");

        final List<LoginJsonModel> result = LoginJsonModelGen.getList(in);
        final String url = Utils.getTransitionUrl(result.get(0));

        assertEquals("meeco", url);
    }

    /**
     * meecoUrl がなくて remocoUrl がある場合は remocoUrl を返す
     *
     * @throws Exception
     */
    public void testGetUrlRemoco() throws Exception {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();

        InputStream in = assetManager.open("response_remoco.json");

        final List<LoginJsonModel> result = LoginJsonModelGen.getList(in);
        final String url = Utils.getTransitionUrl(result.get(0));

        assertEquals("remoco", url);
    }

    /**
     * meecoUrl と remocoUrl 両方共ない場合はnullを返す
     *
     * @throws Exception
     */
    public void testGetUrlNull() throws Exception {
        AssetManager assetManager = getInstrumentation().getContext().getAssets();

        InputStream in = assetManager.open("response_null.json");

        final List<LoginJsonModel> result = LoginJsonModelGen.getList(in);
        final String url = Utils.getTransitionUrl(result.get(0));

        assertNull(url);
    }
}
