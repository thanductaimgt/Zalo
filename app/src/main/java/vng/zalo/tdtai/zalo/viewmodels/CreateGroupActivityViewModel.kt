package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.utils.Constants


class CreateGroupActivityViewModel : ViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(java.util.ArrayList())

    init {
        Database.getUserRooms(
                userPhone = ZaloApplication.curUser!!.phone!!,
                roomType = Room.TYPE_PEER
        ) { liveRoomItems.value = it }
    }

    fun createRoomInFireStore(context: Context, newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null) {
        val newRoomId = Database.getNewRoomId()
        val avatarLocalPath = newRoom.avatarUrl

        // path to save room avatarUrl in storage
        val avatarStoragePath = Storage.getRoomAvatarStoragePath(newRoomId)

        newRoom.id = newRoomId
        // save room avatarUrl in storage
        if (avatarLocalPath != null) {
            Storage.addResourceAndGetDownloadUrl(
                    context,
                    avatarLocalPath,
                    avatarStoragePath,
                    Message.TYPE_IMAGE
            ) {downloadUrl->
                // delete local file
//                MediaManager.deleteZaloFileAtUri(context, avatarLocalPath)

                //change from localPath to storagePath
                newRoom.avatarUrl = downloadUrl

                Database.addRoomAndUserRoom(newRoom, callback)
            }
        } else {
            Database.addRoomAndUserRoom(newRoom, callback)
        }
    }
}