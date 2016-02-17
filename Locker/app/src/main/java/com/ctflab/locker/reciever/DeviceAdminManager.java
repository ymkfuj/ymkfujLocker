package com.ctflab.locker.reciever;


import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.ctflab.locker.R;
import com.ctflab.locker.utils.PreferencesData;


public class DeviceAdminManager {
	
	DevicePolicyManager mDPM;
    ComponentName mDeviceAdmin;
    
    public DeviceAdminManager(Context context){
    		mDPM = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    		mDeviceAdmin = new ComponentName(context, DeviceAdminProof.class);
    }
    
    /**
     * Helper to determine if we are an active admin
     */
    public boolean isActiveAdmin() {
        return mDPM.isAdminActive(mDeviceAdmin);
    }
    
    public void unRegisterDeviceAdmin(){
	    	if(isActiveAdmin()){
	    		mDPM.removeActiveAdmin(mDeviceAdmin);
	    	}
    }
    /**
     * 手动注册设备管理员
     * @param context
     */
    public void registerDeviceAdmin(Context context){
		Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        context.startActivity(intent);
		PreferencesData.setGo2Permission(true);
    }
    
	/**
	 * 对应设备管理器的receiver
	 * @author anguanjia
	 *
	 */
	public static class DeviceAdminProof extends DeviceAdminReceiver {

	    @Override
	    public void onEnabled(Context context, Intent intent) {
//			SystemUtil.showToast(context,R.string.app_name);
	    }

	    @Override
	    public CharSequence onDisableRequested(Context context, Intent intent) {
				return context.getString(R.string.permission_close_noti);

	    }

	    @Override
	    public void onDisabled(Context context, Intent intent) {
//			SystemUtil.showToast(context,R.string.app_name);
//	        showToast(context, "onDisabled");
	    }

	    @Override
	    public void onPasswordChanged(Context context, Intent intent) {
//	        showToast(context, "onPasswordChanged");
	    }
	}
	
}
