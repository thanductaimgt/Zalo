package vng.zalo.tdtai.zalo.ui.create_group

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.repo.Storage
import javax.inject.Inject


class CreateGroupViewModel @Inject constructor(
        private val database: Database,
        private val storage: Storage,
        sessionManager: SessionManager
) : ViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    var liveAvatarLocalUri: MutableLiveData<String> = MutableLiveData()

    init {
        database.getUserRooms(
                userPhone = sessionManager.curUser!!.phone!!,
                roomType = Room.TYPE_PEER
        ) { liveRoomItems.value = it }
    }

    fun createRoomInFireStore(newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null) {
        val newRoomId = database.getNewRoomId()
        val avatarLocalPath = newRoom.avatarUrl

        // path to save room avatarUrl in storage
        val avatarStoragePath = storage.getRoomAvatarStoragePath(newRoomId)

        newRoom.id = newRoomId
        // save room avatarUrl in storage
        if (avatarLocalPath != null) {
            storage.addResourceAndGetDownloadUrl(
                    avatarLocalPath,
                    avatarStoragePath,
                    Message.TYPE_IMAGE
            ) {downloadUrl->
                // delete local file
//                MediaManager.deleteZaloFileAtUri(context, avatarLocalPath)

                //change from localPath to storagePath
                newRoom.avatarUrl = downloadUrl

                database.addRoomAndUserRoom(newRoom, callback)
            }
        } else {
            database.addRoomAndUserRoom(newRoom, callback)
        }
    }
}