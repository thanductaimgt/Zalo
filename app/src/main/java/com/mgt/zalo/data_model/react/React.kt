package com.mgt.zalo.data_model.react

import com.mgt.zalo.R
import com.mgt.zalo.data_model.BaseDataModel

data class React(
        var ownerId: String? = null,
        var ownerName: String? = null,
        var ownerAvatarUrl: String? = null,
        var type: Int = TYPE_LOVE,
        var createdTime: Long? = null
) : BaseDataModel {
    override fun toMap(): HashMap<String, Any?> {
        return HashMap<String, Any?>().apply {
            ownerId?.let { put(FIELD_OWNER_ID, it) }
            put(FIELD_TYPE, type)
            put(FIELD_CREATED_TIME, createdTime)
        }
    }

    fun applyMap(map: Map<String, Any?>) {
        map[FIELD_OWNER_ID]?.let { ownerId = it as String }
        map[FIELD_TYPE]?.let { type = it as Int }
        map[FIELD_CREATED_TIME]?.let { createdTime = it as Long }
    }

    companion object {
        const val FIELD_OWNER_ID = "ownerId"
        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_TYPE = "type"

        const val TYPE_LOVE = 0
        const val TYPE_LIKE = 1
        const val TYPE_HAHA = 2
        const val TYPE_SAD = 3
        const val TYPE_WOW = 4
        const val TYPE_ANGRY = 5
        const val TYPE_CARE = 6
        const val TYPE_ALL = 10

        fun getDrawableResId(type: Int): Int {
            return when (type) {
                0 -> R.drawable.ic_love
                1 -> R.drawable.ic_like
                2 -> R.drawable.ic_haha
                3 -> R.drawable.ic_sad
                4 -> R.drawable.ic_wow
                5 -> R.drawable.ic_angry
                else -> R.drawable.ic_care
            }
        }

        fun getTextColorResId(type: Int):Int{
            return when (type) {
                0 -> R.color.missedCall
                1 -> R.color.blue
                2 -> R.color.yellow
                3 -> R.color.yellow
                4 -> R.color.yellow
                5 -> R.color.orange
                else -> R.color.yellow
            }
        }

        fun getText(type: Int):Int{
            return when (type) {
                0 -> R.string.label_love
                1 -> R.string.label_like
                2 -> R.string.label_haha
                3 -> R.string.label_sad
                4 -> R.string.label_wow
                5 -> R.string.label_angry
                else -> R.string.label_care
            }
        }
    }
}