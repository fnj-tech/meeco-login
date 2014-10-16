package jp.co.fnj.meecologin.model;

import net.vvakame.util.jsonpullparser.annotation.JsonKey;
import net.vvakame.util.jsonpullparser.annotation.JsonModel;


@JsonModel
public class LoginJsonModel {

    @JsonKey
    int status;

    @JsonKey
    String meecoUrl;

    @JsonKey
    String remocoUrl;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMeecoUrl() {
        return meecoUrl;
    }

    public void setMeecoUrl(String meecoUrl) {
        this.meecoUrl = meecoUrl;
    }

    public String getRemocoUrl() {
        return remocoUrl;
    }

    public void setRemocoUrl(String remocoUrl) {
        this.remocoUrl = remocoUrl;
    }
}
