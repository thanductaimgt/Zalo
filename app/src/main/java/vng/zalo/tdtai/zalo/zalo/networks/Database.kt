package vng.zalo.tdtai.zalo.zalo.networks

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.*
import vng.zalo.tdtai.zalo.zalo.utils.Constants

class Database {
    companion object {
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
                val lastMsgPreviewContent = newMessage.content

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
                if (task.isSuccessful) {
                    callback?.invoke()
                } else {
                    Log.e("addMsgUpdateUsersRoom", "task not successful")
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
                    HashMap<String, Any?>().apply {
                        put("createdTime", newRoom.createdTime)
                    }
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
                val newRoomItemRef = ZaloApplication.firebaseFirestore
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
                if (task.isSuccessful) {
                    callback?.invoke(newRoomItem)
                } else {
                    Log.e("addRoomAndUserRoom", "task fail")
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
                        if (querySnapshot != null) {
                            val messages = ArrayList<Message>()
                            for (doc in querySnapshot) {
                                messages.add(doc.toObject(Message::class.java))
                            }
                            callback?.invoke(messages)
                        } else {
                            Log.e("addRoomMessagesListener", "querySnapshot is null")
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
                if (documentSnapshot != null) {
                    callback?.invoke(documentSnapshot)
                } else {
                    Log.e("addUserRoomChangeLsn", "documentSnapshot is null")
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
                        if (querySnapshot != null) {
                            val roomItems = ArrayList<RoomItem>()
                            for (doc in querySnapshot) {
                                roomItems.add(
                                        doc.toObject(RoomItem::class.java).apply {
                                            roomId = doc.id
                                        }
                                )
                            }
                            callback?.invoke(roomItems)
                            Log.d("addUserRoomsListener", "querySnapshot is not null")
                        } else {
                            Log.d("addUserRoomsListener", "querySnapshot is null")
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
            tasks.add(curRoomDocRef.get())

            //get room members
            tasks.add(curRoomDocRef.collection(Constants.COLLECTION_MEMBERS).get())

            // add callback when all tasks done
            Tasks.whenAll(tasks)
                    .addOnCompleteListener {
                        val room = Room()

                        if (tasks[0].isSuccessful) {
                            room.createdTime = (tasks[0].result as DocumentSnapshot).getTimestamp("createdTime")
                        } else {
                            Log.e("getRoomInfo", "tasks[0] fail")
                        }

                        if (tasks[1].isSuccessful) {
                            val memberMap = HashMap<String, RoomMember>()
                            for (doc in (tasks[1].result as QuerySnapshot)) {
                                val roomMember = doc.toObject(RoomMember::class.java)
                                Log.d("getRoomInfo", memberMap.toString())
                                memberMap[doc.getString("phone")!!] = roomMember
                            }
                            room.memberMap = memberMap

                            //set livedata value
                            callback?.invoke(room)
                        } else {
                            Log.e("getRoomInfo", "tasks[1] fail")
                        }
                    }
        }

        fun getUserRooms(userPhone: String, roomType: Int? = null, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((roomItems: List<RoomItem>) -> Unit)?) {
            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .let { if (roomType != null) it.whereEqualTo("roomType", roomType) else it }
                    .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result != null) {
                                val roomItems = ArrayList<RoomItem>()
                                for (doc in task.result!!) {
                                    roomItems.add(
                                            doc.toObject(RoomItem::class.java).apply {
                                                roomId = doc.id
                                            }
                                    )
                                }
                                callback?.invoke(roomItems)
                            } else {
                                Log.e("getUserRooms", "task.result is null")
                            }
                        } else {
                            Log.e("getUserRooms", "task fail")
                        }
                    }
        }

        fun getUserStickerSets(userPhone: String, callback: ((stickerSets: List<StickerSet>) -> Unit)?) {
            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_STICKER_SETS)
                    .get().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            if (task.result != null) {
                                val stickerSets = ArrayList<StickerSet>()
                                for (doc in task.result!!) {
                                    stickerSets.add(
                                            doc.toObject(StickerSet::class.java)
                                    )
                                }
                                callback?.invoke(stickerSets)
                            } else {
                                Log.e("getUserStickerSets", "task.result is null")
                            }
                        } else {
                            Log.e("getUserStickerSets", "task fail")
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
                        if (task.isSuccessful) {
                            callback?.invoke()
                        } else {
                            Log.e("updateUserRoom", "task fail")
                        }
                    }
        }
    }
}