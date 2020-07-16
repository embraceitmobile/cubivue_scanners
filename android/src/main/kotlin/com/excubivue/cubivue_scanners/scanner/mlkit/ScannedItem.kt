package com.excubivue.cubivue_scanners.scanner.mlkit

import android.os.Parcel
import android.os.Parcelable

data class ScannedItem(var item: String = "", var time: Long = 0L) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(item)
        parcel.writeLong(time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ScannedItem> {
        override fun createFromParcel(parcel: Parcel): ScannedItem {
            return ScannedItem(parcel)
        }

        override fun newArray(size: Int): Array<ScannedItem?> {
            return arrayOfNulls(size)
        }
    }

}