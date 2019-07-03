package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

import java.util.ArrayList

import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.Constants

class RecentContactsSubFragmentViewModel : ViewModel() {

    val liveRoomItems: MutableLiveData<List<RoomItem>>

    init {
        liveRoomItems = MutableLiveData(ArrayList())

        ZaloApplication.firebaseInstance
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser!!.phone!!)
                .collection(Constants.COLLECTION_ROOMS)
                .orderBy("lastMsgTime", Query.Direction.DESCENDING)
                .addSnapshotListener { queryDocumentSnapshots, _ ->
                    val roomItems = ArrayList<RoomItem>()
                    if (queryDocumentSnapshots != null) {
                        for (doc in queryDocumentSnapshots) {
                            roomItems.add(
                                    doc.toObject(RoomItem::class.java).apply {
                                        roomId = doc.id
                                    }
                            )
                        }
                        liveRoomItems.value = roomItems
                    } else {
                        Log.d(TAG, "queryDocumentSnapshots is null")
                    }
                }
    }

    companion object {
        private val TAG = RecentContactsSubFragmentViewModel::class.java.simpleName
    }
}