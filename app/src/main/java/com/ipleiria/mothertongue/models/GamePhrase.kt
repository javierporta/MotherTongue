package com.ipleiria.mothertongue.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable

data class GamePhrase(var phrase: String?, var wasGuessed: Boolean) : Parcelable, Serializable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phrase)
        parcel.writeByte(if (wasGuessed) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GamePhrase> {
        override fun createFromParcel(parcel: Parcel): GamePhrase {
            return GamePhrase(parcel)
        }

        override fun newArray(size: Int): Array<GamePhrase?> {
            return arrayOfNulls(size)
        }
    }

}