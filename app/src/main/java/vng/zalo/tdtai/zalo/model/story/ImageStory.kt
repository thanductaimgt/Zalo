package vng.zalo.tdtai.zalo.model.story

data class ImageStory(
        override var id: String? = null,
        override var createdTime: Long? = null,
        override var ownerPhone: String? = null,
        override var ownerName: String? = null,
        override var ownerAvatarUrl: String? = null,
        var imageUrl: String? = null
) : Story(id, createdTime, ownerPhone, ownerName, ownerAvatarUrl) {
}