package vng.zalo.tdtai.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.io.File


class CreateGroupActivityViewModel : ViewModel() {
    val liveRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

    fun createRoomInFireStore(newRoom: Room, callback: ((roomItem: RoomItem) -> Unit)? = null) {
        val newRoomId = Database.getNewRoomId()

        // path to save room avatarUrl in storage
        val fileStoragePath = "${Constants.FOLDER_ROOM_AVATARS}/$newRoomId"

        newRoom.id = newRoomId
        // save room avatarUrl in storage
        if (newRoom.avatarUrl != null) {
            Storage.addFile(
                    localPath = newRoom.avatarUrl!!,
                    storagePath = fileStoragePath
            ) {
                // delete local file
                File(newRoom.avatarUrl).delete()

                //change from localPath to storagePath
                Storage.getReference(fileStoragePath).downloadUrl.addOnCompleteListener { task->
                    Utils.assertTaskSuccessAndResultNotNull(task, TAG, "createRoomInFireStore"){result->
                        newRoom.avatarUrl = result.toString()

                        Database.addRoomAndUserRoom(newRoom) {
                            callback?.invoke(it)
                        }
                    }
                }
            }
        } else {
            Database.addRoomAndUserRoom(newRoom) {
                callback?.invoke(it)
            }
        }
    }
}