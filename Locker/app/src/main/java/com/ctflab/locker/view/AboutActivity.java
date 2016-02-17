package com.ctflab.locker.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.anguanjia.framework.userhabit.UHAnalytics;
import com.ctflab.locker.FrameActivity;
import com.ctflab.locker.common.UserHabitID;
import com.ctflab.locker.model.UpdateManager;
import com.ctflab.locker.utils.PreferencesData;
import com.ctflab.locker.utils.SettingsUtil;
import com.ctflab.locker.utils.SystemUtil;
import com.ctflab.locker.R;

public class AboutActivity extends FrameActivity implements View.OnClickListener {

    private TextView txtVersion, txtSettingUpdate, txtSettingContact;
//    private Button  btnFacebook,btnTwitter,btnEmail;
//    private boolean resume = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle(R.string.title_activity_about);
        txtVersion = (TextView) findViewById(R.id.txtVersion);
        txtVersion.setText(SystemUtil.getVersion(this));

        txtSettingUpdate = (TextView) findViewById(R.id.txtSettingUpdate);
        txtSettingUpdate.setOnClickListener(this);
        txtSettingContact = (TextView) findViewById(R.id.txtSettingContact);
        txtSettingContact.setOnClickListener(this);

        findViewById(R.id.btnFacebook).setOnClickListener(this);
        findViewById(R.id.btnTwitter).setOnClickListener(this);
        findViewById(R.id.btnEmail).setOnClickListener(this);
//        resume = false;
    }

    @Override
    protected int getContentView() {
        return R.layout.activity_about;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferencesData.isShowAuthentication()) {
            SystemUtil.go2Authen(this);
            return;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
//        resume = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        resume = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtSettingUpdate: {
                UHAnalytics.changeDataCount(UserHabitID.LM_35);
                UpdateManager.checkUpdate(this);
                break;
            }
            case R.id.txtSettingContact: {
//                Intent data = new Intent(Intent.ACTION_SENDTO);
//                data.setData(Uri.parse("mailto:way.ping.li@gmail.com"));
//                data.putExtra(Intent.EXTRA_SUBJECT, "这是标题");
//                data.putExtra(Intent.EXTRA_TEXT, "这是内容");
//                startActivity(data);
                SettingsUtil.sendEmail(this);
                break;
            }
            case R.id.btnEmail: {
                SettingsUtil.sendEmail(this);
                break;
            }
            case R.id.btnFacebook: {
                String facebookUrl = "https://www.facebook.com/Lock.Master.Your.Privacy.Keeper";
                try {
                    int versionCode = getPackageManager().getPackageInfo("com.facebook.katana", 0).versionCode;
//                    if (versionCode >= 3002850) {
                    Uri uri = Uri.parse("fb://facewebmodal/f?href=" + facebookUrl);
                    startActivity(new Intent(Intent.ACTION_VIEW, uri));;
//                    } else {
//                        // open the Facebook app using the old method (fb://profile/id or fb://page/id)
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/336227679757310")));
//                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // Facebook is not installed. Open the browser
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
                }
                break;
            }
            case R.id.btnTwitter: {
                Intent intent = null;
                try {
                    // get the Twitter app if possible
                    this.getPackageManager().getPackageInfo("com.twitter.android", 0);
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/Lock__Master"));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                } catch (Exception e) {
                    // no Twitter app, revert to browser
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/Lock__Master"));
                }
                this.startActivity(intent);
                break;
            }
        }
    }


}