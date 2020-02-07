package vng.zalo.tdtai.zalo.storage

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.*
import vng.zalo.tdtai.zalo.models.message.CallMessage
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.room.*
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils

object FirebaseDatabase {
    private val firebaseFirestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun addNewMessageAndUpdateUsersRoom(context: Context, curRoom: Room, newMessage: Message, callback: (() -> Unit)? = null) {
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
            val lastMsgPreviewContent = newMessage.getPreviewContent(context)

            batch.update(
                    firebaseFirestore
                            .collection(COLLECTION_USERS)
                            .document(curUserPhone)
                            .collection(COLLECTION_ROOMS)
                            .document(curRoom.id!!),
                    HashMap<String, Any?>().apply {
                        put(RoomItem.FIELD_LAST_SENDER_PHONE, newMessage.senderPhone)
                        put(RoomItem.FIELD_LAST_SENDER_NAME, ZaloApplication.curUser!!.name)
                        put(RoomItem.FIELD_LAST_MSG, lastMsgPreviewContent)
                        put(RoomItem.FIELD_LAST_MSG_TIME, newMessage.createdTime)
//                            put(RoomItem.FIELD_LAST_MSG_TYPE, newMessage.type)
                        if (curUserPhone != ZaloApplication.curUser!!.phone &&
                                (newMessage.type != Message.TYPE_CALL ||
                                        newMessage.type == Message.TYPE_CALL && (newMessage as CallMessage).isMissed && curUserPhone != ZaloApplication.curUser!!.phone))
                            put(RoomItem.FIELD_UNSEEN_MSG_NUM, FieldValue.increment(1))
                    }
            )
        }

        batch.update(
                firebaseFirestore
                        .collection(COLLECTION_ROOMS)
                        .document(curRoom.id!!),
                Room.FIELD_SEEN_MEMBERS_PHONE, listOf(ZaloApplication.curUser!!.phone)
        )

        batch.commit().addOnCompleteListener { task ->
            Utils.assertTaskSuccess(task, TAG, "addNewMessageAndUpdateUsersRoom") {
                callback?.invoke()
            }
        }
    }

    fun addRoomAndUserRoom(newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null) {
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
                    if (newRoom.type == Room.TYPE_PEER && it.key != ZaloApplication.curUser!!.phone) {
                        RoomItemPeer(
                                roomId = newRoom.id,
                                avatarUrl = ZaloApplication.curUser!!.avatarUrl,
                                name = ZaloApplication.curUser!!.name,
                                phone = ZaloApplication.curUser!!.phone
                        ).toMap()
                    } else {
                        newRoomItem.toMap()
                    }
            )
        }

        batch.commit().addOnCompleteListener { task ->
            Utils.assertTaskSuccess(task, TAG, "addRoomAndUserRoom") {
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

    fun getMessages(roomId: String, upperCreatedTimeLimit: Timestamp? = null, numberLimit:Long, fieldToOrder: String = Message.FIELD_CREATED_TIME, orderDirection: Query.Direction = Query.Direction.DESCENDING, callback: ((messages: List<Message>) -> Unit)? = null){
        getMessagesQuery(roomId, fieldToOrder, orderDirection)
        .let { if (upperCreatedTimeLimit != null) it.whereLessThan(Message.FIELD_CREATED_TIME, upperCreatedTimeLimit) else it }
                .limit(numberLimit)
                .get().addOnCompleteListener {task->
                    Utils.assertTaskSuccessAndResultNotNull(task, TAG, "getMessages"){querySnapshot->
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

    fun addRoomMessagesListener(roomId: String, lowerCreatedTimeLimit: Timestamp, fieldToOrder: String = Message.FIELD_CREATED_TIME, orderDirection: Query.Direction = Query.Direction.DESCENDING, callback: ((messages: List<Message>) -> Unit)? = null): ListenerRegistration {
        return getMessagesQuery(roomId, fieldToOrder, orderDirection)
                .whereGreaterThanOrEqualTo(Message.FIELD_CREATED_TIME, lowerCreatedTimeLimit)
                .addSnapshotListener { querySnapshot, _ ->
                    Utils.assertNotNull(querySnapshot, TAG, "addRoomMessagesListener") { querySnapshotNotNull ->
                        callback?.invoke(parseMessagesFromQuerySnapshot(querySnapshotNotNull))
                    }
                }
    }

    fun addUserRoomChangeListener(userPhone: String, roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration {
        val curRoomRef = firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .document(roomId)

        return curRoomRef.addSnapshotListener { documentSnapshot, _ ->
            Utils.assertNotNull(documentSnapshot, TAG, "addUserRoomChangeListener") { documentSnapshotNotNull ->
                callback?.invoke(documentSnapshotNotNull)
            }
        }
    }

    fun addUserRoomsListener(userPhone: String, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                .addSnapshotListener { querySnapshot, _ ->
                    Utils.assertNotNull(querySnapshot, TAG, "addUserRoomsListener") { querySnapshotNotNull ->
                        val roomItems = ArrayList<RoomItem>()
                        for (doc in querySnapshotNotNull) {
                            val roomItem = RoomItem.fromDoc(doc)

                            roomItems.add(roomItem)
                        }
                        callback?.invoke(roomItems)
                    }
                }
    }

    fun validateLoginInfo(phone: String, password: String, callback: (user: User?) -> Unit) {
        getUser(phone, callback)
    }

    fun getNewRoomId(): String {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document().id
    }

    fun getNewMessageId(roomId: String): String {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .collection(COLLECTION_MESSAGES)
                .document().id
    }

    fun getRoomInfo(roomId: String, callback: ((room: Room) -> Unit)? = null) {
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
                    Utils.assertTaskSuccessAndResultNotNull(getRoomTask, TAG, "getRoomInfo.getRoomTask") { result1 ->
                        val roomType = result1.getLong(Room.FIELD_TYPE)!!.toInt()
                        val room = if (roomType == Room.TYPE_PEER) {
                            RoomPeer()
                        } else {
                            RoomGroup()
                        }

                        room.id = roomId
                        room.createdTime = (getRoomTask.result as DocumentSnapshot).getTimestamp(Room.FIELD_CREATED_TIME)

                        Utils.assertTaskSuccessAndResultNotNull(getRoomMembersTask, TAG, "getRoomInfo.getRoomMembersTask") { result2 ->
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

    fun getUserRoomPeer(phone: String, callback: (roomItem: RoomItem) -> Unit) {
        //current room reference
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(ZaloApplication.curUser!!.phone!!)
                .collection(COLLECTION_ROOMS)
                .whereEqualTo(RoomItemPeer.FIELD_PHONE, phone)
                .get()
                .addOnCompleteListener {
                    Utils.assertTaskSuccessAndResultNotNull(it, TAG, "getUserRoomPeer") { querySnapshot ->
                        querySnapshot.forEach { documentSnapshot ->
                            val roomItem = RoomItem.fromDoc(documentSnapshot)

                            //set livedata value
                            callback(roomItem)
                        }
                    }
                }
    }

    fun addRoomInfoChangeListener(roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .addSnapshotListener { documentSnapshot, _ ->
                    Utils.assertNotNull(documentSnapshot, TAG, "addRoomInfoChangeListener") { documentSnapshotNotNull ->
                        callback?.invoke(documentSnapshotNotNull)
                    }
                }
    }

    fun addRoomsTypingChangeListener(roomsId: List<String>, callback: ((typingPhoneMap: HashMap<String, RoomMember?>) -> Unit)? = null): ListenerRegistration? {
        return if (roomsId.isNotEmpty()) {
            firebaseFirestore
                    .collection(COLLECTION_ROOMS)
                    .whereIn(FieldPath.documentId(), roomsId.take(10))
                    .addSnapshotListener { querySnapshot, _ ->
                        Utils.assertNotNull(querySnapshot, TAG, "addRoomsTypingChangeListener") { querySnapshotNotNull ->
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

    fun getUserRooms(userPhone: String, roomType: Int? = null, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .let { if (roomType != null) it.whereEqualTo(RoomItem.FIELD_ROOM_TYPE, roomType) else it }
                .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                .get()
                .addOnCompleteListener { task ->
                    Utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUserRooms") { result ->
                        val roomItems = ArrayList<RoomItem>()
                        for (doc in result) {
                            roomItems.add(RoomItem.fromDoc(doc))
                        }
                        callback?.invoke(roomItems)
                    }
                }
    }

    fun getUserStickerSetItems(userPhone: String, callback: ((stickerSets: List<StickerSetItem>) -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_STICKER_SETS)
                .get().addOnCompleteListener { task ->
                    Utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUserStickerSetItems") { result ->
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

    fun updateUserRoom(userPhone: String, roomId: String, fieldsAndValues: Map<String, Any>, callback: (() -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(fieldsAndValues)
                .addOnCompleteListener { task ->
                    Utils.assertTaskSuccess(task, TAG, "updateUserRoom") {
                        callback?.invoke()
                    }
                }
    }

    fun addCurUserToRoomSeenMembers(roomId: String, callback: (() -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_SEEN_MEMBERS_PHONE, FieldValue.arrayUnion(ZaloApplication.curUser!!.phone))
                .addOnCompleteListener { task ->
                    Utils.assertTaskSuccess(task, TAG, "addCurUserToRoomSeenMembers") {
                        callback?.invoke()
                    }
                }
    }

    fun addStickerSet(name: String, bucketName: String, stickerUrls: List<String>, callback: ((stickerSet: StickerSet) -> Unit)? = null) {
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
            Utils.assertTaskSuccess(task, TAG, "addStickerSet") {
                callback?.invoke(stickerSet)
            }
        }
    }

    fun getStickerSet(bucketName: String, callback: (stickerSet: StickerSet) -> Unit) {
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

                    Utils.assertTaskSuccessAndResultNotNull(getStickerSetTask, TAG, "getStickerSet.getStickerSetTask") { stickerSetResult ->
                        stickerSet = stickerSetResult.toObject(StickerSet::class.java)!!

                        Utils.assertTaskSuccessAndResultNotNull(getStickersTask, TAG, "getStickerSet.getStickersTask") { stickersResult ->
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

    fun addCurUserToRoomTypings(roomId: String) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_TYPING_MEMBERS, FieldValue.arrayUnion(
                        ZaloApplication.curUser!!.let {
                            RoomMember(
                                    phone = it.phone, avatarUrl = it.avatarUrl, name = it.name
                            ).toMap()
                        }))
                .addOnCompleteListener {
                    Utils.assertTaskSuccess(
                            it,
                            TAG,
                            "addCurUserToRoomTypings"
                    )
                }
    }

    fun removeCurUserFromRoomTypings(roomId: String) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_TYPING_MEMBERS, FieldValue.arrayRemove(
                        ZaloApplication.curUser!!.let {
                            RoomMember(
                                    phone = it.phone, avatarUrl = it.avatarUrl, name = it.name
                            ).toMap()
                        }))
                .addOnCompleteListener {
                    Utils.assertTaskSuccess(
                            it,
                            TAG,
                            "removeCurUserFromRoomTypings"
                    )
                }
    }

    //no effect if isOnline not change
    fun setCurrentUserOnlineState(isOnline: Boolean) {
        if (ZaloApplication.isOnline != isOnline) {
            firebaseFirestore
                    .collection(COLLECTION_USERS)
                    .document(ZaloApplication.curUser!!.phone!!)
                    .update(User.FIELD_LAST_ONLINE_TIME,
                            if (isOnline) null
                            else Timestamp.now()
                    ).addOnCompleteListener {
                        Utils.assertTaskSuccess(
                                it,
                                TAG,
                                "setCurrentUserOnlineState"
                        )
                    }

            ZaloApplication.isOnline = isOnline
        }
    }

    //null->isOnline
    fun addUsersLastOnlineTimeListener(phoneRoomIdMap: HashMap<String, String>, callback: (usersLastOnlineTime: HashMap<String, Timestamp?>) -> Unit): ListenerRegistration? {
        return if (phoneRoomIdMap.isNotEmpty()) {
            firebaseFirestore
                    .collection(COLLECTION_USERS)
                    .whereIn(FieldPath.documentId(), phoneRoomIdMap.keys.take(10))
                    .addSnapshotListener { querySnapshot, _ ->
                        Utils.assertNotNull(querySnapshot, TAG, "addUsersLastOnlineTimeListener") { querySnapshotNotNull ->
                            val res = HashMap<String, Timestamp?>()
                            for (doc in querySnapshotNotNull) {
                                val phone = doc.id
                                val roomId = phoneRoomIdMap[phone]!!
                                res[roomId] = doc.getTimestamp(User.FIELD_LAST_ONLINE_TIME)
                            }
                            callback(res)
                        }
                    }
        } else {
            null
        }
    }

    fun addUserLastOnlineTimeListener(userPhone: String, callback: (userLastOnlineTime: Timestamp?) -> Unit): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userPhone)
                .addSnapshotListener { documentSnapshot, _ ->
                    Utils.assertNotNull(documentSnapshot, TAG, "addUserLastOnlineTimeListener") { documentSnapshotNotNull ->
                        callback(documentSnapshotNotNull.getTimestamp(User.FIELD_LAST_ONLINE_TIME))
                    }
                }
    }

    fun deleteMessageFromFirestore(roomId: String, messageId: String, onSuccess: (() -> Unit)? = null) {
        firebaseFirestore.collection(COLLECTION_ROOMS)
                .document(roomId)
                .collection(COLLECTION_MESSAGES)
                .document(messageId)
                .delete()
                .addOnCompleteListener {
                    Utils.assertTaskSuccess(it, TAG, "deleteMessageFromFirestore") {
                        onSuccess?.invoke()
                    }
                }
    }

    fun addFriend(phone: String, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        firebaseFirestore.collection(COLLECTION_USERS)
                .document(phone)
                .get()
                .addOnCompleteListener {
                    Utils.assertTaskSuccessAndResultNotNull(it, TAG, "addFriend") { doc ->
                        val avatarUrl = doc.getString(User.FIELD_AVATAR_URL)
                        val name = doc.getString(User.FIELD_NAME)

                        if (name != null) {
                            val curTimestamp = Timestamp.now()

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

                                        put(ZaloApplication.curUser!!.phone!!,
                                                RoomMember(
                                                        avatarUrl = ZaloApplication.curUser!!.avatarUrl,
                                                        joinDate = curTimestamp,
                                                        phone = ZaloApplication.curUser!!.phone,
                                                        name = ZaloApplication.curUser!!.name
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

    fun deleteRoom(room: Room, membersPhone: List<String>, callback: (() -> Unit)? = null) {
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
            addAll(FirebaseStorage.deleteRoomAvatarAndData(room))
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
                    Utils.assertTaskSuccess(task, TAG, "assertLoginSuccess") { doc ->
                        if (doc != null) {
                            val user = User.fromDoc(doc)
                            callback(user)
                        } else {
                            callback(null)
                        }
                    }
                }
    }

    fun getUserAvatarUrl(phone: String, callback: ((avatarUrl: String) -> Unit)? = null) {
        getUser(phone) { user ->
            user?.avatarUrl?.let {
                callback?.invoke(user.avatarUrl!!)
            }
        }
    }

    private const val COLLECTION_USERS = "users"
    private const val COLLECTION_ROOMS = "rooms"
    private const val COLLECTION_MESSAGES = "messages"
    private const val COLLECTION_MEMBERS = "members"
    private const val COLLECTION_CONTACTS = "contacts"
    private const val COLLECTION_OFFICIAL_ACCOUNTS = "official_accounts"
    private const val COLLECTION_STICKER_SETS = "sticker_sets"
    private const val COLLECTION_STICKERS = "stickers"
}