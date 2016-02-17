package com.ctflab.locker.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.FrameActivity;
import com.ctflab.locker.MainActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.reciever.DeviceAdminManager;
import com.ctflab.locker.services.LockerService;
import com.ctflab.locker.utils.LanguageManager;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.StringUtil;
import com.ctflab.locker.utils.SystemUtil;
import com.ctflab.locker.widget.BottomAlertDialogBuilder;
import com.ctflab.locker.widget.SwitchButton.CheckSwitchButton;

public class SettingsActivity extends FrameActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private CheckSwitchButton swSettingAntiUninstall, swSettingState;
    private TextView txtSettingLockStyle, txtSettingEmailHint, txtSettingPassword, txtSettingLanguage, txtSettingDelay, txtSettingAntiUninstallHint;
    private View viewSettingLockType, viewSettingLanguage, viewSettingDelay, viewSettingEmail, viewHint;

    private boolean noEmail = false;
    private DeviceAdminManager deviceAdminManager;
    private AlertDialog permissionDialog;
    private boolean go2Permission;

    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                viewHint.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceAdminManager = new DeviceAdminManager(this);
        initWidgets();
        getSupportActionBar().setTitle(R.string.title_activity_settings);
//        resume = false;
    }

    private void initWidgets() {
        boolean isFromTip = getIntent().getBooleanExtra("isFormTip", false);
        viewHint = findViewById(R.id.layout_services_hint);
        viewHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHint.setVisibility(View.GONE);
            }
        });
        if (isFromTip) {
            viewHint.setVisibility(View.VISIBLE);
            mHandle.sendEmptyMessageDelayed(1, 2000);
        } else {
            viewHint.setVisibility(View.GONE);
        }

        swSettingAntiUninstall = (CheckSwitchButton) findViewById(R.id.swSettingAntiUninstall);
        swSettingAntiUninstall.setOnCheckedChangeListener(this);

        swSettingState = (CheckSwitchButton) findViewById(R.id.swSettingState);
        swSettingState.setOnCheckedChangeListener(this);

        findViewById(R.id.ll_state).setOnClickListener(this);


        viewSettingLockType = findViewById(R.id.viewSettingLockType);
        viewSettingLockType.setOnClickListener(this);

        viewSettingLanguage = findViewById(R.id.viewSettingLanguage);
        viewSettingLanguage.setOnClickListener(this);

        viewSettingDelay = findViewById(R.id.viewSettingDelay);
        viewSettingDelay.setOnClickListener(this);

        txtSettingLockStyle = (TextView) findViewById(R.id.txtSettingLockStyle);

        viewSettingEmail = findViewById(R.id.viewSettingEmail);
        viewSettingEmail.setOnClickListener(this);

        txtSettingEmailHint = (TextView) findViewById(R.id.txtSettingEmailHint);

        txtSettingPassword = (TextView) findViewById(R.id.txtSettingPassword);
        txtSettingPassword.setOnClickListener(this);

        txtSettingLanguage = (TextView) findViewById(R.id.txtSettingLanguage);

        txtSettingDelay = (TextView) findViewById(R.id.txtSettingDelay);

        txtSettingAntiUninstallHint = (TextView) findViewById(R.id.txtSettingAntiUninstallHint);

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_settings;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferencesData.go2Permission()) {
            PreferencesData.setGo2Permission(false);
        } else {
            if (!go2Permission && PreferencesData.isShowAuthentication()) {
                SystemUtil.go2Authen(this);
                return;
            }
        }
        swSettingState.setChecked(PreferencesData.getLockStatus(this));
        setWidgetEnable(PreferencesData.getLockStatus(this));
        txtSettingLockStyle.setText(SettingsUtil.getLockStyleString(SettingsActivity.this));
        txtSettingLanguage.setText(LanguageManager.getLanguageString(SettingsActivity.this));
        txtSettingDelay.setText(SettingsUtil.getDelayString(SettingsActivity.this));
        txtSettingAntiUninstallHint.setText(deviceAdminManager.isActiveAdmin() ? R.string.setting_anti_uninstall_hint_2 : R.string.setting_anti_uninstall_hint);
        swSettingAntiUninstall.setChecked(deviceAdminManager.isActiveAdmin());
        String email = SettingsUtil.getEmail(SettingsActivity.this);
        noEmail = StringUtil.isEmpty(email);
        if (noEmail) {
            txtSettingEmailHint.setTextColor(getResources().getColor(R.color.text_red));
            txtSettingEmailHint.setText(R.string.setting_email_hint);
        } else {
            txtSettingEmailHint.setTextColor(getResources().getColor(R.color.text_black_3));
            txtSettingEmailHint.setText(email);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (deviceAdminManager.isActiveAdmin()) {
            UHAnalytics.changeUHOpen(UserHabitID.LM_28);
        } else {
            UHAnalytics.changeUHClose(UserHabitID.LM_28);
        }
        if (go2Permission) {
            go2Permission = false;
            PreferencesData.setGo2Permission(true);
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_state: {
                swSettingState.setChecked(!swSettingState.isChecked());
                UHAnalytics.changeDataCount(UserHabitID.LM_17);
                break;
            }
            case R.id.viewSettingLockType: {
                UHAnalytics.changeDataCount(UserHabitID.LM_18);
                Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
                intent.putExtra("authenChange", true);
                SystemUtil.startActivityForResult(SettingsActivity.this, intent, 0);

                break;
            }
            case R.id.viewSettingLanguage: {
                LanguageManager.showLanguageSettingDialog(SettingsActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LanguageManager.saveLocale(SettingsActivity.this, which);
                        LanguageManager.changeLang(SettingsActivity.this, LanguageManager.loadLocaleString(SettingsActivity.this));
                        //final Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        Intent intentBroadcast = new Intent();
                        intentBroadcast.setAction(LockerService.MODLE_CHANGE);
                        SettingsActivity.this.sendBroadcast(intentBroadcast);

                        Intent intent = new Intent();
                        intent.setClass(SettingsActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("restart", true);
                        startActivity(intent);
                    }
                });
                break;
            }
            case R.id.viewSettingDelay: {
                UHAnalytics.changeDataCount(UserHabitID.LM_25);
                SettingsUtil.showDelaySettingDialog(SettingsActivity.this, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UHAnalytics.changeSetDataCount(UserHabitID.LM_27, String.valueOf(which));
                        SettingsUtil.setDelay(SettingsActivity.this, which);
                        txtSettingDelay.setText(SettingsUtil.getDelayString(SettingsActivity.this));
                        sendBroadcast(new Intent(LockerService.DELAY_CHANGE));
                        dialog.dismiss();
                    }
                });
                break;
            }
            case R.id.viewSettingEmail: {
                UHAnalytics.changeDataCount(UserHabitID.LM_23);
                Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
                intent.putExtra("editEmail", true);
                SystemUtil.startActivityForResult(SettingsActivity.this, intent, 0);
                break;
            }
            case R.id.txtSettingPassword: {
                UHAnalytics.changeDataCount(UserHabitID.LM_21);
                Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
                intent.putExtra("editPassword", true);
                SystemUtil.startActivity(SettingsActivity.this, intent);
                break;
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.swSettingState: {
                if (isChecked && !PreferencesData.getLockStatus(SettingsActivity.this) && !PreferencesData.isPermissionUsable()) {
                    showPermissionDialog();
                }
                setWidgetEnable(isChecked);
                PreferencesData.setLockStatus(SettingsActivity.this, isChecked);
                break;
            }
            case R.id.swSettingAntiUninstall: {
                if (isChecked)
                    deviceAdminManager.registerDeviceAdmin(SettingsActivity.this);
                else
                    deviceAdminManager.unRegisterDeviceAdmin();
                txtSettingAntiUninstallHint.setText(isChecked ? R.string.setting_anti_uninstall_hint_2 : R.string.setting_anti_uninstall_hint);
                break;
            }
        }
    }

    private void showPermissionDialog() {
        if (permissionDialog == null) {
            BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(SettingsActivity.this);
            builder.setMessage(R.string.tips_main_permission);
            builder.setTitle(R.string.tips_main_permission_title);
            builder.setPositiveButton(R.string.dialog_permit, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //TODO:跳转至设置权限
                    SystemUtil.go2Permission(SettingsActivity.this);
                    go2Permission = true;
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(R.string.dialog_cancel, null);
            permissionDialog = builder.create();
        }
        try {
            permissionDialog.dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }
        permissionDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        resume = false;
        if (resultCode == 1) {//密码验证成功
            SettingsUtil.showLockTypeSettingDialog(SettingsActivity.this, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (SettingsUtil.getLockStyle(SettingsActivity.this) != which) {
                        Intent intent = new Intent(SettingsActivity.this, AuthenticationActivity.class);
                        intent.putExtra("change", true);
                        SystemUtil.startActivityForResult(SettingsActivity.this, intent, 0);
                    }
                }
            });
        } else if (resultCode == 2) {//修改解锁方式完成
            sendBroadcast(new Intent(LockerService.MODLE_CHANGE));
            txtSettingLockStyle.setText(SettingsUtil.getLockStyleString(SettingsActivity.this));
        } else if (resultCode == 3) {
            Intent intent = new Intent();
            if (noEmail) {
                intent.setClass(SettingsActivity.this, EmailAddActivity.class);
                intent.putExtra("fromSetting", true);
            } else {
                intent.setClass(SettingsActivity.this, EmailChangeActivity.class);
            }
            SystemUtil.startActivity(SettingsActivity.this, intent);
        }
    }


    private void setWidgetEnable(boolean enable) {
        viewSettingLockType.setEnabled(enable);
        viewSettingLanguage.setEnabled(enable);
        viewSettingDelay.setEnabled(enable);
        viewSettingEmail.setEnabled(enable);
        txtSettingPassword.setEnabled(enable);
        swSettingAntiUninstall.setEnabled(enable);
    }
}
