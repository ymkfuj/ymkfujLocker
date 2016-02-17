package com.ctflab.locker.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.utils.SSLUtilExt;
import com.anguanjia.framework.utils.SslException;
import com.ctflab.locker.MainActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SystemUtil;

import java.util.List;

/**
 * Created by hejw on 2016/1/7.
 * 图形解锁界面Activity
 */
public class PatternAuthenticationFragment extends Fragment implements LockPatternView.OnPatternListener{

    private LockPatternView patternView;
    private int mModle;//当前模式0:设置密码，1：验证密码2：修改密码 3:修改解锁方式-验证密码 4:修改解锁方式 5:重启密码验证
    private RelativeLayout switchView;
    private LinearLayout switchButton;
    private TextView patternTitle,switchIcon;
    private String pswTemp;
    private int step;//修改密码进度0验证密码，1修改，2确认

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://密码二次确认
                    patternView.setEnabled(true);
                    updatePatternTitle(3);
                    patternView.clearPattern();
                    break;
                case 1://设置密码-密码输入不一致
                    patternView.setEnabled(true);
                    patternView.clearPattern();
                    updatePatternTitle(0);
                    break;
                case 2:
                    patternView.clearPattern();
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mModle = ((AuthenticationActivity)getActivity()).getModle();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pattern_layout, container,false);
        initView(view);
        return view;
    }

    private void initView(View view){
        patternView = (LockPatternView) view.findViewById(R.id.pattern);
        patternView.setOnPatternListener(this);
        switchView = (RelativeLayout) view.findViewById(R.id.switchView);
        switchIcon = (TextView) view.findViewById(R.id.switch_icon);
        switchButton = (LinearLayout) view.findViewById(R.id.switch_number);
        switchButton.setBackgroundResource(R.drawable.selector_common);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mModle == 0){
                    pswTemp = null;
                    AuthenticationActivity activity = (AuthenticationActivity) getActivity();
                    activity.switchFragment(0);
                }else {
                    SystemUtil.startActivity(getActivity(),new Intent(getActivity(),ForgetPasswordActivity.class));
                    getActivity().finish();
                }
            }
        });
        TextView hint = (TextView) view.findViewById(R.id.textHint);

        patternTitle = (TextView) view.findViewById(R.id.patternHint);
        switch (mModle){
            case 0:
                updatePatternTitle(0);
                switchView.setVisibility(View.VISIBLE);
                switchIcon.setVisibility(View.VISIBLE);
                hint.setTextColor(getResources().getColor(R.color.switch_psw_button));
                break;
            case 1:
            case 5:
                updatePatternTitle(5);
                switchView.setVisibility(View.VISIBLE);
                hint.setText(R.string.forget_password);
                hint.setCompoundDrawables(null,null,null,null);
                hint.setTextColor(getResources().getColor(R.color.text_blue));
                break;
            default:
                if (mModle == 2){
                    updatePatternTitle(7);
                }else
                    updatePatternTitle(5);
                switchView.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    public void onPatternStart() {
        updatePatternTitle(1);
    }

    @Override
    public void onPatternCleared() {

    }

    @Override
    public void onPatternCellAdded(List<LockPatternView.Cell> pattern) {
        mHandler.removeMessages(2);
    }

    @Override
    public void onPatternDetected(List<LockPatternView.Cell> pattern) {
        switch (mModle) {
            case 0:
            case 4:{
                if (TextUtils.isEmpty(pswTemp)) {//首次绘制
                    if (pattern.size() < 4) {
                        updatePatternTitle(2);
                        patternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                        patternView.setEnabled(false);
                        mHandler.sendEmptyMessageDelayed(1, 500);
                    } else {
                        patternView.setEnabled(false);
                        pswTemp = LockPatternUtil.patternToString(pattern);
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    }
                } else {//二次确认
                    if (LockPatternUtil.patternToString(pattern).equals(pswTemp)) {
//                        updatePatternTitle(-1);
                        try {
                            pswTemp = SSLUtilExt.getInstance().data_to_encrypt(pswTemp);
                        } catch (SslException e) {
                            e.printStackTrace();
                            //TODO:异常咋办？
                        }
                        PreferencesData.setPassword(getContext(), pswTemp);
                        PreferencesData.setPasswordModle(getContext(), 1);
                        //finish
                        if (mModle == 0) {
                            PreferencesData.setLockStatus(getContext(),true);
                            SystemUtil.startActivity(getActivity(), new Intent(getActivity(), EmailAddActivity.class));
                        }
                        else {
                            getActivity().setResult(2);
                        }
                        getActivity().finish();
                    } else {//不一致
                        pswTemp = null;
                        updatePatternTitle(4);
                        patternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                        patternView.setEnabled(false);
                        mHandler.sendEmptyMessageDelayed(1, 500);
                    }
                }
                break;
            }

            case 1:
            case 3:
            case 5:
            case 6:{//密码验证
                String password = PreferencesData.getPassword(getContext());
                if (mModle == 1 ||mModle == 3 || mModle == 5||mModle == 6){
                    PreferencesData.setShowAuthentication(false);
                }
                if (password.equals(LockPatternUtil.patternToString(patternView.getPattern()))) {
                    //TODO:密码验证正确
//                    updatePatternTitle(-1);
                    if (mModle == 1)
                        SystemUtil.startActivity(getActivity(), new Intent(getActivity(), MainActivity.class));
                    else if(mModle == 3){
                        getActivity().setResult(1);
                    }else if (mModle == 6)
                        getActivity().setResult(3);
                    getActivity().finish();
                } else {
                    updatePatternTitle(6);
                    patternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                    mHandler.sendEmptyMessageDelayed(2, 500);
                }
                break;
            }

            case 2:{//修改密码
                if (step==0){
                    String password = PreferencesData.getPassword(getContext());
                    if (password.equals(LockPatternUtil.patternToString(patternView.getPattern()))) {
                        //TODO:密码验证正确
                        step = 1;
                        updatePatternTitle(8);
                        patternView.clearPattern();
                    } else {
                        updatePatternTitle(6);
                        patternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                        mHandler.sendEmptyMessageDelayed(2, 500);
                    }
                }else if (step == 1){
                    if (pattern.size() < 4) {
                        updatePatternTitle(2);
                        patternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                        patternView.setEnabled(false);
                        mHandler.sendEmptyMessageDelayed(1, 500);
                    } else {
                        step = 2;
                        updatePatternTitle(3);
                        patternView.setEnabled(false);
                        pswTemp = LockPatternUtil.patternToString(pattern);
                        mHandler.sendEmptyMessageDelayed(0, 500);
                    }
                }else {
                    if (LockPatternUtil.patternToString(pattern).equals(pswTemp)) {
//                        updatePatternTitle(-1);
                        try {
                            pswTemp = SSLUtilExt.getInstance().data_to_encrypt(pswTemp);
                        } catch (SslException e) {
                            e.printStackTrace();
                            //TODO:异常咋办？
                        }
                        PreferencesData.setPassword(getContext(), pswTemp);
                        PreferencesData.setPasswordModle(getContext(), 1);
                        UHAnalytics.changeDataCount(UserHabitID.LM_22);
                        getActivity().finish();
                    } else {//不一致
                        step = 1;
                        pswTemp = null;
                        updatePatternTitle(4);
                        patternView.setDisplayMode(LockPatternView.DisplayMode.Wrong);
                        patternView.setEnabled(false);
                        mHandler.sendEmptyMessageDelayed(1, 500);
                    }
                }

                break;
            }

        }
    }

    private void updatePatternTitle(int status){
        switch (status){
            case 0://初次设置密码
                patternTitle.setText(R.string.set_map_modle);
                break;
            case 1://绘制密码
                patternTitle.setText(R.string.set_map_doing);
                break;
            case 2://密码设置-绘制密码过短
                patternTitle.setText(R.string.set_map_less_point);
                break;
            case 3://密码设置-确认密码
                patternTitle.setText(R.string.set_map_confirm);
                break;
            case 4://密码设置-确认密码不一致
                patternTitle.setText(R.string.set_map_not_match);
                break;
            case 5://图形验证
                patternTitle.setText(R.string.draw_pattern);
                break;
            case 6://图形错误
                patternTitle.setText(R.string.draw_pattern_wrong);
                break;
            case 7://绘制原密码
                patternTitle.setText(R.string.draw_old_pattern);
                break;
            case 8://绘制新密码
                patternTitle.setText(R.string.draw_new_pattern);
                break;
            case -1:
                patternTitle.setText("");
                break;
        }
    }
}
