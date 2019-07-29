package vng.zalo.tdtai.zalo.zalo.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import vng.zalo.tdtai.zalo.zalo.networks.Storage
import java.util.*

class StickerSetViewModel(bucket_name: String) : ViewModel() {

    val liveStickerLinks: MutableLiveData<List<String>> = MutableLiveData(ArrayList())

    init {
        Storage.getStickerSetUrls(
                bucket_name = bucket_name
        ) { liveStickerLinks.value = it }
    }
}