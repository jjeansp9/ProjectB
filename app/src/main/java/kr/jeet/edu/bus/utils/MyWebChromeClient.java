package kr.jeet.edu.bus.utils;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Message;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MyWebChromeClient extends WebChromeClient {

    private static final String TAG = "MyWebChromeClient";

    private WebView newWebView;
    private AppCompatActivity activity;


    public MyWebChromeClient(AppCompatActivity mActivity) {
        this.activity = mActivity;
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        LogMgr.e("WebView", "Event!! create");
        newWebView = new WebView(view.getContext());
        WebSettings webSettings = newWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);

        newWebView.setWebViewClient(new MyWebViewClient(activity, newWebView));
        newWebView.setWebChromeClient(new MyWebChromeClient(activity) {
            @Override
            public void onCloseWindow(WebView window) {
                LogMgr.e("WebView", "Event!! close2");

                if (newWebView != null) newWebView.destroy();
                if (window != null) window.destroy();

                super.onCloseWindow(window);
            }
        });
        activity.setContentView(newWebView);

        WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(newWebView);
        resultMsg.sendToTarget();

        return true;
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
        LogMgr.e(TAG, "onJsAlert() url ["+url+"], msg ="+message);
        new AlertDialog.Builder(view.getContext())
                .setMessage(message)
                .setPositiveButton(android.R.string.ok,
                        new AlertDialog.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                result.confirm();
                            }
                        })
                .setCancelable(false)
                .create()
                .show();

        return true;
    }
}
