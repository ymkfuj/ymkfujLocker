package com.ctflab.locker.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.anguanjia.framework.components.AnguanjiaActivity;
import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.MainActivity;
import com.ctflab.locker.R;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.utils.AppListUtil;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.SystemUtil;

/**
 * Created by hejw on 2016/1/11.
 */
public class SplashActivity extends AnguanjiaActivity {


    private static final int MSG_STRAT = 0;
    private static final int MSG_GUIDE = 1;
    private static final int MSG_RESTART = 2;
    private static final int MSG_SERVICES = 3;

    private static int DELAY_SPLASH = 2500;

    private LinearLayout mLinearDot;
    /**
     * 进入主界面的按钮
     **/
    private Button btnStart;
    private final int mPageCount = 4;
    private View[] mViewList = new View[mPageCount];

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_STRAT: {

                    if (SystemUtil.isRootSystem()) {
                        UHAnalytics.changeUHOpen(UserHabitID.LM_1);
                    } else {
                        UHAnalytics.changeUHClose(UserHabitID.LM_1);
                    }

                    int type = PreferencesData.getPasswordModle(SplashActivity.this);
                    PreferencesData.setPasswordModle(SplashActivity.this, type);
//                    mHandler.sendEmptyMessageDelayed(MSG_GUIDE, DELAY_SPLASH);
                    mHandler.sendEmptyMessageDelayed(MSG_SERVICES, DELAY_SPLASH);
                    break;
                }
                case MSG_GUIDE: {
                    findViewById(R.id.layout_guide).setVisibility(View.VISIBLE);
                    findViewById(R.id.layout_splash).setVisibility(View.GONE);
                    break;
                }
                case MSG_RESTART: {
                    SystemUtil.startActivity(SplashActivity.this, new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                    break;
                }
                case MSG_SERVICES: {
                    SystemUtil.startActivity(SplashActivity.this, new Intent(SplashActivity.this, AuthenticationActivity.class));
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        //隐藏状态栏
        //定义全屏参数
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //获得当前窗体对象
        Window window = SplashActivity.this.getWindow();
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        AppListUtil.getInstalledAppList(this, null);

        boolean restart = getIntent().getBooleanExtra("restart", false);
        if (restart) {
            //切换语言后重启，不要输密码，直接进主页面
            mHandler.sendEmptyMessage(MSG_RESTART);
        } else {
            boolean isFirst = SettingsUtil.ifFirst(SplashActivity.this);
//            if (isFirst) {
//                setContentView(R.layout.activity_splash);
//                initViews();
//                mHandler.sendEmptyMessage(MSG_STRAT);
//            } else {
//                mHandler.sendEmptyMessage(MSG_SERVICES);
//            }

            setContentView(R.layout.activity_splash);
            initViews();
            // 首次启动后不应展现闪屏图片，直接展现产品轮播图片
            // 非首次启动应用，启动时应展现闪屏图片
            if (isFirst) {
                mHandler.sendEmptyMessage(MSG_GUIDE);
            } else {
                mHandler.sendEmptyMessage(MSG_STRAT);
            }
        }
    }

    private void initViews() {
        mLinearDot = (LinearLayout) findViewById(R.id.linear_dot);
        mLinearDot.setVisibility(mPageCount == 1 ? View.GONE : View.VISIBLE);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.viewpager);

        mViewPager.setAdapter(new MyViewPagerAdapter());
        setImageHighLight(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                // if (position != mViewList.length - 1) {
                mLinearDot.setVisibility(View.VISIBLE);
                setImageHighLight(position);
                // } else {
                // mLinearDot.setVisibility(View.GONE);
                // }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
        });
    }

    /**
     * 生成viewPager的View
     */
    private View createView(final int index) {
        try {
            View view = null;
            switch (index) {
                case 0:
                    view = SystemUtil.inflateView(this, R.layout.guide_item_0);
                    break;
                case 1:
                    view = SystemUtil.inflateView(this, R.layout.guide_item_1);
                    break;
                case 2:
                    view = SystemUtil.inflateView(this, R.layout.guide_item_2);
                    break;
                case 3:
                default:
                    view = SystemUtil.inflateView(this, R.layout.guide_item_3);

                    btnStart = (Button) view.findViewById(R.id.btnStart);
                    btnStart.setVisibility(View.VISIBLE);
                    btnStart.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SettingsUtil.saveGuideLaunched(SplashActivity.this);
                            addShortcut();
                            mHandler.sendEmptyMessage(MSG_SERVICES);
                        }
                    });

                    break;
            }
            return view;
        } catch (Throwable e) {
            return new TextView(this);
        }
    }


    private final class MyViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            if (mViewList != null) {
                return mViewList.length;
            }
            return 0;
        }

        @Override
        public Object instantiateItem(View container, int position) {
            View view = mViewList[position];
            if (view == null) {
                view = createView(position);
                mViewList[position] = view;
            }
            ((ViewPager) container).addView(view, 0);
            return view;
        }

        @Override
        public void destroyItem(View view, int arg1, Object arg2) {
            ((ViewPager) view).removeView(mViewList[arg1]);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return (view == object);
        }
    }

    /**
     * 设置高亮的点点
     */
    private void setImageHighLight(int whichLight) {
        mLinearDot.removeAllViews();

        // int value = SystemUtil.dip2px(this, 5);
        for (int i = 0; i < mPageCount; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setPadding(20, 0, 20, 0);
            if (i == whichLight) {
                imageView.setImageResource(R.drawable.index_icon_current);
            } else {
                imageView.setImageResource(R.drawable.index_icon_other);
            }
            mLinearDot.addView(imageView);
        }

    }

    public static final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

    private void addShortcut() {
        Intent addShortcutIntent = new Intent(ACTION_ADD_SHORTCUT);

        // 不允许重复创建
        addShortcutIntent.putExtra("duplicate", false);// 经测试不是根据快捷方式的名字判断重复的
        // 应该是根据快链的Intent来判断是否重复的,即Intent.EXTRA_SHORTCUT_INTENT字段的value
        // 但是名称不同时，虽然有的手机系统会显示Toast提示重复，仍然会建立快链
        // 屏幕上没有空间时会提示
        // 注意：重复创建的行为MIUI和三星手机上不太一样，小米上似乎不能重复创建快捷方式

        // 名字
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));

        // 图标
        addShortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(this,
                        R.mipmap.ic_launcher));

        // 设置关联程序
        Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
        launcherIntent.setClass(this, SplashActivity.class);
        launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        addShortcutIntent
                .putExtra(Intent.EXTRA_SHORTCUT_INTENT, launcherIntent);

        // 发送广播
        sendBroadcast(addShortcutIntent);
    }

//    public void createShortCut(){
//        Intent addShortCut;
//        //判断是否需要添加快捷方式
//        if(getIntent().getAction().equals(Intent.ACTION_CREATE_SHORTCUT)){
//            addShortCut = new Intent();
//            //快捷方式的名称
//            addShortCut.putExtra(Intent.EXTRA_SHORTCUT_NAME , getString(R.string.app_name));
//            //显示的图片
//            Parcelable icon = Intent.ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher);
//            addShortCut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);
//            //快捷方式激活的activity，需要执行的intent，自己定义
//            addShortCut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new Intent());
//            //OK，生成
//            setResult(RESULT_OK, addShortCut);
//        }else{
//            //取消
//            setResult(RESULT_CANCELED);
//        }
//    }

}
