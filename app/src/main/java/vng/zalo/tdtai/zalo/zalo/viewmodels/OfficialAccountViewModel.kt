package vng.zalo.tdtai.zalo.zalo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot

import java.util.ArrayList

import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.Utils

class OfficialAccountViewModel : ViewModel() {
    private val tasks: List<Task<QuerySnapshot>>

    val liveOfficialAccounts: MutableLiveData<List<RoomItem>>

    init {
        liveOfficialAccounts = MutableLiveData(ArrayList())

        val firestore = ZaloApplication.firebaseFirestore

        tasks = ArrayList()

        //        //get roomIds
        //        tasks.add(firestore.collection(Constants.COLLECTION_BELONGS_TO)
        //                .whereEqualTo("userPhone", ZaloApplication.currentUserPhone)
        //                .get());
        //
        //        //get all rooms
        //        tasks.add(firestore.collection(COLLECTION_ROOMS)
        //                .get());
        //
        //        Tasks.whenAll(tasks)
        //                .addOnCompleteListener(new OfficialAccountViewModel.OfficialAccountQueryListener());
        //        firestore.collection(COLLECTION_OFFICIAL_ACCOUNTS).get().addOnCompleteListener(new OfficialAccountQueryListener());
    }

    internal inner class OfficialAccountQueryListener : OnCompleteListener<QuerySnapshot> {
        override fun onComplete(task: Task<QuerySnapshot>) {
            if (task.isSuccessful) {
                // get rooms
                val rooms = ArrayList<RoomItem>()
                if (task.result != null) {
                    for (doc in task.result!!) {
                        rooms.add(
                                RoomItem(
                                        name = doc.getString("name"),
                                        avatar = doc.getString("avatar")
                                )
                        )
                    }

                    liveOfficialAccounts.value = rooms
                } else {
                    Log.d(Utils.getTag(object {}), "task.result is null")
                }
            } else {
                Log.d(Utils.getTag(object {}), "OfficialAccount Query fail")
            }
        }
    }
}
