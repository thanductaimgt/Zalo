package vng.zalo.tdtai.zalo.ui.share

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import vng.zalo.tdtai.zalo.managers.MessageManager
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject


class ShareViewModel @Inject constructor(
        intent: Intent,
        utils: Utils,
        sessionManager: SessionManager,
        private val database: Database,
        private val messageManager: MessageManager
) : ViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    private val localUris = utils.getLocalUris(intent)

    init {
        database.getUserRooms(
                userPhone = sessionManager.curUser!!.phone!!
        ) { liveRoomItems.value = it }
    }

    fun addNewMessagesToFirestore(context: Context, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        var count = 0
        var isAnyFail = false
        liveSelectedRoomItems.value!!.forEach {
            database.getRoomInfo(it.roomId!!) { room ->
                messageManager.addNewMessagesToFirestore(context, room, localUris, Message.TYPE_FILE, object : Observer<Message> {
                    override fun onComplete() {
                        count++
                        Log.d(TAG, "count: $count, select: ${liveSelectedRoomItems.value!!.size}")
                        if (count == liveSelectedRoomItems.value!!.size) {
                            callback?.invoke(true)
                        }
                    }

                    override fun onSubscribe(d: Disposable) {//To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onNext(t: Message) {//To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onError(e: Throwable) {
                        if (!isAnyFail) {
                            isAnyFail = true
                            callback?.invoke(false)
                        }
                        e.printStackTrace()
                    }
                })
            }
        }
    }
}