package com.ctflab.locker;

public class TestActivity extends FrameActivity {

    @Override
    protected int getContentView() {
        return R.layout.test_frame;
    }

    @Override
    protected void onResume() {
//        progressBar.setColorSchemeColors(android.R.color.holo_blue_bright,android.R.color.holo_orange_light,android.R.color.holo_red_light);
        super.onResume();
    }
}
