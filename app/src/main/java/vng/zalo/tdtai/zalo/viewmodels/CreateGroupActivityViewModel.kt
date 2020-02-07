package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.storage.FirebaseDatabase
import vng.zalo.tdtai.zalo.storage.FirebaseStorage


class CreateGroupActivityViewModel : ViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    var liveAvatarLocalUri: MutableLiveData<String> = MutableLiveData()

    init {
        FirebaseDatabase.getUserRooms(
                userPhone = ZaloApplication.curUser!!.phone!!,
                roomType = Room.TYPE_PEER
        ) { liveRoomItems.value = it }
    }

    fun createRoomInFireStore(context: Context, newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null) {
        val newRoomId = FirebaseDatabase.getNewRoomId()
        val avatarLocalPath = newRoom.avatarUrl

        // path to save room avatarUrl in storage
        val avatarStoragePath = FirebaseStorage.getRoomAvatarStoragePath(newRoomId)

        newRoom.id = newRoomId
        // save room avatarUrl in storage
        if (avatarLocalPath != null) {
            FirebaseStorage.addResourceAndGetDownloadUrl(
                    context,
                    avatarLocalPath,
                    avatarStoragePath,
                    Message.TYPE_IMAGE
            ) {downloadUrl->
                // delete local file
//                MediaManager.deleteZaloFileAtUri(context, avatarLocalPath)

                //change from localPath to storagePath
                newRoom.avatarUrl = downloadUrl

                FirebaseDatabase.addRoomAndUserRoom(newRoom, callback)
            }
        } else {
            FirebaseDatabase.addRoomAndUserRoom(newRoom, callback)
        }
    }
}