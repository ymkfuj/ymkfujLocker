package com.ctflab.locker.widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.ctflab.locker.R;

public class LoadingDialog {
    AlertDialog dialog;
    public LoadingDialog(Context context, String message, boolean cancelabel) {
        AlertDialog.Builder builder  = new AlertDialog.Builder(context, R.style.LoadingDialog);
        View loading = View.inflate(context, R.layout.loading_dialog, null);
        TextView msgView = (TextView)loading.findViewById(R.id.textView);
        msgView.setText(message);
        dialog = builder.setCancelable(cancelabel).setView(loading).create();
    }

    public void show(){
        dialog.show();
    }

    public void dismiss(){
        dialog.dismiss();
    }
}
