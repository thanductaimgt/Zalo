package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.QuerySnapshot
import java.util.ArrayList
import java.util.HashMap
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.Message
import vng.zalo.tdtai.zalo.zalo.models.Room
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.models.RoomMember
import vng.zalo.tdtai.zalo.zalo.utils.Constants

class RoomActivityViewModel(intent: Intent) : ViewModel() {
    private var lastMessagesQuerySnapshot: QuerySnapshot? = null

    private val room: Room
    val liveMessages: MutableLiveData<List<Message>>

    init {
        val firestore = ZaloApplication.firebaseInstance

        liveMessages = MutableLiveData(ArrayList())

        room = Room(
                avatar = intent.getStringExtra(Constants.ROOM_AVATAR),
                id = intent.getStringExtra(Constants.ROOM_ID),
                name = intent.getStringExtra(Constants.ROOM_NAME)
        )

        //create current room reference
        val currentRoom = firestore.collection(Constants.COLLECTION_ROOMS)
                .document(room.id!!)

        //get current room
        currentRoom.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                room.createdTime = task.result!!.getTimestamp("createdTime")
            } else {
                Log.d(TAG, "Get document fail")
            }
        }

        //observe messages change
        currentRoom.collection(Constants.COLLECTION_MESSAGES)
                .orderBy("createdTime")
                .addSnapshotListener { queryDocumentSnapshots, _ ->
                    updateLiveMessagesValue(queryDocumentSnapshots)
                    lastMessagesQuerySnapshot = queryDocumentSnapshots
                }

        //get members
        currentRoom.collection(Constants.COLLECTION_MEMBERS)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        //save members in room
                        val memberMap = HashMap<String, RoomMember>()
                        for (doc in task.result!!) {
                            val roomMember = doc.toObject(RoomMember::class.java)
                            memberMap[doc.getString("phone")!!] = roomMember
                        }
                        room.memberMap = memberMap

                        //set livedata value
                        updateLiveMessagesValue(lastMessagesQuerySnapshot)
                    } else {
                        Log.d(TAG, "RoomQuery fail")
                    }
                }

        //observe to set unseenMsgNum = 0
        val curRoomRef = ZaloApplication.firebaseInstance
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser!!.phone!!)
                .collection(Constants.COLLECTION_ROOMS)
                .document(room.id!!)

        val currentUserPhone = ZaloApplication.currentUser!!.phone
        curRoomRef.addSnapshotListener { documentSnapshot, _ ->
            val roomItem = documentSnapshot!!.toObject(RoomItem::class.java)!!
            if (roomItem.unseenMsgNum > 0 && currentUserPhone == ZaloApplication.currentUser!!.phone) {
                Log.d(TAG, "onEvent set unseen = 0 of " + curRoomRef.path)
                roomItem.unseenMsgNum = 0
                curRoomRef.set(roomItem.toMap())
                        .addOnFailureListener { Log.d(TAG, "Change unseenMsgNum = 0 fail") }
            }
        }
    }

    private fun updateLiveMessagesValue(docs: QuerySnapshot?) {
        //set livedata value
        if (docs != null) {
            val messages = ArrayList<Message>()
            for (doc in docs) {
                messages.add(
                        doc.toObject(Message::class.java).apply {
                            senderAvatar = room.memberMap?.get(senderPhone)?.avatar
                        }
                )
            }
            liveMessages.value = messages
        }
    }

    fun addMessageToFirestore(content: String) {
        val message = Message(
                content = content,
                createdTime = Timestamp.now(),
                senderPhone = ZaloApplication.currentUser!!.phone,
                senderAvatar = ZaloApplication.currentUser!!.avatar,
                type = Constants.MESSAGE_TYPE_TEXT
        )

        //add message
        ZaloApplication.firebaseInstance
                .collection(Constants.COLLECTION_ROOMS)
                .document(room.id!!)
                .collection(Constants.COLLECTION_MESSAGES)
                .document()
                .set(message.toMap())

        //for all users in this room except the current user, change last message
        for ((curUserPhone) in room.memberMap!!) {
            val lastMsgPreviewContent = (if (curUserPhone == ZaloApplication.currentUser!!.phone) Constants.ME else ZaloApplication.currentUser!!.phone) + ": " + message.content

            ZaloApplication.firebaseInstance
                    .collection(Constants.COLLECTION_USERS)
                    .document(curUserPhone)
                    .collection(Constants.COLLECTION_ROOMS)
                    .document(room.id!!)
                    .update(
                            HashMap<String, Any?>().apply {
                                put("lastMsg",lastMsgPreviewContent)
                                put("lastMsgTime",message.createdTime)
                                if (curUserPhone != ZaloApplication.currentUser!!.phone)
                                    put("unseenMsgNum",FieldValue.increment(1))
                            }
                    )
                    .addOnFailureListener { Log.d(TAG, "Update last message fail") }

//            curRoomDocPath.get()
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val newRoomItem = task.result!!.data!!.apply {
//                                newRoomItem["lastMsg"] = lastMsg
//                                newRoomItem["lastMsgTime"] = message.createdTime
//                                if (curUserPhone != ZaloApplication.currentUser!!.phone)
//                                    newRoomItem["unseenMsgNum"] = newRoomItem["unseenMsgNum"] as Long + 1
//                            }
//
//                            curRoomDocPath.set(newRoomItem)
//                                    .addOnFailureListener { Log.d(TAG, "Update last message fail") }
//                        } else {
//                            Log.d(TAG, "GetRoomQuery fail")
//                        }
//                    }
        }
    }

    companion object {
        private val TAG = RoomActivityViewModel::class.java.simpleName
    }
}