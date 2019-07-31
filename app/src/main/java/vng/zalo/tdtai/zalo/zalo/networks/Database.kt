package vng.zalo.tdtai.zalo.zalo.networks

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.*
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class Database {
    companion object {
        private val TAG = Database::class.java.simpleName
        private val firebaseFirestore: FirebaseFirestore
            get() = FirebaseFirestore.getInstance()

        fun addNewMessageAndUpdateUsersRoom(curRoom: Room, newMessage: Message, callback: (() -> Unit)? = null) {
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
                val lastMsgPreviewContent = when (newMessage.type) {
                    Constants.MESSAGE_TYPE_TEXT -> newMessage.content
                    Constants.MESSAGE_TYPE_STICKER -> "<Sticker>"
                    else -> ""
                }

                batch.update(
                        firebaseFirestore
                                .collection(Constants.COLLECTION_USERS)
                                .document(curUserPhone)
                                .collection(Constants.COLLECTION_ROOMS)
                                .document(curRoom.id!!),
                        HashMap<String, Any?>().apply {
                            put("lastSenderPhone", newMessage.senderPhone)
                            put("lastMsg", lastMsgPreviewContent)
                            put("lastMsgTime", newMessage.createdTime)
                            if (curUserPhone != ZaloApplication.currentUser!!.phone)
                                put("unseenMsgNum", FieldValue.increment(1))
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
                    roomType = Constants.ROOM_TYPE_GROUP
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
                    .let { if (fieldToOrder != null) it.orderBy("createdTime", orderDirection) else it }
                    .addSnapshotListener { querySnapshot, _ ->
                        Utils.assertNotNull(querySnapshot, TAG, "addRoomMessagesListener") { querySnapshotNotNull ->
                            val messages = ArrayList<Message>()
                            for (doc in querySnapshotNotNull) {
                                messages.add(doc.toObject(Message::class.java))
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

        fun validateLoginInfo(phone: String, password: String, callback: (isLoginSuccess: Boolean, userInfo: UserInfo?) -> Unit) {
            firebaseFirestore.collection(Constants.COLLECTION_USERS)
                    .document(phone)
                    .get()
                    .addOnCompleteListener { task ->
                        Utils.assertTaskSuccess(task, TAG, "assertLoginSuccess") { result ->
                            if (result != null) {
                                val userInfo = result.toObject(UserInfo::class.java)!!
                                userInfo.phone = phone
                                callback(true, userInfo)
                            } else {
                                callback(false, null)
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
                            room.createdTime = (getRoomTask.result as DocumentSnapshot).getTimestamp("createdTime")
                        }

                        Utils.assertTaskSuccessAndResultNotNull(getRoomMembersTask, TAG, "getRoomInfo.getRoomMembersTask") { result ->
                            val memberMap = HashMap<String, RoomMember>()
                            for (doc in (result as QuerySnapshot)) {
                                val roomMember = doc.toObject(RoomMember::class.java)
                                memberMap[doc.getString("phone")!!] = roomMember
                            }
                            room.memberMap = memberMap

                            //set livedata value
                            callback?.invoke(room)
                        }
                    }
        }

        fun getUserRooms(userPhone: String, roomType: Int? = null, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)? = null) {
            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .let { if (roomType != null) it.whereEqualTo("roomType", roomType) else it }
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
    }
}