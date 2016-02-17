package com.ctflab.locker.view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anguanjia.framework.userhabit.UHAnalytics;
import com.anguanjia.framework.utils.MyLog;
import com.anguanjia.framework.utils.SSLUtilExt;
import com.anguanjia.framework.utils.SslException;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SystemUtil;

import java.util.ArrayList;

/**
 * Created by hejw on 2016/1/8.
 * 数字密码输入框
 */
public class NumberInputView extends LinearLayout{

    private ArrayList<String> passWord = new ArrayList<String>();
    private TextView number0,number1,number2,number3;
    private int setModle;
    private InputListener listener;
    private Animation shake;
    private String tempPassword;
    private int step;
    private Context context;

    private Handler mHandle = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 0://密码输入错误
                    clearnAllView();
                    break;
                case 1://再次输入密码确认
                    listener.inputEnsure();
                    clearnAllView();
                    break;
                case 2://前后密码输入不一致
                    clearnAllView();
                    listener.refreshTitle(setModle == 2);
                    break;
                case 3:
                    listener.inputSuccess();
                    break;
                case 4://修改密码验证完成刷新界面
                    listener.refreshTitle(false);
                    clearnAllView();
                    break;
            }
        }
    };

    public NumberInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.number_input_view_layout,this);
        number0 = (TextView) view.findViewById(R.id.input_show0);
        number1 = (TextView) view.findViewById(R.id.input_show1);
        number2 = (TextView) view.findViewById(R.id.input_show2);
        number3 = (TextView) view.findViewById(R.id.input_show3);
        shake = AnimationUtils.loadAnimation(context, R.anim.shake);

    }

    public void setListener(InputListener listener){
        this.listener = listener;
    }

    public void setModle(int setModle){
        this.setModle = setModle;
    }

    public void setPassword(String number){
        if (passWord.size()==4){
            clearnAllView();
        }
        passWord.add(number);
        refreshView();
    }

    private void refreshView(){
        switch (passWord.size()){
            case 0:
                number0.setBackgroundResource(R.drawable.icon_pin_noinput);
                number1.setBackgroundResource(R.drawable.icon_pin_noinput);
                number2.setBackgroundResource(R.drawable.icon_pin_noinput);
                number3.setBackgroundResource(R.drawable.icon_pin_noinput);
                listener.clearEnable(false);
                break;

            case 1://输入一位密码
                number0.setBackgroundResource(R.drawable.icon_pin_enter);
                number1.setBackgroundResource(R.drawable.icon_pin_noinput);
                number2.setBackgroundResource(R.drawable.icon_pin_noinput);
                number3.setBackgroundResource(R.drawable.icon_pin_noinput);
                listener.clearEnable(true);
                break;
            case 2://输入两位密码
                number0.setBackgroundResource(R.drawable.icon_pin_enter);
                number1.setBackgroundResource(R.drawable.icon_pin_enter);
                number2.setBackgroundResource(R.drawable.icon_pin_noinput);
                number3.setBackgroundResource(R.drawable.icon_pin_noinput);
                break;
            case 3://输入三位密码
                number0.setBackgroundResource(R.drawable.icon_pin_enter);
                number1.setBackgroundResource(R.drawable.icon_pin_enter);
                number2.setBackgroundResource(R.drawable.icon_pin_enter);
                number3.setBackgroundResource(R.drawable.icon_pin_noinput);
                break;
            case 4:
                if (setModle == 0 || setModle == 4){//设置或修改密码时前后密码输入不一致

                    if (TextUtils.isEmpty(tempPassword)){//进二次确认
                        passwordView();
                        tempPassword = getNumberPassword();
                        MyLog.d("inputNumber","tempPassword:"+tempPassword);
                        mHandle.sendEmptyMessageDelayed(1, 500);
                    }else {
                        if (tempPassword.equals(getNumberPassword())){//设置完成
                            passwordView();
                            PreferencesData.setPassword(getContext(), getPassword());
                            PreferencesData.setPasswordModle(getContext(), 0);
                            listener.inputSuccess();
                            mHandle.sendEmptyMessageDelayed(0, 500);

                        }else {//前后密码输入不一致
                            errorView();
                            listener.inputDiff();
                            tempPassword = null;
                            this.startAnimation(shake);
                            mHandle.sendEmptyMessageDelayed(2, 500);
                        }
                    }

                } else if(setModle == 1 || setModle == 3 || setModle == 5 || setModle == 6){
                    String passwrod = PreferencesData.getPassword(getContext());
                    if (getNumberPassword().equals(passwrod)){//密码正确
                        passwordView();
                        mHandle.sendEmptyMessageDelayed(3,50);
                    }else {//密码错误
                        errorView();
                        listener.inputError();
                        this.startAnimation(shake);
                        mHandle.sendEmptyMessageDelayed(0, 500);
                    }
                }else {
                    if (step == 0){//修改密码-密码验证
                        String passwrod = PreferencesData.getPassword(getContext());
                        if (getNumberPassword().equals(passwrod)){//密码正确
                            passwordView();
                            mHandle.sendEmptyMessageDelayed(2, 200);
                            step = 1;
                        }else {//密码错误
                            errorView();
                            listener.inputError();
                            this.startAnimation(shake);
                            mHandle.sendEmptyMessageDelayed(0, 500);
                        }
                    }else if (step == 1){//修改密码-二次确认
                        step = 2;
                        passwordView();
                        tempPassword = getNumberPassword();
                        MyLog.d("inputNumber","tempPassword:"+tempPassword);
                        mHandle.sendEmptyMessageDelayed(1, 500);
                    }else {
                        if (tempPassword.equals(getNumberPassword())){//修改密码-完成
                            UHAnalytics.changeDataCount(UserHabitID.LM_22);
                            passwordView();
                            PreferencesData.setPassword(getContext(), getPassword());
                            PreferencesData.setPasswordModle(getContext(), 0);
                            listener.inputSuccess();
                            mHandle.sendEmptyMessageDelayed(0, 500);

                        }else {//前后密码输入不一致
                            step = 1;
                            errorView();
                            listener.inputDiff();
                            tempPassword = null;
                            this.startAnimation(shake);
                            mHandle.sendEmptyMessageDelayed(2, 500);
                        }
                    }
                }

                break;
        }
    }

    private void errorView(){
        number0.setBackgroundResource(R.drawable.icon_pin_error);
        number1.setBackgroundResource(R.drawable.icon_pin_error);
        number2.setBackgroundResource(R.drawable.icon_pin_error);
        number3.setBackgroundResource(R.drawable.icon_pin_error);
    }

    private void passwordView(){
        number0.setBackgroundResource(R.drawable.icon_pin_enter);
        number1.setBackgroundResource(R.drawable.icon_pin_enter);
        number2.setBackgroundResource(R.drawable.icon_pin_enter);
        number3.setBackgroundResource(R.drawable.icon_pin_enter);
    }

    public String getPassword(){
        try {
            return SSLUtilExt.getInstance().data_to_encrypt(getNumberPassword());
        }catch (SslException e){
            MyLog.d("inputNumber","exception:"+e.getMessage());
            return "";
        }

    }

    private String getNumberPassword(){
        StringBuilder builder = new StringBuilder();
        for (String num:passWord){
            builder.append(num);
        }
        return builder.toString();
    }

    public void clearnAllView(){
        mHandle.removeMessages(0);
        mHandle.removeMessages(2);
        this.clearAnimation();
        passWord.clear();
        number0.setBackgroundResource(R.drawable.icon_pin_noinput);
        number1.setBackgroundResource(R.drawable.icon_pin_noinput);
        number2.setBackgroundResource(R.drawable.icon_pin_noinput);
        number3.setBackgroundResource(R.drawable.icon_pin_noinput);
        listener.clearEnable(false);
    }

    //退格
    public void backOne(){
        if (passWord.size()>0){
            passWord.remove(passWord.size()-1);
        }
        refreshView();
    }

    public interface InputListener{
        //密码输入错误
        void inputError();
        //设置或修改密码时二次确认
        void inputEnsure();
        //密码输入正确
        void inputSuccess();
        //密码输入不一致
        void inputDiff();
        //刷新title
        void refreshTitle(boolean edit);

        void clearEnable(boolean enable);

    }
    //适配有虚拟按键时的密码加密界面显示与提示文字重合问题
    public int getHeightMargin(int h){
        int height = 0;
        if (SystemUtil.checkDeviceHasNavigationBar(context)) {
            Resources resources = getResources();
            try {
                int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    height = resources.getDimensionPixelSize(resourceId);
                }
            } catch (Resources.NotFoundException e) {
                e.printStackTrace();
            }
        }

        return height == 0?h:(h-height/5);
    }


}
