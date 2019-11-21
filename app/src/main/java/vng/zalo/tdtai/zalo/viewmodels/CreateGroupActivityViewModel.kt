package vng.zalo.tdtai.zalo.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class CreateGroupActivityViewModel : ViewModel() {
    val liveSelectedRoomItems: MutableLiveData<ArrayList<RoomItem>> = MutableLiveData(ArrayList())

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