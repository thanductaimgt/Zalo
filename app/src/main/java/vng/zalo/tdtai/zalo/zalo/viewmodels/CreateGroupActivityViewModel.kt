package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.models.Room
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.networks.Database
import vng.zalo.tdtai.zalo.zalo.networks.Storage
import vng.zalo.tdtai.zalo.zalo.utils.Constants


class CreateGroupActivityViewModel : ViewModel() {
    val liveRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    fun createRoomInFireStore(newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null) {
        val newRoomId = Database.getNewRoomId()

        // path to save room avatar in storage
        val fileStoragePath = "${Constants.ROOM_AVATARS_PATH}/$newRoomId"

        // save room avatar in storage
        Storage.addFile(
                localPath = newRoom.avatar!!,
                storagePath = fileStoragePath
        ) {
            newRoom.id = newRoomId
            //change from localPath to storagePath
            newRoom.avatar = fileStoragePath

            Database.addRoomAndUserRoom(newRoom) {
                callback?.invoke(it)
            }
        }
    }
}