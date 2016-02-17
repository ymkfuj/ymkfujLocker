package com.ctflab.locker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.common.AppInfoEntity;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.model.UpdateManager;
import com.ctflab.locker.utils.AppListUtil;
import com.ctflab.locker.utils.LockDB;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.SystemUtil;
import com.ctflab.locker.view.AboutActivity;
import com.ctflab.locker.view.SettingsActivity;
import com.ctflab.locker.widget.BottomAlertDialogBuilder;
import com.ctflab.locker.widget.ResideMenu.ResideMenu;
import com.ctflab.locker.widget.ResideMenu.ResideMenuItem;

import java.util.ArrayList;

public class MainActivity extends FrameActivity implements View.OnClickListener {
    private static final int MSG_LOAD = 1;
    private static final int MSG_SHOW_LOADING = 2;
    private static final int MSG_HIDE_LOADING = 3;
    private static final int MSG_SHOW_LIST = 4;
    private static final int MSG_SHOW_ERROR = 5;
    private static final int MSG_REFRESH = 6;
    private static final int MSG_SHOW_SERVICES_TIP = 7;
    private static final int MSG_CLOSE_SERVICES_TIP = 8;
    private static final int MSG_SHOW_PERMISSION_TIP = 9;
    private static final int MSG_HIDE_PERMISSION_TIP = 10;
    private static final int MSG_SHOW_PERMISSION_DIALOG = 11;
    private static final int MSG_SHOW_NEWVERSION = 12;

    private static long UPDATE_INTERVAL = 86400000;

    private ResideMenu resideMenu;


    private ResideMenuItem itemLock;
    private ResideMenuItem itemSetting;
    private ResideMenuItem itemFeedback;
    private ResideMenuItem itemScore;
    private ResideMenuItem itemAbout;


    private TextView txtPermissionTip, txtServicesTip;
    private Button btnReload;
    private View layoutApp, layoutError, layoutLoading;
    private ListView lstApps;
    private AppAdapter appAdapter;
    private boolean functionEnable;
    private AlertDialog permissionDialog;
    private boolean go2Permission;

    private boolean notShowPermission;//权限请求取消时暂时不显示
    private boolean showPermissionTip = false;

    private ArrayList<AppInfoEntity> appInfoEntities = new ArrayList<>();

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD: {
                    loadAppList();
                    break;
                }
                case MSG_SHOW_LOADING: {
                    showLoading();
                    break;
                }
                case MSG_HIDE_LOADING: {
                    hideLoading();
                    break;
                }
                case MSG_SHOW_LIST: {
                    hideLoading();
                    appInfoEntities = AppListUtil.appInfoEntities;
                    appAdapter.notifyDataSetChanged();
                    showAppList();
                    UHAnalytics.changeAccumulateNumDataCount(UserHabitID.LM_9, appInfoEntities.size());
                    break;
                }
                case MSG_SHOW_ERROR: {
                    hideLoading();
                    showError();
                    break;
                }
                case MSG_REFRESH: {
                    int position = msg.arg1;
                    AppListUtil.appInfoEntities.get(position).isLoacked = !AppListUtil.appInfoEntities.get(position).isLoacked;
                    LockDB lockDB = new LockDB(MainActivity.this);
                    lockDB.refreshApp(AppListUtil.appInfoEntities.get(position).packgeName, AppListUtil.appInfoEntities.get(position).isLoacked);
//                    Collections.sort(AppListUtil.appInfoEntities);
                    appInfoEntities = AppListUtil.appInfoEntities;
                    appAdapter.notifyDataSetChanged();
                    break;
                }

                case MSG_SHOW_PERMISSION_DIALOG: {
                    if (permissionDialog == null) {
                        BottomAlertDialogBuilder builder = new BottomAlertDialogBuilder(MainActivity.this);
                        builder.setMessage(R.string.tips_main_permission);
                        builder.setTitle(R.string.tips_main_permission_title);
                        builder.setPositiveButton(R.string.dialog_permit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //TODO:跳转至设置权限
                                UHAnalytics.changeDataCount(UserHabitID.LM_12);
                                SystemUtil.go2Permission(MainActivity.this);
                                go2Permission = true;
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                UHAnalytics.changeDataCount(UserHabitID.LM_13);
                                mHandler.sendEmptyMessage(MSG_SHOW_PERMISSION_TIP);
                            }
                        });
                        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                mHandler.sendEmptyMessage(MSG_SHOW_PERMISSION_TIP);
                            }
                        });
                        permissionDialog = builder.create();
                    }
                    try {
                        permissionDialog.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    permissionDialog.show();
                    break;
                }
                case MSG_SHOW_SERVICES_TIP: {
                    txtServicesTip.setVisibility(View.VISIBLE);
//                    findViewById(R.id.viewMask).setVisibility(View.VISIBLE);
                    break;
                }
                case MSG_CLOSE_SERVICES_TIP: {
                    txtServicesTip.setVisibility(View.GONE);
//                    findViewById(R.id.viewMask).setVisibility(View.GONE);
                    break;
                }
                case MSG_SHOW_PERMISSION_TIP:
                    UHAnalytics.changeDataCount(UserHabitID.LM_14);
                    txtPermissionTip.setVisibility(View.VISIBLE);
                    break;
                case MSG_HIDE_PERMISSION_TIP:
                    txtPermissionTip.setVisibility(View.GONE);
                    break;
                case MSG_SHOW_NEWVERSION: {
                    if (!mIsDestroyed) {
                        try {
                            int index = ((String) msg.obj).indexOf("|");
                            UpdateManager.showNewVersionDialogInMain(MainActivity.this, ((String) msg.obj).substring(0, index), ((String) msg.obj).substring(index + 1));
                        } catch (Throwable e) {

                        }
                    }
                    break;
                }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpMenu();
        layoutApp = findViewById(R.id.layoutApp);
        layoutError = findViewById(R.id.layoutError);
        layoutLoading = findViewById(R.id.layoutAppLoading);

        txtPermissionTip = (TextView) findViewById(R.id.txtTipsPermission);
        txtPermissionTip.setOnClickListener(this);
        txtServicesTip = (TextView) findViewById(R.id.txtTipsServices);
        txtServicesTip.setOnClickListener(this);

        btnReload = (Button) findViewById(R.id.btnReload);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.sendEmptyMessage(MSG_LOAD);
            }
        });

        lstApps = (ListView) findViewById(R.id.lstApps);
        appAdapter = new AppAdapter(this);
        lstApps.setAdapter(appAdapter);
        lstApps.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (functionEnable) {
                    Message msg = mHandler.obtainMessage(MSG_REFRESH);
                    msg.arg1 = position;
                    mHandler.sendMessage(msg);
                }
            }
        });
        mHandler.sendEmptyMessage(MSG_LOAD);

        SettingsUtil.showRateDialog(this);
        functionEnable = PreferencesData.getLockStatus(this) && SystemUtil.isUsageOpen(this);

        UpdateManager.checkUpdateSilent(this, new UpdateManager.ICheckUpdateCallback() {
            @Override
            public void onNewVersionFound(String ver, String desc) {
                mHandler.sendMessage(Message.obtain(mHandler, MSG_SHOW_NEWVERSION, ver + "|" + desc));
            }
        });
//        resume = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (go2Permission) {
            go2Permission = false;
            PreferencesData.setGo2Permission(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        resume = true;
    }

    @Override
    protected void onResume() {
        if (PreferencesData.go2Permission()) {
            PreferencesData.setGo2Permission(false);
            showPermissionTip = true;
        }else {
            if (!go2Permission&&PreferencesData.isShowAuthentication()) {
                SystemUtil.go2Authen(this);
                super.onResume();
                return;
            }
        }

        if (functionEnable != (PreferencesData.getLockStatus(this) && PreferencesData.isPermissionUsable())) {//开关变化，刷新列表开关显示效果
            appAdapter.notifyDataSetInvalidated();
            functionEnable = !functionEnable;
        }
        if (!PreferencesData.getLockStatus(this)) {//开关关闭
            mHandler.sendEmptyMessage(MSG_SHOW_SERVICES_TIP);
            mHandler.sendEmptyMessage(MSG_HIDE_PERMISSION_TIP);
        } else {
            if (!PreferencesData.isPermissionUsable()) {//开关打开，权限未开启
                if (showPermissionTip){
                    mHandler.sendEmptyMessage(MSG_SHOW_PERMISSION_TIP);
                    showPermissionTip = false;
                }else if (!notShowPermission) {
                    mHandler.sendEmptyMessage(MSG_SHOW_PERMISSION_DIALOG);
                    mHandler.sendEmptyMessage(MSG_HIDE_PERMISSION_TIP);
                } else
                    notShowPermission = false;
            } else {
                mHandler.sendEmptyMessage(MSG_HIDE_PERMISSION_TIP);
            }
            mHandler.sendEmptyMessage(MSG_CLOSE_SERVICES_TIP);
        }
        super.onResume();
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initToolBar() {
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_titlebar_menu);
        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        notShowPermission = true;
//        resume = false;
    }


    private boolean mIsDestroyed = false;

    @Override
    protected void onDestroy() {
        mIsDestroyed = true;
        super.onDestroy();
    }

    private void loadAppList() {
        mHandler.sendEmptyMessage(MSG_SHOW_LOADING);
        UHAnalytics.changeDataCount(UserHabitID.LM_6);
        UHAnalytics.startResident(UserHabitID.LM_8);
        AppListUtil.getInstalledAppList(this, new AppListUtil.IAppLoadListener() {
            @Override
            public void onLoadSuccessed() {
                UHAnalytics.stopResident(UserHabitID.LM_8);
                UHAnalytics.changeDataCount(UserHabitID.LM_7);
                mHandler.sendEmptyMessage(MSG_SHOW_LIST);
            }

            @Override
            public void onLoadFailed() {
                mHandler.sendEmptyMessage(MSG_SHOW_ERROR);
            }

            @Override
            public void onLoading() {
                mHandler.sendEmptyMessage(MSG_SHOW_LOADING);
            }
        });
    }


    private void setUpMenu() {

        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(false);
//        resideMenu.setBackground(R.drawable.menu_background);
        resideMenu.setBackgroundColor(Color.rgb(0x00, 0xb9, 0xff));
        resideMenu.attachToActivity(this);
        resideMenu.setScaleValue(0.6f);
        resideMenu.setFitsSystemWindows(true);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

        // create menu items;
        itemLock = new ResideMenuItem(this, R.drawable.icon_lockmaster, R.string.menu_lock);
        itemSetting = new ResideMenuItem(this, R.drawable.icon_settings, R.string.menu_setting);
        itemFeedback = new ResideMenuItem(this, R.drawable.icon_feedback, R.string.menu_feedback);
        itemScore = new ResideMenuItem(this, R.drawable.icon_score, R.string.menu_score);
        itemAbout = new ResideMenuItem(this, R.drawable.icon_about, R.string.menu_about);


        itemLock.setOnClickListener(this);
        itemSetting.setOnClickListener(this);
        itemFeedback.setOnClickListener(this);
        itemScore.setOnClickListener(this);
        itemAbout.setOnClickListener(this);

        resideMenu.addMenuItem(itemLock, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemSetting, ResideMenu.DIRECTION_LEFT);
//        resideMenu.addMenuItem(itemFeedback, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemScore, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(itemAbout, ResideMenu.DIRECTION_LEFT);

        // You can disable a direction by setting ->
        // resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackClicked() {
        resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
    }

    @Override
    public void onClick(View view) {
        if (view == itemSetting || view == txtServicesTip) {
            Intent intent = new Intent();
            intent.putExtra("isFormTip", view == txtServicesTip);
            intent.setClass(MainActivity.this, SettingsActivity.class);
            SystemUtil.startActivity(this, intent);
        } else if (view == itemAbout) {
            SystemUtil.startActivityForResult(this, new Intent(this, AboutActivity.class), 0);
        } else if (view == itemFeedback) {
            SettingsUtil.sendEmail(this);
        } else if (view == itemScore) {
            SettingsUtil.go2GooglePlay(this);
        } else if (view == itemLock) {
            resideMenu.closeMenu();
//            SettingsUtil.showRateDialog(this);
        } else if (view == txtPermissionTip) {
            UHAnalytics.changeDataCount(UserHabitID.LM_15);
            mHandler.sendEmptyMessage(MSG_SHOW_PERMISSION_DIALOG);
        }
    }

    // What good method is to access resideMenu？
    public ResideMenu getResideMenu() {
        return resideMenu;
    }


    private void showLoading() {
//        Toast.makeText(MainActivity.this, "isLoading...", Toast.LENGTH_SHORT).show();
        layoutLoading.setVisibility(View.VISIBLE);
        layoutApp.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
    }

    private void hideLoading() {
        layoutLoading.setVisibility(View.GONE);
    }

    private void showAppList() {
        findViewById(R.id.layoutApp).setVisibility(View.VISIBLE);
        findViewById(R.id.layoutError).setVisibility(View.GONE);
    }

    private void showError() {
        findViewById(R.id.layoutApp).setVisibility(View.GONE);
        findViewById(R.id.layoutError).setVisibility(View.VISIBLE);
    }

    class AppAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public AppAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return appInfoEntities.size();
        }

        @Override
        public Object getItem(int position) {
            return appInfoEntities.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AppInfoEntity appInfoEntity = (AppInfoEntity) getItem(position);


            ViewHolder holder;
            if (convertView == null) {

                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.listitem_app, parent, false);
                holder.imgAppIcon = (ImageView) convertView.findViewById(R.id.imgItemAppIcon);
                holder.txtAppName = (TextView) convertView.findViewById(R.id.txtItemAppName);
                holder.chbAppLock = (CheckBox) convertView.findViewById(R.id.chbItemAppLock);

                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }

            holder.chbAppLock.setEnabled(functionEnable);
            holder.chbAppLock.setChecked(appInfoEntity.isLoacked);
            holder.imgAppIcon.setImageDrawable(appInfoEntity.icon);
            holder.txtAppName.setText(appInfoEntity.lable);

            return convertView;


        }

        class ViewHolder {
            public ImageView imgAppIcon;
            public TextView txtAppName;
            public CheckBox chbAppLock;
        }
    }
}
