package vng.zalo.tdtai.zalo.repository

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.data_model.*
import vng.zalo.tdtai.zalo.data_model.message.CallMessage
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.data_model.post.Post
import vng.zalo.tdtai.zalo.data_model.post.Watch
import vng.zalo.tdtai.zalo.data_model.room.*
import vng.zalo.tdtai.zalo.data_model.story.Story
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.manager.SessionManager
import vng.zalo.tdtai.zalo.util.Constants
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.Utils
import vng.zalo.tdtai.zalo.util.whereInSafe
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Database @Inject constructor(
        private val application: ZaloApplication,
        private val sessionManager: SessionManager,
        private val storage: Storage,
        private val utils: Utils
) {
    private val firebaseFirestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    fun addNewMessageAndUpdateUsersRoom(curRoom: Room, newMessage: Message, callback: (() -> Unit)? = null) {
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
            val curUserId = it.key
            val lastMsgPreviewContent = newMessage.getPreviewContent(application)

            batch.update(
                    firebaseFirestore
                            .collection(COLLECTION_USERS)
                            .document(curUserId)
                            .collection(COLLECTION_ROOMS)
                            .document(curRoom.id!!),
                    HashMap<String, Any?>().apply {
                        put(RoomItem.FIELD_LAST_SENDER_ID, newMessage.senderId)
                        put(RoomItem.FIELD_LAST_SENDER_NAME, sessionManager.curUser!!.name)
                        put(RoomItem.FIELD_LAST_MSG, lastMsgPreviewContent)
                        put(RoomItem.FIELD_LAST_MSG_TIME, newMessage.createdTime)
//                            put(RoomItem.FIELD_LAST_MSG_TYPE, newMessage.type)
                        if (curUserId != sessionManager.curUser!!.id &&
                                (newMessage.type != Message.TYPE_CALL ||
                                        newMessage.type == Message.TYPE_CALL && (newMessage as CallMessage).isMissed && curUserId != sessionManager.curUser!!.id))
                            put(RoomItem.FIELD_UNSEEN_MSG_NUM, FieldValue.increment(1))
                    }
            )
        }

        batch.update(
                firebaseFirestore
                        .collection(COLLECTION_ROOMS)
                        .document(curRoom.id!!),
                Room.FIELD_SEEN_MEMBERS_ID, listOf(sessionManager.curUser!!.id)
        )

        batch.commit().addOnCompleteListener { task ->
            utils.assertTaskSuccess(task, TAG, "addNewMessageAndUpdateUsersRoom") {
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
                    peerId = newRoom.peerId
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
                    if (newRoom.type == Room.TYPE_PEER && it.key != sessionManager.curUser!!.id) {
                        RoomItemPeer(
                                roomId = newRoom.id,
                                avatarUrl = sessionManager.curUser!!.avatarUrl,
                                name = sessionManager.curUser!!.name,
                                peerId = sessionManager.curUser!!.id
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

    private fun getMessagesQuery(roomId: String): Query {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .collection(COLLECTION_MESSAGES)
    }

    fun getMessages(roomId: String, upperCreatedTimeLimit: Long?, numberLimit: Long, callback: ((messages: List<Message>) -> Unit)? = null) {
        getMessagesQuery(roomId)
                .let { if (upperCreatedTimeLimit != null) it.whereLessThan(Message.FIELD_CREATED_TIME, upperCreatedTimeLimit) else it }
                .orderBy(Message.FIELD_CREATED_TIME, Query.Direction.DESCENDING)
                .limit(numberLimit)
                .get().addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getMessages") { querySnapshot ->
                        callback?.invoke(parseMessagesFromQuerySnapshot(querySnapshot))
                    }
                }
    }

    private fun parseMessagesFromQuerySnapshot(querySnapshot: QuerySnapshot): List<Message> {
        val messages = ArrayList<Message>()
        for (doc in querySnapshot) {
            val message = Message.fromDoc(doc)
            messages.add(message)
        }
        return messages
    }

    fun addRoomMessagesListener(roomId: String, lowerCreatedTimeLimit: Long, callback: ((messages: List<Message>) -> Unit)? = null): ListenerRegistration {
        return getMessagesQuery(roomId)
                .whereGreaterThanOrEqualTo(Message.FIELD_CREATED_TIME, lowerCreatedTimeLimit)
                .addSnapshotListener { querySnapshot, _ ->
                    utils.assertNotNull(querySnapshot, TAG, "addRoomMessagesListener") { querySnapshotNotNull ->
                        callback?.invoke(parseMessagesFromQuerySnapshot(querySnapshotNotNull))
                    }
                }
    }

    fun addUserRoomChangeListener(userId: String, roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration {
        val curRoomRef = firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_ROOMS)
                .document(roomId)

        return curRoomRef.addSnapshotListener { documentSnapshot, _ ->
            utils.assertNotNull(documentSnapshot, TAG, "addUserRoomChangeListener") { documentSnapshotNotNull ->
                callback?.invoke(documentSnapshotNotNull)
            }
        }
    }

    fun addUserRoomsListener(userId: String, fieldToOrder: String = RoomItem.FIELD_LAST_MSG_TIME, orderDirection: Query.Direction = Query.Direction.DESCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_ROOMS)
                .orderBy(fieldToOrder, orderDirection)
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

    fun validateLoginInfo(userId: String, password: String, callback: (user: User?) -> Unit) {
        getUser(userId, callback)
    }

    fun getRoomInfo(roomId: String, callback: ((room: Room) -> Unit)? = null) {
        //current room reference
        val curRoomDocRef = firebaseFirestore
                .collection(COLLECTION_ROOMS)
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

                                memberMap[doc.getString(RoomMember.FIELD_ID)!!] = roomMember
                            }
                            room.memberMap = memberMap

                            //set livedata value
                            callback?.invoke(room)
                        }
                    }
                }
    }

    fun getUserRoomPeer(userId: String, callback: (roomItemPeer: RoomItemPeer) -> Unit) {
        //current room reference
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.id!!)
                .collection(COLLECTION_ROOMS)
                .whereEqualTo(RoomItemPeer.FIELD_PEER_ID, userId)
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

    fun addRoomInfoChangeListener(roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .addSnapshotListener { documentSnapshot, _ ->
                    utils.assertNotNull(documentSnapshot, TAG, "addRoomInfoChangeListener") { documentSnapshotNotNull ->
                        callback?.invoke(documentSnapshotNotNull)
                    }
                }
    }

    fun addRoomsTypingChangeListener(roomsId: List<String>, callback: ((typingIdMap: HashMap<String, RoomMember?>) -> Unit)? = null): ListenerRegistration? {
        return if (roomsId.isNotEmpty()) {
            firebaseFirestore
                    .collection(COLLECTION_ROOMS)
                    .whereInSafe(FieldPath.documentId(), roomsId)
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

    fun getUserRooms(userId: String, roomType: Int? = null, fieldToOrder: String? = RoomItem.FIELD_LAST_MSG_TIME, orderDirection: Query.Direction = Query.Direction.DESCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userId)
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

    fun getUserStickerSetItems(userId: String, callback: ((stickerSets: List<StickerSetItem>) -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userId)
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

    fun updateUserRoom(userId: String, roomId: String, fieldsAndValues: Map<String, Any>, callback: (() -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(fieldsAndValues)
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccess(task, TAG, "updateUserRoom") {
                        callback?.invoke()
                    }
                }
    }

    fun addCurUserToRoomSeenMembers(roomId: String, callback: (() -> Unit)? = null) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_SEEN_MEMBERS_ID, FieldValue.arrayUnion(sessionManager.curUser!!.id))
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccess(task, TAG, "addCurUserToRoomSeenMembers") {
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
            utils.assertTaskSuccess(task, TAG, "addStickerSet") {
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

                    utils.assertTaskSuccessAndResultNotNull(getStickerSetTask, TAG, "getStickerSet.getStickerSetTask") { stickerSetResult ->
                        stickerSet = stickerSetResult.toObject(StickerSet::class.java)!!

                        utils.assertTaskSuccessAndResultNotNull(getStickersTask, TAG, "getStickerSet.getStickersTask") { stickersResult ->
                            val stickers = ArrayList<Sticker>()
                            stickersResult.forEach {
                                stickers.add(
                                        Sticker.fromDoc(it)
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
                        sessionManager.curUser!!.let {
                            RoomMember(
                                    userId = it.id, avatarUrl = it.avatarUrl, name = it.name
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

    fun removeCurUserFromRoomTypings(roomId: String) {
        firebaseFirestore
                .collection(COLLECTION_ROOMS)
                .document(roomId)
                .update(Room.FIELD_TYPING_MEMBERS, FieldValue.arrayRemove(
                        sessionManager.curUser!!.let {
                            RoomMember(
                                    userId = it.id, avatarUrl = it.avatarUrl, name = it.name
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
    fun setCurrentUserOnlineState(isOnline: Boolean) {
        if (sessionManager.isOnline != isOnline) {
            firebaseFirestore
                    .collection(COLLECTION_USERS)
                    .document(sessionManager.curUser!!.id!!)
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
    fun addUsersLastOnlineTimeListener(userIdRoomIdMap: HashMap<String, String>, callback: (usersLastOnlineTime: HashMap<String, Long?>) -> Unit): ListenerRegistration? {
        return if (userIdRoomIdMap.isNotEmpty()) {
            firebaseFirestore
                    .collection(COLLECTION_USERS)
                    .whereInSafe(FieldPath.documentId(), userIdRoomIdMap.keys.toList())
                    .addSnapshotListener { querySnapshot, _ ->
                        utils.assertNotNull(querySnapshot, TAG, "addUsersLastOnlineTimeListener") { querySnapshotNotNull ->
                            val res = HashMap<String, Long?>()
                            for (doc in querySnapshotNotNull) {
                                val userId = doc.id
                                val roomId = userIdRoomIdMap[userId]!!
                                res[roomId] = doc.getLong(User.FIELD_LAST_ONLINE_TIME)
                            }
                            callback(res)
                        }
                    }
        } else {
            null
        }
    }

    fun addUserLastOnlineTimeListener(userId: String, callback: (userLastOnlineTime: Long?) -> Unit): ListenerRegistration {
        return addUserListener(userId) {
            callback(it.lastOnlineTime)
        }
    }

    fun addUserLastStoryCreatedTimeListener(userId: String, callback: (lastStoryCreatedTime: Long?) -> Unit): ListenerRegistration {
        return addUserListener(userId) {
            callback(it.lastStoryCreatedTime)
        }
    }

    private fun addUserListener(userId: String, callback: (user: User) -> Unit): ListenerRegistration {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(userId)
                .addSnapshotListener { documentSnapshot, _ ->
                    utils.assertNotNull(documentSnapshot, TAG, "addUserLastOnlineTimeListener") { docNotNull ->
                        callback(User.fromDoc(docNotNull))
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
                    utils.assertTaskSuccess(it, TAG, "deleteMessageFromFirestore") {
                        onSuccess?.invoke()
                    }
                }
    }

    fun addFriend(userId: String, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        firebaseFirestore.collection(COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnCompleteListener {
                    utils.assertTaskSuccessAndResultNotNull(it, TAG, "addFriend") { doc ->
                        val avatarUrl = doc.getString(User.FIELD_AVATAR_URL)
                        val name = doc.getString(User.FIELD_NAME)

                        if (name != null) {
                            val curTimestamp = System.currentTimeMillis()

                            val newRoom = RoomPeer(
                                    name = name,
                                    peerId = userId,
                                    avatarUrl = avatarUrl,
                                    createdTime = curTimestamp,
                                    memberMap = HashMap<String, RoomMember>().apply {
                                        put(userId, RoomMember(
                                                avatarUrl = avatarUrl,
                                                joinDate = curTimestamp,
                                                userId = userId,
                                                name = name
                                        ))

                                        put(sessionManager.curUser!!.id!!,
                                                RoomMember(
                                                        avatarUrl = sessionManager.curUser!!.avatarUrl,
                                                        joinDate = curTimestamp,
                                                        userId = sessionManager.curUser!!.id,
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

    fun deleteRoom(room: Room, membersUserId: List<String>, callback: (() -> Unit)? = null) {
        val batch = firebaseFirestore.batch()

        batch.delete(
                firebaseFirestore.collection(COLLECTION_ROOMS)
                        .document(room.id!!)
        )

        membersUserId.forEach {
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

    fun getUser(id: String, callback: ((user: User?) -> Unit)) {
        firebaseFirestore.collection(COLLECTION_USERS)
                .document(id)
                .get()
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccess(task, TAG, "getUser") { doc ->
                        if (doc != null) {
                            val user = User.fromDoc(doc)
                            callback(user)
                        } else {
                            callback(null)
                        }
                    }
                }
    }

    fun getUsers(usersId: List<String>, callback: ((users: ArrayList<User>) -> Unit)) {
        firebaseFirestore.collection(COLLECTION_USERS)
                .whereInSafe(FieldPath.documentId(), usersId)
                .get()
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUsers") { docs ->
                        val users = arrayListOf<User>()
                        docs.forEach {
                            users.add(User.fromDoc(it))
                        }
                        callback(users)
                    }
                }
    }

    fun getUserAvatarUrl(userId: String, callback: ((avatarUrl: String) -> Unit)? = null) {
        getUser(userId) { user ->
            user?.avatarUrl?.let {
                callback?.invoke(user.avatarUrl!!)
            }
        }
    }

//    fun updateFirebaseMessagingToken(oldToken: String?, newToken: String) {
//        val docRef = firebaseFirestore.collection(COLLECTION_USERS)
//                .document(sessionManager.curUser!!.id!!)
//
//        firebaseFirestore.batch().apply {
//            oldToken?.let {
//                update(docRef, hashMapOf<String, Any>(
//                        User.FIELD_FIREBASE_MESSAGING_TOKEN to FieldValue.arrayRemove(oldToken)
//                ))
//            }
//
//            update(docRef, hashMapOf<String, Any>(
//                    User.FIELD_FIREBASE_MESSAGING_TOKEN to FieldValue.arrayUnion(newToken)
//            ))
//
//            commit().addOnCompleteListener {
//                utils.assertTaskSuccess(it, TAG, "updateFirebaseMessagingToken")
//            }
//        }
//    }

    fun createStory(story: Story, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        if (story.id == null) {
            story.id = getNewStoryId()
        }

        val curUserDocRef = firebaseFirestore.collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.id!!)

        firebaseFirestore.batch().apply {
            set(
                    curUserDocRef
                            .collection(COLLECTION_STORIES)
                            .document(story.id!!),
                    story.toMap()
            )

            update(
                    curUserDocRef,
                    mapOf(
                            User.FIELD_LAST_STORY_CREATED_TIME to story.createdTime
                    )
            )

            commit().addOnCompleteListener {
                callback?.invoke(it.isSuccessful)
            }
        }
    }

    fun addStoryToGroup(story: Story, storyGroup: StoryGroup, alsoCreateGroup: Boolean = false, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        val curUserDocRef = firebaseFirestore.collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.id!!)

        if (storyGroup.id == null) {
            storyGroup.id = getNewStoryGroupId()
        }

        firebaseFirestore.batch().apply {
            update(
                    curUserDocRef
                            .collection(COLLECTION_STORIES)
                            .document(story.id!!),
                    mapOf(
                            Story.FIELD_GROUPS_ID to FieldValue.arrayUnion(storyGroup.id)
                    )
            )

            if (alsoCreateGroup) {
                set(
                        curUserDocRef
                                .collection(COLLECTION_STORY_GROUPS)
                                .document(storyGroup.id!!),
                        storyGroup.apply { storyCount = 1 }.toMap()
                )
            } else {
                update(
                        curUserDocRef
                                .collection(COLLECTION_STORY_GROUPS)
                                .document(storyGroup.id!!),
                        mapOf(
                                StoryGroup.FIELD_STORY_COUNT to FieldValue.increment(1)
                        )
                )
            }

            commit().addOnCompleteListener {
                callback?.invoke(it.isSuccessful)
            }
        }
    }

    fun removeStoryFromGroup(story: Story, storyGroup: StoryGroup, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        val curUserDocRef = firebaseFirestore.collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.id!!)

        firebaseFirestore.batch().apply {
            update(
                    curUserDocRef
                            .collection(COLLECTION_STORIES)
                            .document(story.id!!),
                    mapOf(
                            Story.FIELD_GROUPS_ID to FieldValue.arrayRemove(storyGroup.id)
                    )
            )

            if (storyGroup.storyCount!! <= 1) {
                delete(
                        curUserDocRef
                                .collection(COLLECTION_STORY_GROUPS)
                                .document(storyGroup.id!!)
                )
            } else {
                update(
                        curUserDocRef
                                .collection(COLLECTION_STORY_GROUPS)
                                .document(storyGroup.id!!),
                        mapOf(
                                StoryGroup.FIELD_STORY_COUNT to FieldValue.increment(-1)
                        )
                )
            }

            commit().addOnCompleteListener {
                callback?.invoke(it.isSuccessful)
            }
        }
    }

    fun getUsersRecentStoryGroup(usersId: List<String>, callback: (storyGroups: List<StoryGroup>) -> Unit) {
        firebaseFirestore.collection(COLLECTION_USERS)
                .whereInSafe(FieldPath.documentId(), usersId)
                .get()
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getStoryGroups") { querySnapshot: QuerySnapshot ->
                        val storyGroups = querySnapshot.documents
                                .filter {
                                    val lastStoryCreatedTime = it.getLong(User.FIELD_LAST_STORY_CREATED_TIME)
                                    lastStoryCreatedTime != null && lastStoryCreatedTime > System.currentTimeMillis() - Constants.DEFAULT_STORY_LIVE_TIME
                                }
                                .map { StoryGroup.fromUserDoc(it) }
                        callback(storyGroups)
                    }
                }
    }

    fun getStories(storyGroupsWithoutStories: List<StoryGroup>, callback: (storyGroupsWithStories: List<StoryGroup>) -> Unit) {
        val tasks = ArrayList<Task<QuerySnapshot>>()
        storyGroupsWithoutStories.forEach { storyGroup ->
            tasks.add(
                    firebaseFirestore.collection(COLLECTION_USERS)
                            .document(storyGroup.ownerId!!)
                            .collection(COLLECTION_STORIES)
                            .let {
                                if (storyGroup.id != StoryGroup.ID_RECENT_STORIES && storyGroup.id != StoryGroup.ID_RECENT_STORY_GROUP) {
                                    it.whereArrayContains(Story.FIELD_GROUPS_ID, storyGroup.id!!)
                                } else {
                                    it.whereGreaterThan(Story.FIELD_CREATED_TIME, System.currentTimeMillis() - Constants.DEFAULT_STORY_LIVE_TIME)
                                }
                            }
                            .orderBy(Story.FIELD_CREATED_TIME, Query.Direction.DESCENDING)
                            .get()
            )
        }

        Tasks.whenAll(tasks)
                .addOnCompleteListener {
                    tasks.forEachIndexed { index, task ->
                        utils.assertTaskSuccessAndResultNotNull(task, TAG, "getStories") { querySnapshot: QuerySnapshot ->
                            val stories = querySnapshot.documents.map { Story.fromDoc(it) }
                            storyGroupsWithoutStories[index].stories = stories
                        }
                    }
                    callback(storyGroupsWithoutStories.filter { it.stories != null })
                }
    }

    fun getUserStoryGroups(user: User, callback: ((storyGroups: List<StoryGroup>) -> Unit)? = null) {
        getUserStoryGroupsQuery(user)
                .get()
                .addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUserStoryGroups") { querySnapshot: QuerySnapshot ->
                        val storyGroups = querySnapshot.documents.map {
                            StoryGroup.fromStoryGroupDoc(it, user)
                        }
                        callback?.invoke(storyGroups)
                    }
                }
    }

    fun addUserStoryGroupsListener(user: User, callback: ((storyGroups: List<StoryGroup>) -> Unit)? = null): ListenerRegistration {
        return getUserStoryGroupsQuery(user)
                .addSnapshotListener { querySnapshot, _ ->
                    utils.assertNotNull(querySnapshot, TAG, "addUserStoryGroupsListener") { querySnapshotNotNull: QuerySnapshot ->
                        val storyGroups = querySnapshotNotNull.documents.map {
                            StoryGroup.fromStoryGroupDoc(it, user)
                        }
                        callback?.invoke(storyGroups)
                    }
                }
    }

    private fun getUserStoryGroupsQuery(user: User): Query {
        return firebaseFirestore.collection(COLLECTION_USERS)
                .document(user.id!!)
                .collection(COLLECTION_STORY_GROUPS)
                .orderBy(StoryGroup.FIELD_CREATED_TIME)
    }

    fun createPost(post: Post, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        if (post.id == null) {
            post.id = getNewPostId()
        }

        firebaseFirestore.collection(COLLECTION_POSTS)
                .document(post.id!!)
                .set(post.toMap())
                .addOnCompleteListener {
                    callback?.invoke(it.isSuccessful)
                }
    }

    fun getUsersRecentDiaries(usersId: List<String>, upperCreatedTimeLimit: Long?, numberLimit: Long, callback: ((diaries: ArrayList<Diary>) -> Unit)? = null) {
        proceedGetDiaryQuery(
                firebaseFirestore.collection(COLLECTION_POSTS)
                        .whereInSafe(Post.FIELD_OWNER_ID, usersId),
                upperCreatedTimeLimit, numberLimit, "getUsersRecentDiaries", callback
        )
    }

    fun getUserRecentDiaries(userId: String, upperCreatedTimeLimit: Long?, numberLimit: Long, callback: ((diaries: ArrayList<Diary>) -> Unit)? = null) {
        proceedGetDiaryQuery(
                firebaseFirestore.collection(COLLECTION_POSTS)
                        .whereEqualTo(Post.FIELD_OWNER_ID, userId),
                upperCreatedTimeLimit, numberLimit, "getUserRecentDiaries", callback
        )
    }

    private fun proceedGetDiaryQuery(query: Query, upperCreatedTimeLimit: Long?, numberLimit: Long, longMsgPrefix:String, callback: ((diaries: ArrayList<Diary>) -> Unit)? = null) {
        query.let { if (upperCreatedTimeLimit != null) it.whereLessThan(Message.FIELD_CREATED_TIME, upperCreatedTimeLimit) else it }
                .orderBy(Message.FIELD_CREATED_TIME, Query.Direction.DESCENDING)
                .limit(numberLimit)
                .get().addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, longMsgPrefix) { querySnapshot ->
                        val diaries = arrayListOf<Diary>()
                        querySnapshot.forEach {
                            diaries.add(Diary.fromDoc(it))
                        }
                        callback?.invoke(diaries)
                    }
                }
    }

    fun getUsersRecentWatches(usersId: List<String>, upperCreatedTimeLimit: Long?, numberLimit: Long, callback: ((diaries: ArrayList<Watch>) -> Unit)? = null) {
        firebaseFirestore.collection(COLLECTION_POSTS)
                .whereInSafe(Post.FIELD_OWNER_ID, usersId)
                .let { if (upperCreatedTimeLimit != null) it.whereLessThan(Message.FIELD_CREATED_TIME, upperCreatedTimeLimit) else it }
                .whereEqualTo(Post.FIELD_TYPE, Post.TYPE_WATCH)
                .orderBy(Message.FIELD_CREATED_TIME, Query.Direction.DESCENDING)
                .limit(numberLimit)
                .get().addOnCompleteListener { task ->
                    utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUsersRecentWatches") { querySnapshot ->
                        val watches = arrayListOf<Watch>()
                        querySnapshot.forEach {
                            watches.addAll(Watch.fromDoc(it))
                        }
                        callback?.invoke(watches)
                    }
                }
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

    fun getNewStoryGroupId(): String {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.id!!)
                .collection(COLLECTION_STORY_GROUPS)
                .document().id
    }

    private fun getNewStoryId(): String {
        return firebaseFirestore
                .collection(COLLECTION_USERS)
                .document(sessionManager.curUser!!.id!!)
                .collection(COLLECTION_STORIES)
                .document().id
    }

    fun getNewPostId(): String {
        return firebaseFirestore
                .collection(COLLECTION_POSTS)
                .document().id
    }

    companion object {
        private const val COLLECTION_USERS = "users"
        private const val COLLECTION_ROOMS = "rooms"
        private const val COLLECTION_MESSAGES = "messages"
        private const val COLLECTION_POSTS = "posts"

        private const val COLLECTION_STORY_GROUPS = "story_groups"
        private const val COLLECTION_STORIES = "stories"

        private const val COLLECTION_MEMBERS = "members"
        private const val COLLECTION_STICKER_SETS = "sticker_sets"
        private const val COLLECTION_STICKERS = "stickers"
    }
}