package vng.zalo.tdtai.zalo.repo

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.model.*
import vng.zalo.tdtai.zalo.model.message.CallMessage
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.model.room.*
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject

//@Singleton
class FirebaseDatabase @Inject constructor(
        private val application: ZaloApplication,
        private val sessionManager: SessionManager,
        private val storage: Storage,
        private val utils: Utils
):Database {
    private val firebaseFirestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    override fun addNewMessageAndUpdateUsersRoom(curRoom: Room, newMessage: Message, callback: (() -> Unit)?) {
        val batch = firebaseFirestore.batch()

        //create message
        batch.set(
                firebaseFirestore
                        .collection(COLLECTION_ROOMS)
                        .document(curRoom.id!!)
                        .collection(COLLECTION_MESSAGES)
                        .document(newMessage.id!!),
                newMessage.toMap()
        )

        //for each user rooms, update fields
        curRoom.memberMap!!.forEach {
            val curUserPhone = it.key
            val lastMsgPreviewContent = newMessage.getPreviewContent(application)

            batch.update(
                    firebaseFirestore
                            .collection(COLLECTION_USERS)
                            .document(curUserPhone)
                            .collection(COLLECTION_ROOMS)
                            .document(curRoom.id!!),
                    HashMap<String, Any?>().apply {
                        put(RoomItem.FIELD_LAST_SENDER_PHONE, newMessage.senderPhone)
                        put(RoomItem.FIELD_LAST_SENDER_NAME, sessionManager.curUser!!.name)
                        put(RoomItem.FIELD_LAST_MSG, lastMsgPreviewContent)
                        put(RoomItem.FIELD_LAST_MSG_TIME, newMessage.createdTime)
//                            put(RoomItem.FIELD_LAST_MSG_TYPE, newMessage.type)
                        if (curUserPhone != sessionManager.curUser!!.phone &&
                                (newMessage.type != Message.TYPE_CALL ||
                                        newMessage.type == Message.TYPE_CALL && (newMessage as CallMessage).isMissed && curUserPhone != sessionManager.curUser!!.phone))
                            put(RoomItem.FIELD_UNSEEN_MSG_NUM, FieldValue.increment(1))
                    }
            )
        }

        batch.update(
                firebaseFirestore
                        .collection(COLLECTION_ROOMS)
                        .document(curRoom.id!!),
                Room.FIELD_SEEN_MEMBERS_PHONE, listOf(sessionManager.curUser!!.phone)
        )

        batch.commit().addOnCompleteListener { task ->
            utils.assertTaskSuccess(task, TAG, "addNewMessageAndUpdateUsersRoom") {
                callback?.invoke()
            }
        }
    }

    override fun addRoomAndUserRoom(newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)?) {
        // generate roomId if it's not generated
        if (newRoom.id == null) {
            newRoom.id = getNewRoomId()
        }

        val batch = firebaseFirestore.batch()

        val newRoomDocRef = firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(newRoom.id!!)

        // create room
        batch.set(
                newRoomDocRef,
                newRoom.toMap()
        )

        // user room are the same for all members when room created
        val newRoomItem: RoomItem = if (newRoom is RoomPeer) {
            RoomItemPeer(
                    roomId = newRoom.id,
                    avatarUrl = newRoom.avatarUrl,
                    name = newRoom.name,
                    phone = newRoom.phone
            )
        } else {
            RoomItemGroup(
                    roomId = newRoom.id,
                    avatarUrl = newRoom.avatarUrl,
                    name = newRoom.name
            )
        }

        // in room, create each room member and
        // for each member of room, create user room
        newRoom.memberMap!!.forEach {
            // create room member
            batch.set(
                    newRoomDocRef.collection(COLLECTION_MEMBERS).document(),
                    it.value.toMap()
            )

            //create user room
            val newRoomItemRef = firebaseFirestore
                    .collection(COLLECTION_USERS)
                    .document(it.key)
                    .collection(COLLECTION_ROOMS)
                    .document(newRoom.id!!)

            batch.set(
                    newRoomItemRef,
                    if (newRoom.type == Room.TYPE_PEER && it.key != sessionManager.curUser!!.phone) {
                        RoomItemPeer(
                                roomId = newRoom.id,
                                avatarUrl = sessionManager.curUser!!.avatarUrl,
                                name = sessionManager.curUser!!.name,
                                phone = sessionManager.curUser!!.phone
                        ).toMap()
                    } else {
                        newRoomItem.toMap()
                    }
            )
        }

        batch.commit().addOnCompleteListener { task ->
            utils.assertTaskSuccess(task, TAG, "addRoomAndUserRoom") {
                callback?.invoke(newRoomItem)
            }
        }
    }

    private fun getMessagesQuery(roomId: String, fieldToOrder: String = Message.FIELD_CREATED_TIME, orderDirection: Query.Direction = Query.Direction.DESCENDING):Query{
        return firebaseFirestore
        .collection(COLLECTION_ROOMS)
        .document(roomId)
        .collection(COLLECTION_MESSAGES)
                .orderBy(fieldToOrder, orderDirection)
    }

    override fun getMessages(roomId: String, upperCreatedTimeLimit: Long?, numberLimit:Long, fieldToOrder: String, orderDirection: Query.Direction, callback: ((messages: List<Message>) -> Unit)?){
        getMessagesQuery(roomId, fieldToOrder, orderDirection)
        .let { if (upperCreatedTimeLimit != null) it.whereLessThan(Message.FIELD_CREATED_TIME, upperCreatedTimeLimit) else it }
                .limit(numberLimit)
                .get().addOnCompleteListener {task->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getMessages"){querySnapshot->
                        callback?.invoke(parseMessagesFromQuerySnapshot(querySnapshot))
                    }
                }
    }

    private fun parseMessagesFromQuerySnapshot(querySnapshot: QuerySnapshot):List<Message>{
        val messages = ArrayList<Message>()
        for (doc in querySnapshot) {
            val message = Message.fromDoc(doc)
            messages.add(message)
        }
        return messages
    }

    override fun addRoomMessagesListener(roomId: String, lowerCreatedTimeLimit: Long, fieldToOrder: String, orderDirection: Query.Direction, callback: ((messages: List<Message>) -> Unit)?): ListenerRegistration {
        return getMessagesQuery(roomId, fieldToOrder, orderDirection)
                .whereGreaterThanOrEqualTo(Message.FIELD_CREATED_TIME, lowerCreatedTimeLimit)
                .addSnapshotListener { querySnapshot, _ ->
                    utils.assertNotNull(querySnapshot, TAG, "addRoomMessagesListener") { querySnapshotNotNull ->
                        callback?.invoke(parseMessagesFromQuerySnapshot(querySnapshotNotNull))
                    }
                }
    }

    override fun addUserRoomChangeListener(userPhone: String, roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)?): ListenerRegistration {
        val curRoomRef = firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .document(roomId)

        return curRoomRef.addSnapshotListener { documentSnapshot, _ ->
            utils.assertNotNull(documentSnapshot, TAG, "addUserRoomChangeListener") { documentSnapshotNotNull ->
                callback?.invoke(documentSnapshotNotNull)
            }
        }
    }

    override fun addUserRoomsListener(userPhone: String, fieldToOrder: String?, orderDirection: Query.Direction, callback: ((roomItems: List<RoomItem>) -> Unit)?): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                .addSnapshotListener { querySnapshot, _ ->
                    utils.assertNotNull(querySnapshot, TAG, "addUserRoomsListener") { querySnapshotNotNull ->
                        val roomItems = ArrayList<RoomItem>()
                        for (doc in querySnapshotNotNull) {
                            val roomItem = RoomItem.fromDoc(doc)

                            roomItems.add(roomItem)
                        }
                        callback?.invoke(roomItems)
                    }
                }
    }

    override fun validateLoginInfo(phone: String, password: String, callback: (user: User?) -> Unit) {
        getUser(phone, callback)
    }

    override fun getNewRoomId(): String {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document().id
    }

    override fun getNewMessageId(roomId: String): String {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .collection(COLLECTION_MESSAGES)
                .document().id
    }

    override fun getRoomInfo(roomId: String, callback: ((room: Room) -> Unit)?) {
        //current room reference
        val curRoomDocRef = firebaseFirestore
                .collection(COLLECTION_ROOMS)
//                    .whereArrayContains(FieldPath.of("members","phone"), ZaloApplication.currentUser)
                .document(roomId)

        val tasks: ArrayList<Task<*>> = ArrayList()

        //get room
        val getRoomTask = curRoomDocRef.get()
        tasks.add(getRoomTask)

        //get room members
        val getRoomMembersTask = curRoomDocRef.collection(COLLECTION_MEMBERS).get()
        tasks.add(getRoomMembersTask)

        // add callback when all tasks done
        Tasks.whenAll(tasks)
                .addOnCompleteListener {
                    utils.assertTaskSuccessAndResultNotNull(getRoomTask, TAG, "getRoomInfo.getRoomTask") { result1 ->
                        val roomType = result1.getLong(Room.FIELD_TYPE)!!.toInt()
                        val room = if (roomType == Room.TYPE_PEER) {
                            RoomPeer()
                        } else {
                            RoomGroup()
                        }

                        room.id = roomId
                        room.createdTime = (getRoomTask.result as DocumentSnapshot).getLong(Room.FIELD_CREATED_TIME)

                        utils.assertTaskSuccessAndResultNotNull(getRoomMembersTask, TAG, "getRoomInfo.getRoomMembersTask") { result2 ->
                            val memberMap = HashMap<String, RoomMember>()
                            for (doc in (result2 as QuerySnapshot)) {
                                val roomMember = RoomMember.fromDoc(doc)

                                memberMap[doc.getString(RoomMember.FIELD_PHONE)!!] = roomMember
                            }
                            room.memberMap = memberMap

                            //set livedata value
                            callback?.invoke(room)
                        }
                    }
                }
    }

    override fun getUserRoomPeer(phone: String, callback: (roomItemPeer: RoomItemPeer) -> Unit) {
        //current room reference
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.phone!!)
                .collection(COLLECTION_ROOMS)
                .whereEqualTo(RoomItemPeer.FIELD_PHONE, phone)
                .get()
                .addOnCompleteListener {
                    utils.assertTaskSuccessAndResultNotNull(it, TAG, "getUserRoomPeer") { querySnapshot ->
                        querySnapshot.forEach { documentSnapshot ->
                            val roomItemPeer = RoomItem.fromDoc(documentSnapshot) as RoomItemPeer

                            //set livedata value
                            callback(roomItemPeer)
                        }
                    }
                }
    }

    override fun addRoomInfoChangeListener(roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)?): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .addSnapshotListener { documentSnapshot, _ ->
                    utils.assertNotNull(documentSnapshot, TAG, "addRoomInfoChangeListener") { documentSnapshotNotNull ->
                        callback?.invoke(documentSnapshotNotNull)
                    }
                }
    }

    override fun addRoomsTypingChangeListener(roomsId: List<String>, callback: ((typingPhoneMap: HashMap<String, RoomMember?>) -> Unit)?): ListenerRegistration? {
        return if (roomsId.isNotEmpty()) {
            firebaseFirestore
                    .collection(COLLECTION_ROOMS)
                    .whereIn(FieldPath.documentId(), roomsId.take(10))
                    .addSnapshotListener { querySnapshot, _ ->
                        utils.assertNotNull(querySnapshot, TAG, "addRoomsTypingChangeListener") { querySnapshotNotNull ->
                            val res = HashMap<String, RoomMember?>()
                            querySnapshotNotNull.forEach { doc ->
                                val roomId = doc.id
                                val lastTypingMember = (doc.get(Room.FIELD_TYPING_MEMBERS) as List<*>?)?.lastOrNull()?.let { RoomMember.fromObject(it) }
                                res[roomId] = lastTypingMember
                            }
                            callback?.invoke(res)
                        }
                    }
        } else {
            null
        }
    }

    override fun getUserRooms(userPhone: String, roomType: Int?, fieldToOrder: String?, orderDirection: Query.Direction, callback: ((roomItems: List<RoomItem>) -> Unit)?) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .let { if (roomType != null) it.whereEqualTo(RoomItem.FIELD_ROOM_TYPE, roomType) else it }
                .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                .get()
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUserRooms") { result ->
                        val roomItems = ArrayList<RoomItem>()
                        for (doc in result) {
                            roomItems.add(RoomItem.fromDoc(doc))
                        }
                        callback?.invoke(roomItems)
                    }
                }
    }

    override fun getUserStickerSetItems(userPhone: String, callback: ((stickerSets: List<StickerSetItem>) -> Unit)?) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_STICKER_SETS)
                .get().addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUserStickerSetItems") { result ->
                        val stickerSetItems = ArrayList<StickerSetItem>()
                        for (doc in result) {
                            stickerSetItems.add(
                                    doc.toObject(StickerSetItem::class.java)
                            )
                        }
                        callback?.invoke(stickerSetItems)
                    }
                }
    }

    override fun updateUserRoom(userPhone: String, roomId: String, fieldsAndValues: Map<String, Any>, callback: (() -> Unit)?) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(fieldsAndValues)
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccess(task, TAG, "updateUserRoom") {
                        callback?.invoke()
                    }
                }
    }

    override fun addCurUserToRoomSeenMembers(roomId: String, callback: (() -> Unit)?) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_SEEN_MEMBERS_PHONE, FieldValue.arrayUnion(sessionManager.curUser!!.phone))
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccess(task, TAG, "addCurUserToRoomSeenMembers") {
                        callback?.invoke()
                    }
                }
    }

    override fun addStickerSet(name: String, bucketName: String, stickerUrls: List<String>, callback: ((stickerSet: StickerSet) -> Unit)?) {
        val stickerSetRef = firebaseFirestore
                .collection(COLLECTION_STICKER_SETS)
                .document(bucketName)

        val batch = firebaseFirestore.batch()

        val stickers = ArrayList<Sticker>()
        stickerUrls.forEach { stickerUrl ->
            val newDocRef = stickerSetRef
                    .collection(COLLECTION_STICKERS)
                    .document()

            val sticker = Sticker(
                    id = newDocRef.id,
                    url = stickerUrl
            )

            batch.set(newDocRef, sticker.toMap())

            stickers.add(sticker)
        }

        val stickerSet = StickerSet(
                bucketName = bucketName,
                name = name,
                stickers = stickers
        )

        batch.set(
                stickerSetRef,
                stickerSet.toMap()
        )

        batch.commit().addOnCompleteListener { task ->
            utils.assertTaskSuccess(task, TAG, "addStickerSet") {
                callback?.invoke(stickerSet)
            }
        }
    }

    override fun getStickerSet(bucketName: String, callback: (stickerSet: StickerSet) -> Unit) {
        val stickerSetDocRef = firebaseFirestore
                .collection(COLLECTION_STICKER_SETS)
                .document(bucketName)
        Log.d(TAG, "getStickerSet." + stickerSetDocRef.path)

        val tasks = ArrayList<Task<*>>()

        val getStickerSetTask = stickerSetDocRef.get()
        tasks.add(getStickerSetTask)

        val getStickersTask = stickerSetDocRef
                .collection(COLLECTION_STICKERS)
                .get()
        tasks.add(getStickersTask)

        Tasks.whenAll(tasks)
                .addOnCompleteListener {
                    var stickerSet: StickerSet

                    utils.assertTaskSuccessAndResultNotNull(getStickerSetTask, TAG, "getStickerSet.getStickerSetTask") { stickerSetResult ->
                        stickerSet = stickerSetResult.toObject(StickerSet::class.java)!!

                        utils.assertTaskSuccessAndResultNotNull(getStickersTask, TAG, "getStickerSet.getStickersTask") { stickersResult ->
                            val stickers = ArrayList<Sticker>()
                            stickersResult.forEach {
                                stickers.add(
                                        it.toObject(Sticker::class.java).apply {
                                            id = it.id
                                        }
                                )
                            }
                            stickerSet.stickers = stickers

                            callback(stickerSet)
                        }
                    }
                }
    }

    override fun addCurUserToRoomTypings(roomId: String) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_TYPING_MEMBERS, FieldValue.arrayUnion(
                        sessionManager.curUser!!.let {
                            RoomMember(
                                    phone = it.phone, avatarUrl = it.avatarUrl, name = it.name
                            ).toMap()
                        }))
                .addOnCompleteListener {
                    utils.assertTaskSuccess(
                            it,
                            TAG,
                            "addCurUserToRoomTypings"
                    )
                }
    }

    override fun removeCurUserFromRoomTypings(roomId: String) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_TYPING_MEMBERS, FieldValue.arrayRemove(
                        sessionManager.curUser!!.let {
                            RoomMember(
                                    phone = it.phone, avatarUrl = it.avatarUrl, name = it.name
                            ).toMap()
                        }))
                .addOnCompleteListener {
                    utils.assertTaskSuccess(
                            it,
                            TAG,
                            "removeCurUserFromRoomTypings"
                    )
                }
    }

    //no effect if isOnline not change
    override fun setCurrentUserOnlineState(isOnline: Boolean) {
        if (sessionManager.isOnline != isOnline) {
            firebaseFirestore
                    .collection(COLLECTION_USERS)
                    .document(sessionManager.curUser!!.phone!!)
                    .update(User.FIELD_LAST_ONLINE_TIME,
                            if (isOnline) null
                            else System.currentTimeMillis()
                    ).addOnCompleteListener {
                        utils.assertTaskSuccess(
                                it,
                                TAG,
                                "setCurrentUserOnlineState"
                        )
                    }

            sessionManager.isOnline = isOnline
        }
    }

    //null->isOnline
    override fun addUsersLastOnlineTimeListener(phoneRoomIdMap: HashMap<String, String>, callback: (usersLastOnlineTime: HashMap<String, Long?>) -> Unit): ListenerRegistration? {
        return if (phoneRoomIdMap.isNotEmpty()) {
            firebaseFirestore
                    .collection(COLLECTION_USERS)
                    .whereIn(FieldPath.documentId(), phoneRoomIdMap.keys.take(10))
                    .addSnapshotListener { querySnapshot, _ ->
                        utils.assertNotNull(querySnapshot, TAG, "addUsersLastOnlineTimeListener") { querySnapshotNotNull ->
                            val res = HashMap<String, Long?>()
                            for (doc in querySnapshotNotNull) {
                                val phone = doc.id
                                val roomId = phoneRoomIdMap[phone]!!
                                res[roomId] = doc.getLong(User.FIELD_LAST_ONLINE_TIME)
                            }
                            callback(res)
                        }
                    }
        } else {
            null
        }
    }

    override fun addUserLastOnlineTimeListener(userPhone: String, callback: (userLastOnlineTime: Long?) -> Unit): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .addSnapshotListener { documentSnapshot, _ ->
                    utils.assertNotNull(documentSnapshot, TAG, "addUserLastOnlineTimeListener") { documentSnapshotNotNull ->
                        callback(documentSnapshotNotNull.getLong(User.FIELD_LAST_ONLINE_TIME))
                    }
                }
    }

    override fun deleteMessageFromFirestore(roomId: String, messageId: String, onSuccess: (() -> Unit)?) {
        firebaseFirestore.collection(COLLECTION_ROOMS)
                .document(roomId)
                .collection(COLLECTION_MESSAGES)
                .document(messageId)
                .delete()
                .addOnCompleteListener {
                    utils.assertTaskSuccess(it, TAG, "deleteMessageFromFirestore") {
                        onSuccess?.invoke()
                    }
                }
    }

    override fun addFriend(phone: String, callback: ((isSuccess: Boolean) -> Unit)?) {
        firebaseFirestore.collection(COLLECTION_USERS)
                .document(phone)
                .get()
                .addOnCompleteListener {
                    utils.assertTaskSuccessAndResultNotNull(it, TAG, "addFriend") { doc ->
                        val avatarUrl = doc.getString(User.FIELD_AVATAR_URL)
                        val name = doc.getString(User.FIELD_NAME)

                        if (name != null) {
                            val curTimestamp = System.currentTimeMillis()

                            val newRoom = RoomPeer(
                                    name = name,
                                    phone = phone,
                                    avatarUrl = avatarUrl,
                                    createdTime = curTimestamp,
                                    memberMap = HashMap<String, RoomMember>().apply {
                                        put(phone, RoomMember(
                                                avatarUrl = avatarUrl,
                                                joinDate = curTimestamp,
                                                phone = phone,
                                                name = name
                                        ))

                                        put(sessionManager.curUser!!.phone!!,
                                                RoomMember(
                                                        avatarUrl = sessionManager.curUser!!.avatarUrl,
                                                        joinDate = curTimestamp,
                                                        phone = sessionManager.curUser!!.phone,
                                                        name = sessionManager.curUser!!.name
                                                ))
                                    }
                            )

                            addRoomAndUserRoom(newRoom) {
                                callback?.invoke(true)
                            }
                        } else {
                            callback?.invoke(false)
                        }
                    }
                }
    }

    override fun deleteRoom(room: Room, membersPhone: List<String>, callback: (() -> Unit)?) {
        val batch = firebaseFirestore.batch()

        batch.delete(
                firebaseFirestore.collection(COLLECTION_ROOMS)
                        .document(room.id!!)
        )

        membersPhone.forEach {
            batch.delete(
                    firebaseFirestore.collection(COLLECTION_USERS)
                            .document(it)
                            .collection(COLLECTION_ROOMS)
                            .document(room.id!!)
            )
        }

        val tasks = ArrayList<Task<Void>>().apply {
            add(batch.commit())
            addAll(storage.deleteRoomAvatarAndData(room))
        }

        Tasks.whenAll(tasks).addOnCompleteListener {
            //            Utils.assertTaskSuccess(it, TAG, "deleteRoom") {
            callback?.invoke()
//            }
        }
    }

    private fun getUser(phone: String, callback: ((user: User?) -> Unit)) {
        firebaseFirestore.collection(COLLECTION_USERS)
                .document(phone)
                .get()
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccess(task, TAG, "assertLoginSuccess") { doc ->
                        if (doc != null) {
                            val user = User.fromDoc(doc)
                            callback(user)
                        } else {
                            callback(null)
                        }
                    }
                }
    }

    override fun getUserAvatarUrl(phone: String, callback: ((avatarUrl: String) -> Unit)?) {
        getUser(phone) { user ->
            user?.avatarUrl?.let {
                callback?.invoke(user.avatarUrl!!)
            }
        }
    }

    override fun updateFirebaseMessagingToken(oldToken: String?, newToken: String) {
        val docRef = firebaseFirestore.collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.phone!!)

        firebaseFirestore.batch().apply {
            oldToken?.let {
                update(docRef, hashMapOf<String, Any>(
                        User.FIELD_FIREBASE_MESSAGING_TOKEN to FieldValue.arrayRemove(oldToken)
                ))
            }

            update(docRef, hashMapOf<String, Any>(
                    User.FIELD_FIREBASE_MESSAGING_TOKEN to FieldValue.arrayUnion(newToken)
            ))

            commit().addOnCompleteListener {
                utils.assertTaskSuccess(it, TAG, "updateFirebaseMessagingToken")
            }
        }
    }

    companion object{
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_ROOMS = "rooms"
        private const val COLLECTION_MESSAGES = "messages"
        private const val COLLECTION_MEMBERS = "members"
        private const val COLLECTION_CONTACTS = "contacts"
        private const val COLLECTION_OFFICIAL_ACCOUNTS = "official_accounts"
        private const val COLLECTION_STICKER_SETS = "sticker_sets"
        private const val COLLECTION_STICKERS = "stickers"
    }
}