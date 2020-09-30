package com.mgt.zalo.data_model.media

import android.os.Parcel

data class ImageMedia(
        override var uri: String?=null,
        override var ratio: String? = null,
        override var description: String="",
        override var reactCount: Int=0,
        override var commentCount: Int=0,
        override var shareCount: Int=0
) : Media(uri, ratio, description, reactCount, commentCount, shareCount) {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(TYPE_IMAGE)
        super.writeToParcel(parcel, flags)
    }
}