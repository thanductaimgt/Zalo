package com.mgt.zalo.data_model.post

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.data_model.media.Media

data class Diary(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var ownerId: String? = null,
        override var ownerName: String? = null,
        override var ownerAvatarUrl: String? = null,
        override var text: String = "",
        override var reactCount: Int = 0,
        override var commentCount: Int = 0,
        override var shareCount: Int = 0,
        override var reacts: HashMap<String, React> = hashMapOf(),
        var medias: ArrayList<Media> = ArrayList()
) : Post(
        id, createdTime, ownerId, ownerName, ownerAvatarUrl, text, reactCount, commentCount, shareCount, reacts, TYPE_DIARY
), Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readBundle()!!.let {it.getSerializable(KEY_REACTS) as HashMap<String, React>},
            parcel.readArrayList(Media::class.java.classLoader) as ArrayList<Media>
    )

    override fun toMap(): HashMap<String, Any?> {
        return super.toMap().apply {
            put(FIELD_MEDIAS, medias.map { it.toMap() })
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        super.applyDoc(doc)
        id = doc.id
        doc.get(FIELD_MEDIAS)?.let { medias = (it as ArrayList<Any>).map {any->
            Media.fromObject(any)
        } as ArrayList<Media> }
    }

    fun getFirstMedia(): Media? {
        return medias.firstOrNull()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(createdTime!!)
        parcel.writeString(ownerId)
        parcel.writeString(ownerName)
        parcel.writeString(ownerAvatarUrl)
        parcel.writeString(text)
        parcel.writeInt(reactCount)
        parcel.writeInt(commentCount)
        parcel.writeInt(shareCount)
        parcel.writeBundle(Bundle().apply {
            putSerializable(KEY_REACTS, reacts)
        })
        parcel.writeList(medias as MutableList<Any?>)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Diary> {
        override fun createFromParcel(parcel: Parcel): Diary {
            return Diary(parcel)
        }

        override fun newArray(size: Int): Array<Diary?> {
            return arrayOfNulls(size)
        }

        const val FIELD_MEDIAS = "medias"
        const val KEY_REACTS = "reacts"

        fun fromDoc(doc: DocumentSnapshot): Diary {
            return Diary().apply { applyDoc(doc) }
        }
    }
}