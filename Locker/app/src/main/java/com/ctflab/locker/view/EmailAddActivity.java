package com.ctflab.locker.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.FrameActivity;
import com.ctflab.locker.MainActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.StringUtil;
import com.ctflab.locker.utils.SystemUtil;

public class EmailAddActivity extends FrameActivity implements View.OnClickListener {
    private Button btnSave;
    private EditText edtEmail;
    private TextView txtSkip;
    private boolean isFromSetting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFromSetting = getIntent().getBooleanExtra("fromSetting", false);
        txtSkip = (TextView) findViewById(R.id.txtAddEmailSkip);
        if (isFromSetting) {
            txtSkip.setVisibility(View.GONE);
        } else {
            getSupportActionBar().hide();
            findViewById(R.id.txtAddEmailSkip).setOnClickListener(this);
        }

        btnSave = (Button) findViewById(R.id.btnAddEmailSave);
        btnSave.setOnClickListener(this);

        edtEmail = (EditText) findViewById(R.id.edtAddEmailInput);
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
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_email_add;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddEmailSave: {

                String email = edtEmail.getEditableText().toString();
                if (StringUtil.isEmail(email)) {
                    SettingsUtil.setEmail(EmailAddActivity.this, email);
                    gotoNext();
                } else {
                    edtEmail.setText("");
                    edtEmail.setHint(R.string.email_add_error);
                }

                break;
            }
            case R.id.txtAddEmailSkip: {
                UHAnalytics.changeDataCount(UserHabitID.LM_3);
                gotoNext();
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        edtEmail.requestFocus();
        if (PreferencesData.isShowAuthentication()){
            SystemUtil.go2Authen(this);
            return;
        }
    }

    private void gotoNext() {
        SystemUtil.hideKeyboard(edtEmail);
        if(isFromSetting){
            UHAnalytics.changeDataCount(UserHabitID.LM_4);
        }else{
            UHAnalytics.changeDataCount(UserHabitID.LM_2);
            SystemUtil.startActivity(EmailAddActivity.this,new Intent(EmailAddActivity.this, MainActivity.class));
        }
        finish();
    }
}
