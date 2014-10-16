package jp.co.fnj.meecologin.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * バックグラウンド処理の間にキャンセルできないプログレスダイアログを表示する
 */
public abstract class ModalProgressTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

    private final WeakReference<ProgressDialog> mProgressRef;

    public ModalProgressTask(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        mProgressRef = new WeakReference<>(dialog);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        ProgressDialog dialog = mProgressRef.get();
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        ProgressDialog dialog = mProgressRef.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        ProgressDialog dialog = mProgressRef.get();
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
