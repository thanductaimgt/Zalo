package vng.zalo.tdtai.zalo.viewmodels

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils


class CreateGroupActivityViewModel : ViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(java.util.ArrayList())

    init {
        Database.getUserRooms(
                userPhone = ZaloApplication.curUser!!.phone!!,
                roomType = Room.TYPE_PEER,
                fieldToOrder = RoomItem.FIELD_NAME
        ) { liveRoomItems.value = it }
    }

    fun createRoomInFireStore(context: Context, newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null) {
        val newRoomId = Database.getNewRoomId()
        val avatarLocalPath = newRoom.avatarUrl

        // path to save room avatarUrl in storage
        val avatarStoragePath = "${Constants.FOLDER_ROOM_AVATARS}/$newRoomId"

        newRoom.id = newRoomId
        // save room avatarUrl in storage
        if (avatarLocalPath != null) {
            Storage.addFileAndGetDownloadUrl(
                    context,
                    avatarLocalPath,
                    avatarStoragePath
            ) {downloadUrl->
                // delete local file
                Utils.deleteZaloFileAtUri(context, avatarLocalPath)

                //change from localPath to storagePath
                newRoom.avatarUrl = downloadUrl

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