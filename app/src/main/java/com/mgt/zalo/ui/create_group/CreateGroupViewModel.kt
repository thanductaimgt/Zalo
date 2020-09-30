package com.mgt.zalo.ui.create_group

import androidx.lifecycle.MutableLiveData
import com.mgt.zalo.base.BaseViewModel
import com.mgt.zalo.data_model.room.Room
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.repository.Storage
import javax.inject.Inject


class CreateGroupViewModel @Inject constructor() : BaseViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    val liveRoomItems: MutableLiveData<List<RoomItem>> = MutableLiveData(ArrayList())

    var liveAvatarLocalUri: MutableLiveData<String> = MutableLiveData()

    init {
        database.getUserRooms(
                userId = sessionManager.curUser!!.id!!,
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
            storage.addFileAndGetDownloadUrl(
                    avatarLocalPath,
                    avatarStoragePath,
                    Storage.FILE_TYPE_IMAGE
            ) {downloadUrl->
                //change from localPath to storagePath
                newRoom.avatarUrl = downloadUrl

                database.addRoomAndUserRoom(newRoom, callback)
            }
        } else {
            database.addRoomAndUserRoom(newRoom, callback)
        }
    }
}