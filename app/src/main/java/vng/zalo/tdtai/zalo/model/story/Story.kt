package vng.zalo.tdtai.zalo.model.story

abstract class Story(
        open var id: String? = null,
        open var createdTime: Long? = null,
        open var ownerPhone: String? = null,
        open var ownerName: String? = null,
        open var ownerAvatarUrl: String? = null
)