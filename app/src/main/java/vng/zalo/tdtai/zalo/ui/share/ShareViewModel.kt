package vng.zalo.tdtai.zalo.ui.share

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.data_model.room.RoomItem
import javax.inject.Inject


class ShareViewModel @Inject constructor(intent: Intent) : BaseViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    private val localUris = utils.getLocalUris(intent)

    init {
        database.getUserRooms(
                userId = sessionManager.curUser!!.id!!
        ) { liveRoomItems.value = it }
    }

    fun addNewMessagesToFirestore(callback: ((isSuccess: Boolean) -> Unit)? = null) {
        var count = 0
        var isAnyFail = false

        val observer = object :Observer<Message> {
            override fun onComplete() {
                count++
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
        }

        liveSelectedRoomItems.value!!.forEach {
            database.getRoomInfo(it.roomId!!) { room ->
                messageManager.addNewMessagesToFirestore(room, localUris, Message.TYPE_FILE, observer)
            }
        }
    }
}