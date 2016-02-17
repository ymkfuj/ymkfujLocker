package com.ctflab.locker.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anguanjia.framework.components.AnguanjiaService;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.utils.MyLog;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.services.pulse.PulseManager;
import com.ctflab.locker.utils.AppListUtil;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.SystemUtil;
import com.ctflab.locker.view.ForgetPasswordActivity;
import com.ctflab.locker.view.LockPatternUtil;
import com.ctflab.locker.view.LockPatternView;
import com.ctflab.locker.view.NumberInputView;
import com.ctflab.locker.widget.SwitchButton.CheckSwitchButton;

import java.util.List;


/**
 * Created by hejw on 2016/1/5.
 */
public class LockerService extends AnguanjiaService{

    public static final String MODLE_CHANGE = "com.ctflab.locker.modleChange";
    public static final String DELAY_CHANGE = "com.ctflab.locker.delayChange";

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://monitor
                    doMonitor();
                    sendEmptyMessageDelayed(0,500);
                    break;
                case 1://number错误
                    inputView.clearnAllView();
                    break;
                case 2://pattern错误
                    patternView.clearPattern();
                    break;
                case 3:
                    patternView.setEnabled(true);
                    patternView.clearPattern();
                    removeAuthenticationView();
                    break;
                case 4:
                    removePermissionView();
                    break;
            }
        }
    };
    private String mCurrentPackage,mLastPackage;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View lockView,permissionView,permissionPop;
    private ImageView appIcon;
    private TextView appName,noti;
    private LockPatternView patternView;
    private NumberInputView inputView;
//    private String thirdApp;

    private boolean mLockShow;

    private HomeClickReceiver mHomeClickReceiver;
    private ModleChangeReceiver mModleChangeReceiver;
    private DelayChangeReceiver mDelayChangeReceiver;
    private ScreenReceiver mScreenReceiver;

    private long delayTime;//延时锁定时间
    private boolean screenOff;
    private boolean screenOffLock;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PulseManager.startPulseCounter(this);
        MyLog.d("hejw", "service oncreate");
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mHomeClickReceiver = new HomeClickReceiver();
        IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        try{
            registerReceiver(mHomeClickReceiver, homeFilter);
        }catch(Exception e){
            e.printStackTrace();
        }
        mModleChangeReceiver = new ModleChangeReceiver();
        IntentFilter modleChangeFilter = new IntentFilter(MODLE_CHANGE);
        try {
            registerReceiver(mModleChangeReceiver,modleChangeFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDelayChangeReceiver = new DelayChangeReceiver();
        IntentFilter delayFilter = new IntentFilter(DELAY_CHANGE);
        try {
            registerReceiver(mDelayChangeReceiver,delayFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mScreenReceiver = new ScreenReceiver();
        IntentFilter screenFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        screenFilter.addAction(Intent.ACTION_USER_PRESENT);
        try {
            registerReceiver(mScreenReceiver, screenFilter);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessage(0);
        initDelayTime();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLog.d("hejw", "service onStartCommand");
        if (intent != null && intent.getBooleanExtra("permission",false)){
            showPermission();
        }if (intent != null && intent.getBooleanExtra("permissionPop",false)){
            showPermissionPop();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean stopService(Intent name) {
        if (mHomeClickReceiver != null){
            try {
                unregisterReceiver(mHomeClickReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mModleChangeReceiver != null){
            try {
                unregisterReceiver(mModleChangeReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mDelayChangeReceiver != null){
            try {
                unregisterReceiver(mDelayChangeReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (mScreenReceiver != null){
            try {
                unregisterReceiver(mScreenReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.stopService(name);
    }

    private void initDelayTime(){
        switch (SettingsUtil.getDelay(this)){
            case 0:
                delayTime = 0;
                break;
            case 1:
                delayTime = 15000;
                break;
            case 2:
                delayTime = 30000;
                break;
            case 3:
                delayTime = 60000;
                break;
            case 4:
                delayTime = 60000*5;
                break;
            case 5:
                delayTime = 60000*10;
                break;
            case 6:
                delayTime = -1;
                break;
        }
    }

    //轮询机制检查当前app
    private void doMonitor(){
        if (SystemUtil.isUsageOpen(this)){
            if (System.currentTimeMillis() > PreferencesData.getPermissionCheckTime(this)){
                PreferencesData.setPermissionCheckTime(this);
                UHAnalytics.changeDataCount(UserHabitID.LM_16);
            }
            if (!PreferencesData.isPermissionUsable()){
                PreferencesData.setPermissionUsable(true);
            }
            mCurrentPackage = SystemUtil.getTopPackgeName(this);
            if (!mCurrentPackage.equals(getPackageName())){//切到后台
                if (!PreferencesData.go2Permission()){
                    PreferencesData.setShowAuthentication(true);
                }
            }else {
                if (!screenOff && PreferencesData.isShowAuthentication())
                    PreferencesData.setShowAuthentication(false);
            }
        }else {
            if (SystemUtil.isSelfBackground(this)){//切到后台
                if (!PreferencesData.go2Permission()){
                    PreferencesData.setShowAuthentication(true);
                }
            }else {
                if (!screenOff && PreferencesData.isShowAuthentication())
                    PreferencesData.setShowAuthentication(false);
            }
            if (PreferencesData.isPermissionUsable()){
                PreferencesData.setPermissionUsable(false);
                showPermissionPop();
            }
            mLastPackage = "";
        }

        if (PreferencesData.getLockStatus(this)){//锁定状态已开启
            if ((delayTime == -1 && screenOffLock)||(delayTime > -1 && (System.currentTimeMillis()-PreferencesData.getLastUnlockTime(this)>delayTime))){
                if (!TextUtils.isEmpty(mLastPackage)&&!mCurrentPackage.equals(mLastPackage)||"lock".equals(mLastPackage)){
                    if (AppListUtil.getLockedPackageList(this).contains(mCurrentPackage)){
                        if (screenOffLock)
                            screenOffLock = false;
                        if (PreferencesData.go2Permission()&&mCurrentPackage.equals("com.android.settings")){
                            //防卸载不要验证（权限申请因权限未开无需考虑）
                        }else {
                            try {
                                ApplicationInfo applicationInfo = getPackageManager().getApplicationInfo(mCurrentPackage,0);
                                showAuthenticationView(applicationInfo.loadLabel(getPackageManager()).toString(),applicationInfo.loadIcon(getPackageManager()));
                            } catch (PackageManager.NameNotFoundException e) {
                                e.printStackTrace();
                            }
                        }

                    }
//                    else {
//                        removeAuthenticationView();
//                    }
                }
                mLastPackage = mCurrentPackage;
            }
        }
    }

    //开启权限引导弹窗
    private void showPermission(){
        initLayoutParam();
        mLayoutParams.alpha = 0.99f;
        permissionView = LinearLayout.inflate(this,R.layout.view_permission,null);
        permissionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeMessages(4);
                removePermissionView();
            }
        });
        ((CheckSwitchButton)permissionView.findViewById(R.id.csPermission)).setChecked(true);
        SystemUtil.addWindowView(mWindowManager, permissionView, mLayoutParams);

        mHandler.sendEmptyMessageDelayed(4, 6000);
    }

    private void removePermissionView(){
        if (mWindowManager != null && permissionView != null){
            try {
                mWindowManager.removeView(permissionView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            permissionView = null;
        }
    }

    private void showPermissionPop(){
        initLayoutParam();
        mLayoutParams.alpha = 0.99f;
        if (permissionPop == null){
            permissionPop = RelativeLayout.inflate(this,R.layout.permission_pop_view,null);
            Button ok = (Button) permissionPop.findViewById(R.id.permissionOk);
            ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePermissionPop();
                }
            });
            permissionPop.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removePermissionPop();
                }
            });
        }
        SystemUtil.addWindowView(mWindowManager, permissionPop, mLayoutParams);
    }

    private void removePermissionPop(){
        if (mWindowManager != null && permissionPop != null){
            try {
                mWindowManager.removeView(permissionPop);
            } catch (Exception e) {
                e.printStackTrace();
            }
            permissionPop = null;
        }
    }

    private void initLayoutParam(){
        if (mLayoutParams == null){
            mLayoutParams = new WindowManager.LayoutParams();
            mLayoutParams.flags = WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN|WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            mLayoutParams.format = PixelFormat.RGBA_8888;
            mLayoutParams.screenOrientation = WindowManager.LayoutParams.SCREEN_ORIENTATION_CHANGED;
        }
    }

    private void showAuthenticationView(String packageName,Drawable packageIcon){
        screenOff = false;
        if (!mLockShow){
            if (lockView == null){
                initLayoutParam();
                mLayoutParams.alpha = 1.0f;
                lockView = LinearLayout.inflate(this, R.layout.lock_view_layout,null);
                appIcon = (ImageView) lockView.findViewById(R.id.app_icon);
                appName = (TextView) lockView.findViewById(R.id.app_name);
                noti = (TextView) lockView.findViewById(R.id.noti);
                RelativeLayout forget = (RelativeLayout) lockView.findViewById(R.id.forget_password);
                forget.setBackgroundResource(R.drawable.selector_common);
                forget.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO:忘记密码流程
                        PreferencesData.setShowAuthentication(false);
                        SystemUtil.showHome(LockerService.this);
                        removeAuthenticationView();
                        Intent intent = new Intent(LockerService.this, ForgetPasswordActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        SystemUtil.startActivity(LockerService.this, intent);
                    }
                });
                ViewStub stub;
                if (PreferencesData.getPasswordModle(LockerService.this) == 0){
                    stub = (ViewStub) lockView.findViewById(R.id.stub_number);
                    View number = stub.inflate();
                    inputView = (NumberInputView) number.findViewById(R.id.numberInput);
                    final ImageView number0,number1,number2,number3,number4,number5,number6,number7,number8,number9,numberBack;

                    number0 = (ImageView) lockView.findViewById(R.id.number0);
                    number1 = (ImageView) lockView.findViewById(R.id.number1);
                    number2 = (ImageView) lockView.findViewById(R.id.number2);
                    number3 = (ImageView) lockView.findViewById(R.id.number3);
                    number4 = (ImageView) lockView.findViewById(R.id.number4);
                    number5 = (ImageView) lockView.findViewById(R.id.number5);
                    number6 = (ImageView) lockView.findViewById(R.id.number6);
                    number7 = (ImageView) lockView.findViewById(R.id.number7);
                    number8 = (ImageView) lockView.findViewById(R.id.number8);
                    number9 = (ImageView) lockView.findViewById(R.id.number9);
                    numberBack = (ImageView) lockView.findViewById(R.id.numberBack);
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String tag = (String) v.getTag();
                            if (!TextUtils.isEmpty(tag)){//数字
                                inputView.setPassword(tag);
                            }else {//退格
                                inputView.backOne();
                            }
                        }
                    };
                    GridLayout.LayoutParams lm = (GridLayout.LayoutParams) number4.getLayoutParams();
                    lm.setMargins(lm.leftMargin,inputView.getHeightMargin(lm.topMargin),lm.rightMargin,inputView.getHeightMargin(lm.bottomMargin));
                    number4.setLayoutParams(lm);

                    lm = (GridLayout.LayoutParams) number5.getLayoutParams();
                    lm.setMargins(lm.leftMargin, inputView.getHeightMargin(lm.topMargin), lm.rightMargin, inputView.getHeightMargin(lm.bottomMargin));
                    number5.setLayoutParams(lm);

                    lm = (GridLayout.LayoutParams) number6.getLayoutParams();
                    lm.setMargins(lm.leftMargin,inputView.getHeightMargin(lm.topMargin),lm.rightMargin,inputView.getHeightMargin(lm.bottomMargin));
                    number6.setLayoutParams(lm);

                    lm = (GridLayout.LayoutParams) number0.getLayoutParams();
                    lm.setMargins(lm.leftMargin,inputView.getHeightMargin(lm.topMargin),lm.rightMargin,lm.bottomMargin);
                    number0.setLayoutParams(lm);

                    lm = (GridLayout.LayoutParams) numberBack.getLayoutParams();
                    lm.setMargins(lm.leftMargin,inputView.getHeightMargin(lm.topMargin),lm.rightMargin,lm.bottomMargin);
                    numberBack.setLayoutParams(lm);


                    number0.setOnClickListener(listener);
                    number1.setOnClickListener(listener);
                    number2.setOnClickListener(listener);
                    number3.setOnClickListener(listener);
                    number4.setOnClickListener(listener);
                    number5.setOnClickListener(listener);
                    number6.setOnClickListener(listener);
                    number7.setOnClickListener(listener);
                    number8.setOnClickListener(listener);
                    number9.setOnClickListener(listener);
                    numberBack.setOnClickListener(listener);
                    NumberInputView.InputListener inputListener = new NumberInputView.InputListener() {
                        @Override
                        public void inputError() {
                            noti.setText(R.string.input_number_wrong);
                        }

                        @Override
                        public void inputEnsure() {

                        }

                        @Override
                        public void inputSuccess() {
                            SettingsUtil.setUnlockTime(LockerService.this,System.currentTimeMillis());
                            removeAuthenticationView();
                            inputView.clearnAllView();
                        }

                        @Override
                        public void inputDiff() {

                        }

                        @Override
                        public void refreshTitle(boolean edit) {

                        }

                        @Override
                        public void clearEnable(boolean enable) {
                            numberBack.setEnabled(enable);
                        }
                    };
                    inputView.setListener(inputListener);
                    inputView.setModle(1);

                    number0.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number1.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number2.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number3.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number4.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number5.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number6.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number7.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number8.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    number9.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));
                    numberBack.setImageDrawable(getResources().getDrawable(R.drawable.input_button_selector));

                    numberBack.setEnabled(false);

                }else /*(PreferencesData.getPasswordModle(LockerService.this) == 1)*/{

                    stub = (ViewStub) lockView.findViewById(R.id.stub_pattern);
                    View pattern = stub.inflate();
                    patternView = (LockPatternView) pattern.findViewById(R.id.pattern);
                    LockPatternView.OnPatternListener listener = new LockPatternView.OnPatternListener() {
                        @Override
                        public void onPatternStart() {
                            mHandler.removeMessages(2);
                        }

                        @Override
                        public void onPatternCleared() {

                        }

                        @Override
                        public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {

                        }

                        @Override
                        public void onPatternDetected(List<LockPatternView.Cell> pattern) {
                            String password = PreferencesData.getPassword(LockerService.this);
                            if (password.equals(LockPatternUtil.patternToString(patternView.getPattern()))){
                                //TODO:密码验证正确
                                SettingsUtil.setUnlockTime(LockerService.this,System.currentTimeMillis());
                                patternView.setEnabled(false);
                                mHandler.sendEmptyMessageDelayed(3,500);
                            }else {
                                noti.setText(R.string.draw_pattern_wrong);
                                patternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                                mHandler.sendEmptyMessageDelayed(2,1000);
                            }
                        }
                    };
                    patternView.setOnPatternListener(listener);
                }

            }
            lockView.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        SystemUtil.showHome(LockerService.this);
                        removeAuthenticationView();
                        return true;
                    }
                    return false;
                }
            });
            if (PreferencesData.getPasswordModle(LockerService.this) == 0) {
                noti.setText(R.string.set_number_password);
                inputView.clearnAllView();
            }else {
                noti.setText(R.string.draw_pattern);
                patternView.clearPattern();
            }
            appIcon.setImageDrawable(packageIcon);
            appName.setText(packageName);
            SystemUtil.addWindowView(mWindowManager, lockView, mLayoutParams);
            mLockShow = true;
        }
    }

    //移除程序锁界面
    private void removeAuthenticationView(){
        if (mLockShow){
            if (mWindowManager != null && lockView !=null){
                try {
                    mWindowManager.removeView(lockView);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mLockShow = false;
        }

    }

    //桌面按钮监听
    private class HomeClickReceiver extends BroadcastReceiver {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
//        private static final String SYSTEM_DIALOG_REASON_RECENTAPPS_KEY = "recentapps";
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    PreferencesData.setShowAuthentication(true);
                    PreferencesData.setGo2Permission(false);
                    if (!SystemUtil.isCurrentSelf(context)&&PreferencesData.isPermissionUsable()){
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                removeAuthenticationView();
                                SystemUtil.showHome(context);
                            }
                        }, 200);
                    }

                }
//                else if(SYSTEM_DIALOG_REASON_RECENTAPPS_KEY.equals(reason)){
//                    MyLog.d("hejw","SYSTEM_DIALOG_REASON_RECENTAPPS_KEY");
//                    removeAuthenticationView();
//                }
            }
        }
    }

    //解锁方式更改监听
    private class ModleChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            lockView = null;
        }
    }

    //延时时间更改
    private class DelayChangeReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            initDelayTime();
        }
    }

    //锁屏广播
    private class ScreenReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())){
                screenOff = true;
                screenOffLock = true;
                PreferencesData.setShowAuthentication(true);
                PreferencesData.setGo2Permission(false);//锁屏后，授权流程视为中断
                stopMonitor();
            } else {
                startMonitor();
            }

        }
    }

    private void stopMonitor(){
        mHandler.removeMessages(0);
        removeAuthenticationView();
        mLastPackage = "lock";
    }

    private void startMonitor(){
        try {
            mHandler.removeMessages(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHandler.sendEmptyMessageDelayed(0,500);
    }

    public static class RebootReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            SystemUtil.startServiceSafeMode(context, new Intent(context, LockerService.class));
        }
    }

}
