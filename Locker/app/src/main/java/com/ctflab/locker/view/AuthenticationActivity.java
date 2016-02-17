package com.ctflab.locker.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.ctflab.locker.FrameActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SystemUtil;

/**
 * Created by hejw on 2016/1/8.
 */
public class AuthenticationActivity extends FrameActivity {
    private NumberAuthenticationFragment numberFragment;
    private PatternAuthenticationFragment patternFragment;
    private int mModle;//当前模式0:设置密码，1：验证密码2：修改密码 3:修改解锁方式-验证密码 4:修改解锁方式 5:重启密码验证 6:邮箱添加/修改密码验证
//    private boolean resume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        Intent intent = getIntent();
        if (intent.getBooleanExtra("editEmail",false)){
            mModle = 6;
        }else if (intent.getBooleanExtra("resumeAuthen",false)){
            mModle = 5;
        }else if (intent.getBooleanExtra("change",false)){
            mModle = 4;
        }else if (intent.getBooleanExtra("authenChange",false)){
            mModle = 3;
        }else if (intent.getBooleanExtra("editPassword", false)){
            mModle = 2;
        }else if (TextUtils.isEmpty(PreferencesData.getPassword(this))){
            mModle = 0;
        }else {
            mModle = 1;
        }
//        resume = false;
    }

    @Override
    protected int getContentView() {
        return R.layout.authentication_layout;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(PreferencesData.getPassword(this)))
            mModle = 0;
        else if (mModle>1&&PreferencesData.isShowAuthentication()){
            mModle = 5;
        }
        switchFragment(PreferencesData.getPasswordModle(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
//        resume = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void switchFragment(int page){
        if (mModle == 4){//修改解锁方式，设置互相调换
            if (page == 0)
                page = 1;
            else
                page = 0;
        }
        if (page == 0){
//            if (numberFragment == null)
                numberFragment = new NumberAuthenticationFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.rootView,numberFragment).commitAllowingStateLoss();
        }else if (page == 1){
//            if (patternFragment == null)
                patternFragment = new PatternAuthenticationFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.rootView,patternFragment).commitAllowingStateLoss();
        }
    }

    public int getModle(){
        return mModle;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mModle == 3 || mModle == 4){
//            setResult(0);
        }else if (mModle == 5){
            SystemUtil.showHome(this);
        }
    }
}
