package vng.zalo.tdtai.zalo.networks

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.*
import vng.zalo.tdtai.zalo.models.message.CallMessage
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils

class Database {
    companion object {
        private val firebaseFirestore: FirebaseFirestore
            get() = FirebaseFirestore.getInstance()

        fun addNewMessageAndUpdateUsersRoom(context: Context, curRoom: Room, newMessage: Message, callback: (() -> Unit)? = null) {
            val batch = firebaseFirestore.batch()

            //create message
            batch.set(
                    firebaseFirestore
                            .collection(Constants.COLLECTION_ROOMS)
                            .document(curRoom.id!!)
                            .collection(Constants.COLLECTION_MESSAGES)
                            .document(),
                    newMessage.toMap()
            )

            //for user rooms except for current user, update fields
            curRoom.memberMap!!.forEach {
                val curUserPhone = it.key
                val lastMsgPreviewContent = newMessage.getPreviewContent(context)

                batch.update(
                        firebaseFirestore
                                .collection(Constants.COLLECTION_USERS)
                                .document(curUserPhone)
                                .collection(Constants.COLLECTION_ROOMS)
                                .document(curRoom.id!!),
                        HashMap<String, Any?>().apply {
                            put(RoomItem.FIELD_LAST_SENDER_PHONE, newMessage.senderPhone)
                            put(RoomItem.FIELD_LAST_MSG, lastMsgPreviewContent)
                            put(RoomItem.FIELD_LAST_MSG_TIME, newMessage.createdTime)
                            if (curUserPhone != ZaloApplication.curUser!!.phone)
                                put(RoomItem.FIELD_UNSEEN_MSG_NUM, FieldValue.increment(1))
                        }
                )
            }

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
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(newRoom.id!!)

            // create room
            batch.set(
                    newRoomDocRef,
                    newRoom.toMap()
            )

            // user room are the same for all members when room created
            val newRoomItem = RoomItem(
                    roomId = newRoom.id,
                    avatarUrl = newRoom.avatarUrl,
                    name = newRoom.name,
                    roomType = RoomItem.TYPE_GROUP
            )

            // in room, create each room member and
            // for each member of room, create user room
            newRoom.memberMap!!.forEach {
                // create room member
                batch.set(
                        newRoomDocRef.collection(Constants.COLLECTION_MEMBERS).document(),
                        it.value
                )

                //create user room
                val newRoomItemRef = firebaseFirestore
                        .collection(Constants.COLLECTION_USERS)
                        .document(it.key)
                        .collection(Constants.COLLECTION_ROOMS)
                        .document(newRoom.id!!)

                batch.set(
                        newRoomItemRef,
                        newRoomItem
                )
            }

            batch.commit().addOnCompleteListener { task ->
                Utils.assertTaskSuccess(task, TAG, "addRoomAndUserRoom") {
                    callback?.invoke(newRoomItem)
                }
            }
        }

        fun addRoomMessagesListener(roomId: String, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((messages: List<Message>) -> Unit)? = null): ListenerRegistration {
            return firebaseFirestore
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(roomId)
                    .collection(Constants.COLLECTION_MESSAGES)
                    .let { if (fieldToOrder != null) it.orderBy(Message.FIELD_CREATED_TIME, orderDirection) else it }
                    .addSnapshotListener { querySnapshot, _ ->
                        Utils.assertNotNull(querySnapshot, TAG, "addRoomMessagesListener") { querySnapshotNotNull ->
                            val messages = ArrayList<Message>()
                            for (doc in querySnapshotNotNull) {
                                val message = when (doc.getLong(Message.FIELD_TYPE)) {
                                    Message.TYPE_CALL.toLong() -> CallMessage()
                                    else -> Message()
                                }
                                message.applyDoc(doc)
                                messages.add(message)
                            }
                            callback?.invoke(messages)
                        }
                    }
        }

        fun addUserRoomChangeListener(userPhone: String, roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration {
            val curRoomRef = firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(roomId)

            return curRoomRef.addSnapshotListener { documentSnapshot, _ ->
                Utils.assertNotNull(documentSnapshot, TAG, "addUserRoomChangeListener") { documentSnapshotNotNull ->
                    callback?.invoke(documentSnapshotNotNull)
                }
            }
        }

        fun addUserRoomsListener(userPhone: String, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null): ListenerRegistration {
            return firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                    .addSnapshotListener { querySnapshot, _ ->
                        Utils.assertNotNull(querySnapshot, TAG, "addUserRoomsListener") { querySnapshotNotNull ->
                            val roomItems = ArrayList<RoomItem>()
                            for (doc in querySnapshotNotNull) {
                                roomItems.add(
                                        doc.toObject(RoomItem::class.java).apply {
                                            roomId = doc.id
                                        }
                                )
                            }
                            callback?.invoke(roomItems)
                        }
                    }
        }

        fun validateLoginInfo(phone: String, password: String, callback: (user: User?) -> Unit) {
            firebaseFirestore.collection(Constants.COLLECTION_USERS)
                    .document(phone)
                    .get()
                    .addOnCompleteListener { task ->
                        Utils.assertTaskSuccess(task, TAG, "assertLoginSuccess") { result ->
                            if (result != null) {
                                val user = result.toObject(User::class.java)!!
                                user.phone = phone
                                callback(user)
                            } else {
                                callback(null)
                            }
                        }
                    }
        }

        fun getNewRoomId(): String {
            return firebaseFirestore
                    .collection(Constants.COLLECTION_ROOMS)
                    .document().id
        }

        fun getRoomInfo(roomId: String, callback: ((room: Room) -> Unit)? = null) {
            //current room reference
            val curRoomDocRef = firebaseFirestore
                    .collection(Constants.COLLECTION_ROOMS)
//                    .whereArrayContains(FieldPath.of("members","phone"), ZaloApplication.currentUser)
                    .document(roomId)

            val tasks: ArrayList<Task<*>> = ArrayList()

            //get room
            val getRoomTask = curRoomDocRef.get()
            tasks.add(getRoomTask)

            //get room members
            val getRoomMembersTask = curRoomDocRef.collection(Constants.COLLECTION_MEMBERS).get()
            tasks.add(getRoomMembersTask)

            // add callback when all tasks done
            Tasks.whenAll(tasks)
                    .addOnCompleteListener {
                        val room = Room()

                        Utils.assertTaskSuccess(getRoomTask, TAG, "getRoomInfo.getRoomTask") {
                            room.createdTime = (getRoomTask.result as DocumentSnapshot).getTimestamp(Room.FIELD_CREATED_TIME)
                        }

                        Utils.assertTaskSuccessAndResultNotNull(getRoomMembersTask, TAG, "getRoomInfo.getRoomMembersTask") { result ->
                            val memberMap = HashMap<String, RoomMember>()
                            for (doc in (result as QuerySnapshot)) {
                                val roomMember = doc.toObject(RoomMember::class.java)

                                memberMap[doc.getString(RoomMember.FIELD_PHONE)!!] = roomMember
                            }
                            room.memberMap = memberMap

                            //set livedata value
                            callback?.invoke(room)
                        }
                    }
        }

        fun getUserRoom(phone: String, callback: (roomItem: RoomItem) -> Unit) {
            //current room reference
            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(ZaloApplication.curUser!!.phone!!)
                    .collection(Constants.COLLECTION_ROOMS)
                    .whereEqualTo(RoomItem.FIELD_NAME, phone)
                    .get()
                    .addOnCompleteListener {
                        Utils.assertTaskSuccessAndResultNotNull(it, TAG, "getUserRoom") { querySnapshot ->
                            querySnapshot.forEach { documentSnapshot ->
                                val roomItem = documentSnapshot.toObject(RoomItem::class.java).apply {
                                    this.roomId = documentSnapshot.id
                                }

                                //set livedata value
                                callback(roomItem)
                            }
                        }
                    }
        }

        fun addRoomInfoChangeListener(roomId: String, callback: ((documentSnapshot: DocumentSnapshot) -> Unit)? = null): ListenerRegistration {
            return firebaseFirestore
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(roomId)
                    .addSnapshotListener { documentSnapshot, _ ->
                        Utils.assertNotNull(documentSnapshot, TAG, "addRoomInfoChangeListener") { documentSnapshotNotNull ->
                            callback?.invoke(documentSnapshotNotNull)
                        }
                    }
        }

        fun getUserRooms(userPhone: String, roomType: Int? = null, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null) {
            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .let { if (roomType != null) it.whereEqualTo(RoomItem.FIELD_ROOM_TYPE, roomType) else it }
                    .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                    .get()
                    .addOnCompleteListener { task ->
                        Utils.assertTaskSuccessAndResultNotNull(task, TAG, "getUserRooms") { result ->
                            val roomItems = ArrayList<RoomItem>()
                            for (doc in result) {
                                roomItems.add(
                                        doc.toObject(RoomItem::class.java).apply {
                                            roomId = doc.id
                                        }
                                )
                            }
                            callback?.invoke(roomItems)
                        }
                    }
        }

        fun getUserStickerSetItems(userPhone: String, callback: ((stickerSets: List<StickerSetItem>) -> Unit)? = null) {
            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_STICKER_SETS)
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

        fun updateUserRoom(userPhone: String, roomId: String, fieldsAndValues: HashMap<String, Any>, callback: (() -> Unit)? = null) {
            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(roomId)
                    .update(fieldsAndValues)
                    .addOnCompleteListener { task ->
                        Utils.assertTaskSuccess(task, TAG, "updateUserRoom") {
                            callback?.invoke()
                        }
                    }
        }

        fun addStickerSet(name: String, bucketName: String, stickerUrls: List<String>, callback: ((stickerSet: StickerSet) -> Unit)? = null) {
            val stickerSetRef = firebaseFirestore
                    .collection(Constants.COLLECTION_STICKER_SETS)
                    .document(bucketName)

            val batch = firebaseFirestore.batch()

            val stickers = ArrayList<Sticker>()
            stickerUrls.forEach { stickerUrl ->
                val newDocRef = stickerSetRef
                        .collection(Constants.COLLECTION_STICKERS)
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
                    .collection(Constants.COLLECTION_STICKER_SETS)
                    .document(bucketName)
            Log.d(TAG, "getStickerSet." + stickerSetDocRef.path)

            val tasks = ArrayList<Task<*>>()

            val getStickerSetTask = stickerSetDocRef.get()
            tasks.add(getStickerSetTask)

            val getStickersTask = stickerSetDocRef
                    .collection(Constants.COLLECTION_STICKERS)
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

        fun addCurrentUserToRoomTypingMembers(roomId: String) {
            Utils.assertTaskSuccess(
                    firebaseFirestore
                            .collection(Constants.COLLECTION_ROOMS)
                            .document(roomId)
                            .update(Room.FIELD_TYPING_MEMBERS_PHONE, FieldValue.arrayUnion(ZaloApplication.curUser!!.phone)),
                    TAG,
                    "addCurrentUserToRoomTypingMembers"
            )
        }

        fun removeCurrentUserToRoomTypingMembers(roomId: String) {
            Utils.assertTaskSuccess(
                    firebaseFirestore
                            .collection(Constants.COLLECTION_ROOMS)
                            .document(roomId)
                            .update(Room.FIELD_TYPING_MEMBERS_PHONE, FieldValue.arrayRemove(ZaloApplication.curUser!!.phone)),
                    TAG,
                    "removeCurrentUserToRoomTypingMembers"
            )
        }

        fun setCurrentUserOnlineState(isOnline: Boolean) {
            Utils.assertTaskSuccess(
                    firebaseFirestore
                            .collection(Constants.COLLECTION_USERS)
                            .document(ZaloApplication.curUser!!.phone!!)
                            .update(User.FIELD_IS_ONLINE, isOnline),
                    TAG,
                    "setCurrentUserOnlineState"
            )
            Log.d(TAG, ZaloApplication.curUser!!.phone)
        }
    }
}