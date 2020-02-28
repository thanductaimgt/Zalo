package vng.zalo.tdtai.zalo.repo

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import vng.zalo.tdtai.zalo.model.RoomMember
import vng.zalo.tdtai.zalo.model.StickerSet
import vng.zalo.tdtai.zalo.model.StickerSetItem
import vng.zalo.tdtai.zalo.model.User
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.model.room.RoomItemPeer
import javax.inject.Singleton

@Singleton
interface Database {
    fun addNewMessageAndUpdateUsersRoom(curRoom: Room, newMessage: Message, callback: (() -> Unit)? = null)

    fun addRoomAndUserRoom(newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null)

    fun getMessages(roomId: String, upperCreatedTimeLimit: Long? = null, numberLimit:Long, fieldToOrder: String = Message.FIELD_CREATED_TIME, orderDirection: Query.Direction = Query.Direction.DESCENDING, callback: ((messages: List<Message>) -> Unit)? = null)

    fun addRoomMessagesListener(roomId: String, lowerCreatedTimeLimit: Long, fieldToOrder: String = Message.FIELD_CREATED_TIME, orderDirection: Query.Direction = Query.Direction.DESCENDING, callback: ((messages: List<Message>) -> Unit)? = null): ListenerRegistration

    fun addUserRoomChangeListener(userPhone: String, roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration

    fun addUserRoomsListener(userPhone: String, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null): ListenerRegistration

    fun validateLoginInfo(phone: String, password: String, callback: (user: User?) -> Unit)

    fun getNewRoomId(): String

    fun getNewMessageId(roomId: String): String

    fun getRoomInfo(roomId: String, callback: ((room: Room) -> Unit)? = null)

    fun getUserRoomPeer(phone: String, callback: (roomItemPeer: RoomItemPeer) -> Unit)

    fun addRoomInfoChangeListener(roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration

    fun addRoomsTypingChangeListener(roomsId: List<String>, callback: ((typingPhoneMap: HashMap<String, RoomMember?>) -> Unit)? = null): ListenerRegistration?

    fun getUserRooms(userPhone: String, roomType: Int? = null, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null)

    fun getUserStickerSetItems(userPhone: String, callback: ((stickerSets: List<StickerSetItem>) -> Unit)? = null)

    fun updateUserRoom(userPhone: String, roomId: String, fieldsAndValues: Map<String, Any>, callback: (() -> Unit)? = null)

    fun addCurUserToRoomSeenMembers(roomId: String, callback: (() -> Unit)? = null)

    fun addStickerSet(name: String, bucketName: String, stickerUrls: List<String>, callback: ((stickerSet: StickerSet) -> Unit)? = null)

    fun getStickerSet(bucketName: String, callback: (stickerSet: StickerSet) -> Unit)

    fun addCurUserToRoomTypings(roomId: String)

    fun removeCurUserFromRoomTypings(roomId: String)

    //no effect if isOnline not change
    fun setCurrentUserOnlineState(isOnline: Boolean)

    //null->isOnline
    fun addUsersLastOnlineTimeListener(phoneRoomIdMap: HashMap<String, String>, callback: (usersLastOnlineTime: HashMap<String, Long?>) -> Unit): ListenerRegistration?

    fun addUserLastOnlineTimeListener(userPhone: String, callback: (userLastOnlineTime: Long?) -> Unit): ListenerRegistration

    fun deleteMessageFromFirestore(roomId: String, messageId: String, onSuccess: (() -> Unit)? = null)

    fun addFriend(phone: String, callback: ((isSuccess: Boolean) -> Unit)? = null)

    fun deleteRoom(room: Room, membersPhone: List<String>, callback: (() -> Unit)? = null)

    fun getUserAvatarUrl(phone: String, callback: ((avatarUrl: String) -> Unit)? = null)
}