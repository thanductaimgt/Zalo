package com.mgt.zalo.data_model.story

import com.google.firebase.firestore.DocumentSnapshot
import com.mgt.zalo.data_model.User
import java.util.*

data class StoryGroup(
        var id: String? = null,
        var name: String? = null,
        var avatarUrl: String? = null,
        var ownerId: String? = null,
        var ownerName: String? = null,
        var ownerAvatarUrl: String? = null,
        var stories: List<Story>? = null,
        var curPosition: Int = 0,
        var createdTime:Long?=null,
        var storyCount:Int?=null
) {
    fun toMap(): HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            name?.let { put(FIELD_NAME, it) }
            avatarUrl?.let { put(FIELD_AVATAR_URL, it) }
            createdTime?.let { put(FIELD_CREATED_TIME, it) }
            storyCount?.let { put(FIELD_STORY_COUNT, it) }
        }
    }

    fun applyUserDoc(userDoc: DocumentSnapshot) {
        id = ID_RECENT_STORIES
        ownerId = userDoc.id
        userDoc.getString(User.FIELD_NAME)?.let { ownerName = it }
        userDoc.getString(User.FIELD_AVATAR_URL)?.let { ownerAvatarUrl = it }
        userDoc.getLong(User.FIELD_LAST_STORY_CREATED_TIME)?.let { createdTime = it }
    }

    fun applyStoryGroupDoc(storyGroupDoc: DocumentSnapshot, user: User) {
        id = storyGroupDoc.id
        storyGroupDoc.getString(FIELD_NAME)?.let { name = it }
        storyGroupDoc.getString(FIELD_AVATAR_URL)?.let { avatarUrl = it }
        ownerId = user.id
        ownerName = user.name
        ownerAvatarUrl = user.avatarUrl
        storyGroupDoc.getLong(FIELD_CREATED_TIME)?.let { createdTime = it }
        storyGroupDoc.getLong(FIELD_STORY_COUNT)?.let { storyCount = it.toInt() }
    }

    companion object {
        const val PAYLOAD_NEW_STORY = 0
        const val PAYLOAD_ADDED_STATE = 1

        const val ID_RECENT_STORIES = "ID_RECENT_STORIES"
        const val ID_RECENT_STORY_GROUP = "ID_RECENT_STORY_GROUP"
        const val ID_CREATE_STORY = "ID_CREATE_STORY"
        const val ID_CREATE_STORY_GROUP = "ID_CREATE_STORY_GROUP"

        const val FIELD_NAME = "name"
        const val FIELD_AVATAR_URL = "avatarUrl"
        const val FIELD_CREATED_TIME = "createdTime"
        const val FIELD_STORY_COUNT = "storyCount"

        fun fromUserDoc(doc: DocumentSnapshot): StoryGroup {
            return StoryGroup().apply { applyUserDoc(doc) }
        }

        fun fromStoryGroupDoc(doc: DocumentSnapshot, user:User): StoryGroup {
            return StoryGroup().apply { applyStoryGroupDoc(doc, user) }
        }
    }
}