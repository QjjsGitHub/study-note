package com.example.study.service;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class People implements Parcelable {

    int number = 1;
    String name = "name";

    People() {
        number = 2;
        name = "name2";
    }


    public static final Parcelable.Creator<People> CREATOR = new Parcelable.Creator<People>() {
        public People createFromParcel(Parcel in) {
            return new People(in);
        }

        public People[] newArray(int size) {
            return new People[size];
        }
    };

    public People(Parcel in) {
        readFromParcel(in);
    }

    public People(int i, String newName) {
        number = i;
        name = newName;
    }

    private void readFromParcel(Parcel in) {
        number = in.readInt();
        name = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(number);
        parcel.writeString(name);
    }
}
