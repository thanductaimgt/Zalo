package vng.zalo.tdtai.zalo.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.squareup.picasso.Picasso
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
import java.io.IOException
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList


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
                Storage.getReference(fileStoragePath).downloadUrl.addOnCompleteListener { task ->
                    Utils.assertTaskSuccessAndResultNotNull(task, TAG, "createRoomInFireStore") { result ->
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

    @SuppressLint("CheckResult")
    fun processPickedImage(context: Context, curImageUriString: String?, newImageUri: Uri, callback: ((newImageUriString: String?) -> Unit)) {
        Single.fromCallable {
            deleteZaloFileAtUri(curImageUriString)
            Utils.cloneFileToPictureDir(context, newImageUri)
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ uriInPictureDir ->
                    callback.invoke(uriInPictureDir)
                }, { e ->
                    Log.e(TAG, "onError: processPickedImage")
                    e.printStackTrace()
                })
    }

    @SuppressLint("CheckResult")
    fun createImageFile(context: Context, callback: (file: File?) -> Unit) {
        Single.fromCallable {
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
                File.createTempFile(
                        "${Constants.FILE_PREFIX}${Date().time}", /* prefix */
                        ".jpg", /* suffix */
                        it /* directory */
                )
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { file -> callback(file) },
                        { e ->
                            Log.e(TAG, "onError: createImageFile")
                            e.printStackTrace()
                        }
                )
    }

    fun deleteZaloFileAtUri(uriString: String?) {
        uriString?.let {
            if (Utils.parseFileName(uriString).startsWith(Constants.FILE_PREFIX)) {
                File(uriString).delete()
            }
        }
    }
}