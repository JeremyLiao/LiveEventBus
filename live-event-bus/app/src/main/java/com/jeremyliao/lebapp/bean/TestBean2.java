package com.jeremyliao.lebapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class TestBean2 implements Parcelable {
    public String content;

    private TestBean2(Parcel in) {
        content = in.readString();
    }

    public TestBean2() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(content);
    }

    public static final Parcelable.Creator<TestBean2> CREATOR
            = new Parcelable.Creator<TestBean2>() {
        public TestBean2 createFromParcel(Parcel in) {
            return new TestBean2(in);
        }

        public TestBean2[] newArray(int size) {
            return new TestBean2[size];
        }
    };

    @Override
    public String toString() {
        return content;
    }
}
