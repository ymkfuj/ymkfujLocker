package com.ctflab.locker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.ViewStubCompat;
import android.view.MenuItem;

abstract public class FrameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initToolBar();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewStubCompat vc = (ViewStubCompat) findViewById(R.id.content);
        vc.setLayoutResource(getContentView());
        vc.inflate();
//        PreferencesData.setShowAuthentication(false);
    }

    abstract protected int getContentView();

    /**
     * 默认响应 onBackClicked
     * 也可以自己设置Toolbar.setNavigationOnClickListener
     * 也可以通过 onOptionsItemSelected itemId：android.R.id.home响应
     */
    protected void initToolBar(){
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.selector_back);
    }

    /**
     * 返回按钮响应事件
     */
    protected void onBackClicked(){
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
