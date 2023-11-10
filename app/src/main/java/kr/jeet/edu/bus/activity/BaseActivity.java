package kr.jeet.edu.bus.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import kr.jeet.edu.bus.R;
import kr.jeet.edu.bus.common.Constants;
import kr.jeet.edu.bus.dialog.PopupDialog;
import kr.jeet.edu.bus.utils.LogMgr;

public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener {

    public Context mContext;
    private AlertDialog mProgressDialog = null;
    //메세지 팝업
    protected PopupDialog popupDialog = null;
    private int move = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }
    abstract void initAppbar();
    abstract void initView();

    @Override
    public void onClick(View v) {}

    protected void showProgressDialog()
    {
        showProgressDialog(getString(R.string.requesting), null);
    }

    protected void showProgressDialog(String msg)
    {
        showProgressDialog(msg, null);
    }

    protected void showProgressDialog(DialogInterface.OnCancelListener listener)
    {
        showProgressDialog(getString(R.string.requesting), listener);
    }

    protected void showProgressDialog(String msg, DialogInterface.OnCancelListener listener) {
        runOnUiThread(() -> {
            if (mProgressDialog == null){
                View view = getLayoutInflater().inflate(R.layout.dialog_progressbar, null, false);
                TextView txt = view.findViewById(R.id.text);
                txt.setText(msg);

                mProgressDialog = new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setView(view)
                        .create();
                mProgressDialog.show();
            }
        });
    }

    protected void hideProgressDialog() {
        runOnUiThread(() -> {
            try {
                if (mProgressDialog != null) {
                    mProgressDialog.dismiss();
                    mProgressDialog = null;
                }
            } catch (Exception e) {
                LogMgr.e("hideProgressDialog()", e.getMessage());
            }
        });
    }

    protected void showMessageDialog(String title, String msg, View.OnClickListener okListener, View.OnClickListener cancelListener) {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
        popupDialog = new PopupDialog(mContext);
        popupDialog.setTitle(title);
        popupDialog.setContent(msg);
        popupDialog.setOnOkButtonClickListener(okListener);
        if(cancelListener != null) {
            popupDialog.setOnCancelButtonClickListener(cancelListener);
        }
        popupDialog.show();
    }
    protected void hideMessageDialog() {
        if(popupDialog != null && popupDialog.isShowing()) {
            popupDialog.dismiss();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // 이전 화면으로 이동
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        switch (move){
            case Constants.MOVE_DEFAULT:
                LogMgr.e("EVENT_DETAIL");
                break;
            case Constants.MOVE_LEFT:
                overridePendingTransition(R.anim.none, R.anim.horizontal_out);
                LogMgr.e("EVENT_LEFT");
                break;
            case Constants.MOVE_RIGHT:
                overridePendingTransition(R.anim.none, R.anim.horizontal_exit);
                LogMgr.e("EVENT_RIGHT");
                break;
            case Constants.MOVE_UP:
                overridePendingTransition(R.anim.none, R.anim.vertical_out);
                LogMgr.e("EVENT_UP");
                break;
            case Constants.MOVE_DOWN:
                overridePendingTransition(R.anim.none, R.anim.vertical_exit);
                LogMgr.e("EVENT_DOWN");
                break;
            case Constants.MOVE_DETAIL_RIGHT:
                overridePendingTransition(R.anim.horizontal_in, R.anim.horizontal_exit);
                LogMgr.e("EVENT_DETAIL_RIGHT");
                break;
        }
    }
    protected void setAnimMove(int setMove){
        move = setMove;
    }
}