package jp.co.fnj.meecologin.model;

public class Response {

    private final ErrorType mErrorType;
    private final String mToken;

    public Response(String token) {
        this.mErrorType = ErrorType.None;
        this.mToken = token;
    }

    public Response(ErrorType errorType) {
        this.mErrorType = errorType;
        this.mToken = null;
    }

    public enum ErrorType {
        None,
        UrlNotFound,
        AuthenticationError,
        NetworkError,
        Error
    }

    public interface Visitor {
        public void onSuccess(String token);

        public void onUrlNotFound();

        public void onAuthenticationError();

        public void onNetworkError();

        public void onError();
    }

    public void visit(Visitor visitor) {
        if (mToken != null) {
            visitor.onSuccess(mToken);
        } else if (mErrorType == ErrorType.AuthenticationError) {
            visitor.onAuthenticationError();
        } else if (mErrorType == ErrorType.UrlNotFound) {
            visitor.onUrlNotFound();
        } else if (mErrorType == ErrorType.Error) {
            visitor.onError();
        } else {
            visitor.onNetworkError();
        }
    }
}