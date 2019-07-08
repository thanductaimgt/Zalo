package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.Room
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.Constants


class CreateGroupActivityViewModel : ViewModel() {
    val liveRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    fun createRoomInFireStore(newRoom: Room, callback: (roomItem: RoomItem) -> Unit) {
//        val newRoomDocRef = ZaloApplication.firebaseFirestore
//                .collection(Constants.COLLECTION_ROOMS)
//                .document()
//
//        newRoom.id = newRoomDocRef.id
//
//        // Create a storage reference from our app
//        val storageRef = ZaloApplication.firebaseStorage.reference
//
//// Create a reference to "mountains.jpg"
//        val mountainsRef = storageRef.child("mountains.jpg")
//
//// Create a reference to 'images/mountains.jpg'
//        val mountainImagesRef = storageRef.child("images/mountains.jpg")
//
//// While the file names are the same, the references point to different files
//        mountainsRef.name == mountainImagesRef.name // true
//        mountainsRef.path == mountainImagesRef.path // false
//
//        val batch = ZaloApplication.firebaseFirestore.batch()
//
//        val newRoomItem = RoomItem(
//                roomId = newRoom.id,
//                avatar = newRoom.avatar,
//                name = newRoom.name,
//                roomType = Constants.ROOM_TYPE_GROUP,
//                unseenMsgNum = 0
//        )
//
//        newRoom.memberMap!!.forEach {
//            val newRoomItemRef = ZaloApplication.firebaseFirestore
//                    .collection(Constants.COLLECTION_USERS)
//                    .document(it.key)
//                    .collection(Constants.COLLECTION_ROOMS)
//                    .document()
//
//            batch.set(
//                    newRoomItemRef,
//                    HashMap<String, Any?>().apply {
//                        put("createdTime", newRoom.createdTime)
//                    }
//            ).set(
//                    newRoomDocRef.collection(Constants.COLLECTION_MEMBERS).document(),
//                    it.value
//            ).set(
//                    newRoomItemRef,
//                    newRoomItem.toMap()
//            )
//        }
//
//        batch.commit().addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                callback(newRoomItem)
//            } else {
//                Log.d(TAG, "task fail")
//            }
//        }
    }

    companion object {
        private val TAG = ChatFragmentViewModel::class.java.simpleName
    }
}