package com.ctflab.locker.widget;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;

import com.ctflab.locker.R;

public class BottomAlertDialogBuilder extends AlertDialog.Builder {

    public BottomAlertDialogBuilder(Context context) {
        super(context, R.style.BottomAlertDialg);
    }

    @Override
    public AlertDialog create() {
        AlertDialog dialog = super.create();
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        return dialog;
    }
}
