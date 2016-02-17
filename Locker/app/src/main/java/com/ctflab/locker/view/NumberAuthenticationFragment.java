package com.ctflab.locker.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ctflab.locker.MainActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SystemUtil;

/**
 * Created by hejw on 2016/1/8.
 * 数字密码界面
 */
public class NumberAuthenticationFragment extends Fragment implements NumberInputView.InputListener,View.OnClickListener{

    private ImageView number0,number1,number2,number3,number4,number5,number6,number7,number8,number9,numberBack;
    private NumberInputView numberInputView;
    private GridLayout inputLayout;
    private TextView numberTitle,switchIcon;
    private int mModle;//当前模式0:设置密码，1：验证密码2：修改密码 3:修改解锁方式-验证密码 4:修改解锁方式 5:重启密码验证
    private RelativeLayout switchView;
    private LinearLayout switchButton;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mModle = ((AuthenticationActivity)getActivity()).getModle();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.number_layout,container,false);
        initView(view);
        return view;
    }

    private void initView(View view){
        numberTitle = (TextView) view.findViewById(R.id.numberTitle);

        numberInputView = (NumberInputView) view.findViewById(R.id.numberInput);
        numberInputView.setListener(this);
        numberInputView.setModle(mModle);

        inputLayout = (GridLayout) view.findViewById(R.id.inputLayout);
        inputLayout.setEnabled(false);

        number0 = (ImageView) view.findViewById(R.id.number0);
        number1 = (ImageView) view.findViewById(R.id.number1);
        number2 = (ImageView) view.findViewById(R.id.number2);
        number3 = (ImageView) view.findViewById(R.id.number3);
        number4 = (ImageView) view.findViewById(R.id.number4);
        number5 = (ImageView) view.findViewById(R.id.number5);
        number6 = (ImageView) view.findViewById(R.id.number6);
        number7 = (ImageView) view.findViewById(R.id.number7);
        number8 = (ImageView) view.findViewById(R.id.number8);
        number9 = (ImageView) view.findViewById(R.id.number9);
        numberBack = (ImageView) view.findViewById(R.id.numberBack);

        number0.setOnClickListener(this);
        number1.setOnClickListener(this);
        number2.setOnClickListener(this);
        number3.setOnClickListener(this);
        number4.setOnClickListener(this);
        number5.setOnClickListener(this);
        number6.setOnClickListener(this);
        number7.setOnClickListener(this);
        number8.setOnClickListener(this);
        number9.setOnClickListener(this);
        numberBack.setOnClickListener(this);

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

        switchView = (RelativeLayout) view.findViewById(R.id.switch_view);
        switchIcon = (TextView) view.findViewById(R.id.switch_icon);
        switchButton = (LinearLayout) view.findViewById(R.id.switch_number);
        switchButton.setBackgroundResource(R.drawable.selector_common);
        switchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mModle == 0) {
                    AuthenticationActivity authenticationActivity = (AuthenticationActivity) getActivity();
                    authenticationActivity.switchFragment(1);
                } else {
                    SystemUtil.startActivity(getActivity(), new Intent(getActivity(), ForgetPasswordActivity.class));
                    getActivity().finish();
                }

            }
        });
        numberBack.setEnabled(false);

        TextView hint = (TextView) view.findViewById(R.id.textHint);

        switch (mModle){
            case 0:
                switchView.setVisibility(View.VISIBLE);
                switchIcon.setVisibility(View.VISIBLE);
                hint.setTextColor(getResources().getColor(R.color.switch_psw_button));
                break;
            case 1:
            case 5:
                switchView.setVisibility(View.VISIBLE);
                hint.setText(R.string.forget_password);
                hint.setCompoundDrawables(null,null,null,null);
                hint.setTextColor(getResources().getColor(R.color.text_blue));
                break;
            default:
                switchView.setVisibility(View.INVISIBLE);
                break;
        }

        if (mModle == 2){
            numberTitle.setText(R.string.input_old_password);
        }else {
            numberTitle.setText(R.string.set_number_password);
        }
    }


    @Override
    public void inputError() {
        numberTitle.setText(R.string.input_number_wrong);
    }

    @Override
    public void inputEnsure() {
        numberTitle.setText(R.string.set_number_password_ensure);
    }

    @Override
    public void inputSuccess() {
        if (mModle == 1 ||mModle == 3 || mModle == 5 || mModle == 6){
            PreferencesData.setShowAuthentication(false);
        }
        if (mModle ==0) {
            PreferencesData.setLockStatus(getActivity(),true);
            SystemUtil.startActivity(getActivity(), new Intent(getActivity(), EmailAddActivity.class));
        }
        else if(mModle ==1)
            SystemUtil.startActivity(getActivity(), new Intent(getActivity(), MainActivity.class));
        else if (mModle == 3){
            getActivity().setResult(1);
        }else if (mModle == 4){
            getActivity().setResult(2);
        }else if (mModle == 6)
            getActivity().setResult(3);
        getActivity().finish();

    }

    @Override
    public void inputDiff() {
        numberTitle.setText(R.string.set_number_password_diff);
    }

    @Override
    public void refreshTitle(boolean eidt) {
        numberTitle.setText(eidt?R.string.input_new_password:R.string.set_number_password);
    }

    @Override
    public void clearEnable(boolean enable) {
        numberBack.setEnabled(enable);
    }

    @Override
    public void onClick(View v) {
        String tag = (String) v.getTag();
        if (!TextUtils.isEmpty(tag)){//数字
            numberInputView.setPassword(tag);
        }else {//退格
            numberInputView.backOne();
        }
    }
}
