package kr.jeet.edu.bus.utils;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.net.URISyntaxException;

import kr.jeet.edu.bus.R;


public class MyWebViewClient extends WebViewClient {

    private static final String TAG = "MyWebViewClient";

    private WebView wv;

    private AppCompatActivity activity;
    private AlertDialog mProgressDialog = null;

    public MyWebViewClient(AppCompatActivity mActivity, WebView webView) {
        this.activity = mActivity;
        this.wv = webView;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (!request.getUrl().toString().startsWith("http://") && !request.getUrl().toString().startsWith("https://")) {
            if (request.getUrl().toString().startsWith("intent")) {
                Intent schemeIntent;
                try {
                    schemeIntent = Intent.parseUri(request.getUrl().toString(), Intent.URI_INTENT_SCHEME);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }
                try {
                    activity.startActivity(schemeIntent);
                    return true;
                } catch (ActivityNotFoundException e) {
                    String pkgName = schemeIntent.getPackage();
                    if (pkgName != null) {
                        activity.startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" + pkgName)
                                )
                        );
                        return true;
                    }
                }
            } else {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(request.getUrl().toString())));
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        } else {
            view.loadUrl(request.getUrl().toString());
        }

        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        LogMgr.e(TAG, "onPageStarted() : " + url);
        showProgressDialog();
        wv.setVisibility(View.GONE);
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        LogMgr.e(TAG, "onLoadResource() : " + url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        LogMgr.e(TAG, "onPageFinished() : " + url);
        hideProgressDialog();
        wv.setVisibility(View.VISIBLE);

    }

    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
        super.onReceivedError(view, request, error);

        Toast.makeText(activity, R.string.server_error, Toast.LENGTH_SHORT).show();

        // 오류가 났을 때 대체 페이지 로드
        //wv.loadUrl("");
    }

    private void showProgressDialog() {
        if (mProgressDialog == null){
            View view = activity.getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
            TextView txt = view.findViewById(R.id.text);
            txt.setText(activity.getString(R.string.requesting));

            mProgressDialog = new AlertDialog.Builder(activity)
                    .setCancelable(false)
                    .setView(view)
                    .create();
            mProgressDialog.show();
        }
    }

    private void hideProgressDialog() {
        try {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }catch (Exception e){
            LogMgr.e("hideProgressDialog()", e.getMessage());
        }
    }
}
