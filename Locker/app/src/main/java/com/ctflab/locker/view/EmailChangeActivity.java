package com.ctflab.locker.view;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anguanjia.framework.preferece.CommonPreference;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.FrameActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.StringUtil;
import com.ctflab.locker.utils.SystemUtil;

public class EmailChangeActivity extends FrameActivity {

    private TextView txtCurrent;
    private EditText edtEmail;
    private Button btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_activity_change_email);

        txtCurrent = (TextView) findViewById(R.id.txtChangeEmailCurrent);
        txtCurrent.setText(SettingsUtil.getEmail(this));

        edtEmail = (EditText) findViewById(R.id.edtChangeEmailInput);
        edtEmail.setHint("");
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (StringUtil.isEmpty(str)) {
                    btnSave.setEnabled(false);
                } else {
                    btnSave.setEnabled(true);
                }
            }
        });

        btnSave = (Button) findViewById(R.id.btnChangeEmailSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getEditableText().toString();
                if (StringUtil.isEmail(email)) {
                    CommonPreference.getDefault(EmailChangeActivity.this).saveString("setting_email", email);
                    SystemUtil.showToast(EmailChangeActivity.this, R.string.email_change_toast);
                    UHAnalytics.changeDataCount(UserHabitID.LM_24);
                    finish();
                } else {
                    edtEmail.setText("");
                    edtEmail.setHint(R.string.email_add_error);
                }
            }
        });
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_email_change;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferencesData.isShowAuthentication()) {
            SystemUtil.go2Authen(this);
            return;
        }
    }
}
