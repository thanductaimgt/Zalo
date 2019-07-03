package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import java.util.*

class GroupFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>>

    init {
        liveRoomItems = MutableLiveData(ArrayList())

        Log.d(TAG, ZaloApplication.currentUser!!.toString())

        ZaloApplication.firebaseInstance
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser!!.phone!!)
                .collection(Constants.COLLECTION_ROOMS)
                .whereEqualTo("roomType", Constants.ROOM_TYPE_GROUP)
                .orderBy("lastMsgTime")
                .addSnapshotListener { queryDocumentSnapshots, _ ->
                    if (queryDocumentSnapshots != null) {
                        val roomItems = ArrayList<RoomItem>()
                        for (doc in queryDocumentSnapshots) {
                            roomItems.add(
                                    doc.toObject(RoomItem::class.java).apply {
                                        roomId = doc.id
                                    }
                            )
                        }
                        liveRoomItems.value = roomItems
                        Log.d(TAG, liveRoomItems.value!!.toString())
                    } else {
                        Log.d(TAG, Constants.COLLECTION_USERS + "/" + ZaloApplication.currentUser!!.phone + "/" + Constants.COLLECTION_ROOMS)
                        Log.d(TAG, "queryDocumentSnapshots is null")
                    }
                }
    }

    companion object {
        private val TAG = GroupFragmentViewModel::class.java.simpleName
    }
}