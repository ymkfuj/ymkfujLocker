package com.anguanjia.framework.userhabit;

import android.os.Parcel;
import android.os.Parcelable;

public class UHItem implements Parcelable {
    public int type;
    public String key;
    public String value;
    public boolean acc;

    public UHItem(int type, String key, String value, boolean acc){
        this.type = type;
        this.key = key;
        this.value = value;
        this.acc = acc;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeString(this.key);
        dest.writeString(this.value);
        dest.writeInt(this.acc ? 1 : 0);
    }

    protected UHItem(Parcel in) {
        this.type = in.readInt();
        this.key = in.readString();
        this.value = in.readString();
        this.acc = in.readInt() == 1 ? true : false;
    }

    public static final Parcelable.Creator<UHItem> CREATOR = new Parcelable.Creator<UHItem>() {
        public UHItem createFromParcel(Parcel source) {
            return new UHItem(source);
        }

        public UHItem[] newArray(int size) {
            return new UHItem[size];
        }
    };
}
