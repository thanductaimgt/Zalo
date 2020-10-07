package com.mgt.zalo.data_model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.post.Diary
import com.mgt.zalo.data_model.react.React

data class Comment(
        var id: String? = null,
        var postId: String? = null,
        var createdTime: Long? = null,
        var ownerId: String? = null,
        var ownerName: String? = null,
        var ownerAvatarUrl: String? = null,
        var text: String = "",
        var reactCount: Int = 0,
        var reacts: HashMap<String, React> = hashMapOf(),
        var media: Media? = null,
        var replyCount: Int = 0,
        var replyToId: String?=null // for reply
) : BaseDataModel, Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readValue(Long::class.java.classLoader) as? Long,
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()!!,
            parcel.readInt(),
            parcel.readBundle()!!.let {it.getSerializable(KEY_REACTS) as HashMap<String, React>},
            parcel.readParcelable(Media::class.java.classLoader),
            parcel.readInt(),
            parcel.readString()) {
    }

    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            ownerId?.let { put(FIELD_OWNER_ID, it) }
            put(FIELD_REACT_COUNT, reactCount)
            put(FIELD_TEXT, text)
            media?.let { put(FIELD_MEDIA, it.toMap()) }
            put(FIELD_REPLY_COUNT, replyCount)
        }
    }

    override fun applyDoc(doc: DocumentSnapshot) {
        id = doc.id
        doc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        doc.getString(FIELD_OWNER_ID)?.let { ownerId = it }
        doc.getString(FIELD_TEXT)?.let { text = it }
        doc.getLong(FIELD_REACT_COUNT)?.let { reactCount = it.toInt() }
        doc.get(FIELD_MEDIA)?.let { media = Media.fromObject(it) }
        doc.getLong(FIELD_REPLY_COUNT)?.let { replyCount = it.toInt() }
        doc.get(FIELD_REACTS)?.let {
            reacts = (it as HashMap<String, Long?>).mapValues { entry->
                React(
                        ownerId = entry.key,
                        type = if (entry.value != null) entry.value!!.toInt() else React.TYPE_LOVE
                )
            } as HashMap<String, React>
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(postId)
        parcel.writeValue(createdTime)
        parcel.writeString(ownerId)
        parcel.writeString(ownerName)
        parcel.writeString(ownerAvatarUrl)
        parcel.writeString(text)
        parcel.writeInt(reactCount)
        parcel.writeParcelable(media, flags)
        parcel.writeInt(replyCount)
        parcel.writeString(replyToId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }

        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_OWNER_ID = "ownerId"
        const val FIELD_TEXT = "description"
        const val FIELD_REACT_COUNT = "reactCount"
        const val FIELD_MEDIA = "media"
        const val FIELD_REPLY_COUNT = "replyCount"
        const val FIELD_REACTS = "reacts"

        const val KEY_REACTS = "reacts"

        const val PAYLOAD_METRICS = 0

        fun fromDoc(doc: DocumentSnapshot): Comment {
            return Comment().apply { applyDoc(doc) }
        }
    }
}