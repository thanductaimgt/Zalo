package vng.zalo.tdtai.zalo.zalo.networks

import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.Message
import vng.zalo.tdtai.zalo.zalo.models.Room
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.models.RoomMember
import vng.zalo.tdtai.zalo.zalo.utils.Constants

class Database {
    companion object {
        val TAG = Database::class.java.simpleName
        private val firebaseFirestore: FirebaseFirestore
            get() = FirebaseFirestore.getInstance()

        fun addUserRoomsChangeListener(userPhone: String, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((querySnapshot: QuerySnapshot) -> Unit)? = null) {
            val thisFuncTag = TAG + "." + object {}.javaClass.enclosingMethod?.name

            firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .let { if (fieldToOrder != null) it.orderBy(fieldToOrder, orderDirection) else it }
                    .addSnapshotListener { querySnapshot, _ ->
                        if (querySnapshot != null) {
                            callback?.invoke(querySnapshot)
                            Log.d(thisFuncTag, "querySnapshot is not null")
                        } else {
                            Log.d(thisFuncTag, "querySnapshot is null")
                        }
                    }
        }

        fun getUserRooms(userPhone: String, roomType: Int? = null, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((querySnapshot: QuerySnapshot) -> Unit)?) {
            val thisFuncTag = TAG + "." + object {}.javaClass.enclosingMethod?.name

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
                                callback?.invoke(task.result!!)
                            } else {
                                Log.e(thisFuncTag, "task.result is null")
                            }
                        } else {
                            Log.e(thisFuncTag, "Contact Query Fail")
                        }
                    }
        }

        fun addNewMessageAndUpdateUsersRoom(curRoom: Room, newMessage: Message, callback: (() -> Unit)? = null) {
            val thisFuncTag = TAG + "." + object {}.javaClass.enclosingMethod?.name

            val batch = firebaseFirestore.batch()

            //add message
            batch.set(
                    firebaseFirestore
                            .collection(Constants.COLLECTION_ROOMS)
                            .document(curRoom.id!!)
                            .collection(Constants.COLLECTION_MESSAGES)
                            .document(),
                    newMessage.toMap()
            )

            //for all users in this room except the current user, change last message
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
                    Log.e(thisFuncTag, "task not successful")
                }
            }
        }

        fun getRoomInfo(roomId: String, callback: ((room: Room) -> Unit)? = null) {
            val thisFuncTag = TAG + "." + object {}.javaClass.enclosingMethod?.name

            //current room reference
            val currentRoomDocRef = firebaseFirestore.collection(Constants.COLLECTION_ROOMS)
                    .document(roomId)

            val tasks: ArrayList<Task<*>> = ArrayList()

            //get room
            tasks.add(currentRoomDocRef.get())

            //get room members
            tasks.add(currentRoomDocRef.collection(Constants.COLLECTION_MEMBERS).get())

            Tasks.whenAll(tasks)
                    .addOnCompleteListener {
                        val room = Room()

                        if (tasks[0].isSuccessful) {
                            room.createdTime = (tasks[0].result as DocumentSnapshot).getTimestamp("createdTime")
                        } else {
                            Log.e(thisFuncTag, "tasks[0] fail")
                        }

                        if (tasks[1].isSuccessful) {
                            val memberMap = HashMap<String, RoomMember>()
                            for (doc in (tasks[1].result as QuerySnapshot)) {
                                val roomMember = doc.toObject(RoomMember::class.java)
                                memberMap[doc.getString("phone")!!] = roomMember
                            }
                            room.memberMap = memberMap

                            //set livedata value
                            callback?.invoke(room)
                        } else {
                            Log.e(thisFuncTag, "tasks[1] fail")
                        }
                    }
        }

        fun addRoomMessagesListener(roomId: String, fieldToOrder: String? = null, orderDirection: Query.Direction = Query.Direction.ASCENDING, callback: ((querySnapshot: QuerySnapshot) -> Unit)? = null) {
            val thisFuncTag = TAG + "." + object {}.javaClass.enclosingMethod?.name

            firebaseFirestore
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(roomId)
                    .collection(Constants.COLLECTION_MESSAGES)
                    .let { if (fieldToOrder != null) it.orderBy("createdTime", orderDirection) else it }
                    .addSnapshotListener { querySnapshot, _ ->
                        if (querySnapshot != null) {
                            callback?.invoke(querySnapshot)
                        } else {
                            Log.e(thisFuncTag, "querySnapshot is null")
                        }
                    }
        }

        fun addUserRoomUnseenMsgNumChangeListener(userPhone: String, roomId: String) {
            val thisFuncTag = TAG + "." + object {}.javaClass.enclosingMethod?.name

            val curRoomRef = firebaseFirestore
                    .collection(Constants.COLLECTION_USERS)
                    .document(userPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(roomId)

            curRoomRef.addSnapshotListener { documentSnapshot, _ ->
                val roomItem = documentSnapshot!!.toObject(RoomItem::class.java)!!
                if (roomItem.unseenMsgNum > 0 && userPhone == ZaloApplication.currentUser!!.phone) {
                    Log.d(thisFuncTag, "onEvent set unseen = 0 of " + curRoomRef.path)
                    roomItem.unseenMsgNum = 0
                    curRoomRef.set(roomItem.toMap())
                            .addOnFailureListener { Log.d(thisFuncTag, "Change unseenMsgNum = 0 fail") }
                }
            }
        }
    }
}