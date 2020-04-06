package vng.zalo.tdtai.zalo.ui.home.watch

import android.util.Log
import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.post.Watch
import vng.zalo.tdtai.zalo.util.TAG
import javax.inject.Inject

class WatchViewModel @Inject constructor() : BaseViewModel() {
    val liveWatches = MutableLiveData(ArrayList<Watch>())

    var allWatches: ArrayList<Watch>
    var count = INIT_NUM

    init {
        var now = System.currentTimeMillis()
        var id = 0

        val watch = Watch(
                id.toString(),
                now++,
                "0123456789",
                "Phương Anh Lê",
                "https://scontent-hkt1-1.xx.fbcdn.net/v/t1.0-9/88180750_969154280146996_3801549023400165376_o.jpg?_nc_cat=111&_nc_sid=09cbfe&_nc_ohc=nR-PrCdz87kAX9Z4lLi&_nc_ht=scontent-hkt1-1.xx&oh=f21ec3453b9111fecef11152c849d49c&oe=5EA2FCBB",
                "sao anh còn chưa về hả ???",
                12500000,
                98543,
                12345,
                "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FOMhTOwy0weMT2aWY0MXi%2Ffile_1584972727604?alt=media&token=708db5a2-1404-484d-9712-476390c307ce",
                15
                // ,
//                musicName = "Sao anh chưa về nhà - Amee ft. Ricky Star",
//                musicOwnerAvatarUrl = "https://png.pngtree.com/png-clipart/20190516/original/pngtree-cute-girl-avatar-material-png-image_4023832.jpg"
        )

        val para = "GIÁO VIÊN ĐƯỢC NGHỈ DỊCH Ở NHÀ, HIỆU TRƯỞNG VẪN TRẢ ĐỦ TIỀN LƯƠNG CHO GIÁO VIÊN 100%"

        allWatches = arrayListOf(
                watch,
                watch.copy(id = (id++).toString(), createdTime = now++, ownerId = "0123456789", text = para, ownerName = "1->9 brotherrrrrrrrrrrrrrrrrrrrrrrrrrrr", videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FOMhTOwy0weMT2aWY0MXi%2Ffile_1584972687745?alt=media&token=6ef29b51-01d9-4b8c-bb51-ec77ccd07e7c"),
                watch.copy(id = (id++).toString(), createdTime = now++, ownerId = "0111111111", text = "", videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FOMhTOwy0weMT2aWY0MXi%2Ffile_1584597436923?alt=media&token=eb2581c3-5096-46dc-a232-50f3c2677906"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FhLxbrt2IpNAcOaYlS7VC%2Ffile_1585396178099?alt=media&token=fcd564d7-ada6-4344-84f5-b94fc8573a27"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FUbfchgr9xPAQclxK4Obx%2Ffile_1585396332313?alt=media&token=6143fab1-84ce-4db4-b102-8c42e9d02b38"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FUbfchgr9xPAQclxK4Obx%2Ffile_1585396250534?alt=media&token=7d173e78-7fb5-464b-b3d6-9aea7872904a"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FUbfchgr9xPAQclxK4Obx%2Ffile_1585396200320?alt=media&token=18efaae3-fd1f-45e3-ae80-0d29c629b15c"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FUbfchgr9xPAQclxK4Obx%2Ffile_1585396044554?alt=media&token=bd1c5037-867f-4b00-b118-d03ca5e2aa46"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FOMhTOwy0weMT2aWY0MXi%2Ffile_1585396141590?alt=media&token=0c02928c-2390-4094-b894-e87331a3a382"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FOMhTOwy0weMT2aWY0MXi%2Ffile_1585363369998?alt=media&token=3e650941-407c-45e6-91fd-90e2ed6884d3"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FcejYs9Y0KeyLbFkf2Fh5%2Ffile_1585417724659?alt=media&token=2ccbca9b-2516-46ba-b547-58360ad46dce"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FcejYs9Y0KeyLbFkf2Fh5%2Ffile_1585415080140?alt=media&token=df03bb04-b166-406f-8c66-2255ac479f42"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FcejYs9Y0KeyLbFkf2Fh5%2Ffile_1585413558815?alt=media&token=ea0a1042-341b-4423-86af-5c986f63f9fa"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FcejYs9Y0KeyLbFkf2Fh5%2Ffile_1585413997660?alt=media&token=c6785e5e-5a00-4ae4-98f4-4c0ade1e3490"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FcejYs9Y0KeyLbFkf2Fh5%2Ffile_1585414571289?alt=media&token=7db6725a-72cd-4d8c-b6eb-88c7df70b9bb"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FcejYs9Y0KeyLbFkf2Fh5%2Ffile_1585414296100?alt=media&token=d2f6305d-5826-47c7-8856-dc07a25dd809"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FcejYs9Y0KeyLbFkf2Fh5%2Ffile_1585417798197?alt=media&token=e148e0e9-aafe-4088-b0dc-7abf43967bc4"),
                watch.copy(id = (id++).toString(), createdTime = now++, videoUrl = "https://firebasestorage.googleapis.com/v0/b/zalo-4c204.appspot.com/o/room_data%2FOMhTOwy0weMT2aWY0MXi%2Ffile_1585396280384?alt=media&token=bead0c73-4086-4aad-99f0-73184144a199")
        )

        loadWatches()
    }

    fun loadWatches(callback: (() -> Unit)? = null) {
        liveWatches.value = allWatches.take(INIT_NUM).toMutableList() as ArrayList<Watch>
        count = INIT_NUM
        callback?.invoke()
    }

    fun loadMoreWatches() {
        Log.d(TAG, "load more")
        liveWatches.value = allWatches.take(++count).toMutableList() as ArrayList<Watch>
    }

    companion object {
        const val INIT_NUM = 2
    }
}