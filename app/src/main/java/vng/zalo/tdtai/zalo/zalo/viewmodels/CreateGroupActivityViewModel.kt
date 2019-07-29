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

        // path to save room avatarUrl in storage
        val fileStoragePath = "${Constants.ROOM_AVATARS_PATH}/$newRoomId"

        newRoom.id = newRoomId
        // save room avatarUrl in storage
        if (newRoom.avatarUrl != null) {
            Storage.addFile(
                    localPath = newRoom.avatarUrl!!,
                    storagePath = fileStoragePath
            ) {
                //change from localPath to storagePath
                newRoom.avatarUrl = fileStoragePath

                Database.addRoomAndUserRoom(newRoom) {
                    callback?.invoke(it)
                }
            }
        } else {
            Database.addRoomAndUserRoom(newRoom) {
                callback?.invoke(it)
            }
        }
    }
}