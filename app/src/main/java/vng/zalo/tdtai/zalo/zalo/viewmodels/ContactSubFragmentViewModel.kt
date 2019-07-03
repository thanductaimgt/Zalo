package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import java.util.*

class ContactSubFragmentViewModel : ViewModel() {
    val liveContacts: MutableLiveData<List<RoomItem>>

    init {
        liveContacts = MutableLiveData(ArrayList())

        ZaloApplication.firebaseInstance
                .collection(Constants.COLLECTION_USERS)
                .document(ZaloApplication.currentUser!!.phone!!)
                .collection(Constants.COLLECTION_CONTACTS)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val roomItems = ArrayList<RoomItem>()
                        if (task.result != null) {
                            for (doc in task.result!!) {
                                roomItems.add(
                                        RoomItem(
                                                avatar = doc.getString("avatar"),
                                                name = doc.id,
                                                roomId = doc.getString("roomId")
                                        )
                                )
                            }
                            liveContacts.value = roomItems
                        } else {
                            Log.d(TAG, "task.result is null")
                        }
                    } else {
                        Log.d(TAG, "Contact Query Fail")
                    }
                }
    }

    companion object {
        private val TAG = ContactSubFragmentViewModel::class.java.simpleName
    }
}