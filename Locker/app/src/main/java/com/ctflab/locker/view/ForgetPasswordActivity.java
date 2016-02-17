package com.ctflab.locker.view;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.anguanjia.framework.net.RequestException;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.FrameActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.net.PwdRecoveryRequest;
import com.ctflab.locker.utils.LanguageManager;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.StringUtil;
import com.ctflab.locker.utils.SystemUtil;

import org.json.JSONObject;

public class ForgetPasswordActivity extends FrameActivity implements View.OnClickListener {
    private static final int TYPE_NORMAL = 1;
    private static final int TYPE_ERROR = 2;
    private static final int TYPE_NET = 3;
    private static final int TYPE_SUCCESS = 4;
    private static final int TYPE_LOADING = 5;

    private static int DELAY_FINISH = 5000;
    private static int MSG_FINISH = 1;

    private View viewForgetPWError, viewForgetPWNet, viewForgetPWNormal, viewForgetPWSuccess, viewForgetPWLoading;
    private TextView txtForgetPWEmail,txtForgetPWEmail2;
    private Button btnForgetPWNet, btnForgetPWNormal, btnForgetPWSuccess;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MSG_FINISH) {
                SystemUtil.showHome(ForgetPasswordActivity.this);
                finish();
            } else {
                changeView(msg.what);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UHAnalytics.changeDataCount(UserHabitID.LM_29);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setTitle(R.string.title_activity_forget_password);
        initWidgets();

        String email = SettingsUtil.getEmail(this);
        if (StringUtil.isEmpty(email)) {
            UHAnalytics.changeDataCount(UserHabitID.LM_30);
            changeView(TYPE_ERROR);
            mHandler.sendEmptyMessageDelayed(MSG_FINISH, DELAY_FINISH);
        } else {
            changeView(TYPE_NORMAL);
            txtForgetPWEmail.setText(email);
            txtForgetPWEmail2.setText(email);
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_forget_password;
    }

    private void initWidgets() {
        viewForgetPWError = findViewById(R.id.viewForgetPWError);
        viewForgetPWNet = findViewById(R.id.viewForgetPWNet);
        viewForgetPWNormal = findViewById(R.id.viewForgetPWNormal);
        viewForgetPWSuccess = findViewById(R.id.viewForgetPWSuccess);
        viewForgetPWLoading = findViewById(R.id.viewForgetPWLoading);

        txtForgetPWEmail = (TextView) findViewById(R.id.txtForgetPWEmail);
        txtForgetPWEmail2 = (TextView) findViewById(R.id.txtForgetPWEmail2);

        btnForgetPWNet = (Button) findViewById(R.id.btnForgetPWNet);
        btnForgetPWNet.setOnClickListener(this);
        btnForgetPWNormal = (Button) findViewById(R.id.btnForgetPWNormal);
        btnForgetPWNormal.setOnClickListener(this);
        btnForgetPWSuccess = (Button) findViewById(R.id.btnForgetPWSuccess);
        btnForgetPWSuccess.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnForgetPWNet: {
                UHAnalytics.changeDataCount(UserHabitID.LM_31);
                sendForgetRequest();
                changeView(TYPE_LOADING);
                break;
            }
            case R.id.btnForgetPWNormal: {
                UHAnalytics.changeDataCount(UserHabitID.LM_31);
                sendForgetRequest();
                changeView(TYPE_LOADING);
                break;
            }
            case R.id.btnForgetPWSuccess: {
                mHandler.sendEmptyMessage(MSG_FINISH);
                break;
            }
        }
    }

    private void changeView(int type) {
        switch (type) {
            case TYPE_NORMAL: {
                viewForgetPWError.setVisibility(View.GONE);
                viewForgetPWNet.setVisibility(View.GONE);
                viewForgetPWNormal.setVisibility(View.VISIBLE);
                viewForgetPWSuccess.setVisibility(View.GONE);
                viewForgetPWLoading.setVisibility(View.GONE);
                break;
            }
            case TYPE_ERROR: {
                viewForgetPWError.setVisibility(View.VISIBLE);
                viewForgetPWNet.setVisibility(View.GONE);
                viewForgetPWNormal.setVisibility(View.GONE);
                viewForgetPWSuccess.setVisibility(View.GONE);
                viewForgetPWLoading.setVisibility(View.GONE);
                break;
            }
            case TYPE_NET: {
                viewForgetPWError.setVisibility(View.GONE);
                viewForgetPWNet.setVisibility(View.VISIBLE);
                viewForgetPWNormal.setVisibility(View.GONE);
                viewForgetPWSuccess.setVisibility(View.GONE);
                viewForgetPWLoading.setVisibility(View.GONE);
                break;
            }
            case TYPE_SUCCESS: {
                viewForgetPWError.setVisibility(View.GONE);
                viewForgetPWNet.setVisibility(View.GONE);
                viewForgetPWNormal.setVisibility(View.GONE);
                viewForgetPWSuccess.setVisibility(View.VISIBLE);
                viewForgetPWLoading.setVisibility(View.GONE);
                break;
            }
            case TYPE_LOADING: {
                viewForgetPWError.setVisibility(View.GONE);
                viewForgetPWNet.setVisibility(View.GONE);
                viewForgetPWNormal.setVisibility(View.GONE);
                viewForgetPWSuccess.setVisibility(View.GONE);
                viewForgetPWLoading.setVisibility(View.VISIBLE);
                break;
            }
        }
    }

    private void sendForgetRequest() {
        new PwdRecoveryRequest(SettingsUtil.getEmail(this), PreferencesData.getPassword(this), PreferencesData.getPasswordModle(this) + "", LanguageManager.loadLocaleString(this)) {

            @Override
            protected void parseNetworkResponse(JSONObject response) {
                UHAnalytics.changeDataCount(UserHabitID.LM_32);
                mHandler.sendEmptyMessage(TYPE_SUCCESS);
            }

            @Override
            protected void onErrorResponse(RequestException e) {
                mHandler.sendEmptyMessage(TYPE_NET);
            }
        }.commit(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        SystemUtil.showHome(ForgetPasswordActivity.this);
        mHandler.removeMessages(MSG_FINISH);
    }
}
