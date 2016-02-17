package com.ctflab.locker.common;

import android.graphics.drawable.Drawable;

import com.ctflab.locker.utils.CharacterCompareUtil;

/**
 * Created by hejw on 2016/1/7.
 * <p/>
 * app相关信息实体类
 */
public class AppInfoEntity implements Comparable {
    //app logo
    public Drawable icon;
    //app名
    public String lable;
    //app包名
    public String packgeName;

    public boolean isPerset = false;

    public boolean isLoacked = false;

    @Override
    public int compareTo(Object another) {

        if (this.isLoacked && !((AppInfoEntity) another).isLoacked) {
            return -1;
        } else if (!this.isLoacked && ((AppInfoEntity) another).isLoacked) {
            return 1;
        } else {
            if (this.isPerset && !((AppInfoEntity) another).isPerset) {
                return -1;
            } else if (!this.isPerset && ((AppInfoEntity) another).isPerset) {
                return 1;
            } else {
                return CharacterCompareUtil.singleSort(lable, ((AppInfoEntity) another).lable);
            }
        }
    }
}
