package vng.zalo.tdtai.zalo.ui.media

import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.data_model.message.MediaMessage
import javax.inject.Inject


class MediaViewModel @Inject constructor(
        messages: ArrayList<Media>
) : BaseViewModel() {
    val liveResourceMessages: MutableLiveData<List<Media>> = MutableLiveData(messages)

    fun getMoreResourceMessages(curMessage: MediaMessage, l: Int = 10, r: Int = 10, includeCurMessage: Boolean = false): ArrayList<MediaMessage> {
//        @Suppress("UNCHECKED_CAST")
//        val resourceMessages = viewModel.liveMessageMap.value!!.values
//                .filterIsInstance<ResourceMessage>()
//                .sortedByDescending { it.createdTime }
//        val position = resourceMessages.indexOfFirst { it.id == curMessage.id }
//
//        val left = max(0, position - l)
//        val leftResourceMessages = resourceMessages.subList(left, position)
//
//        val right = min(resourceMessages.size, position + 1 + r)
//        val rightResourceMessages = resourceMessages.subList(position + 1, right)
//
//        return ArrayList<ResourceMessage>().apply {
//            addAll(leftResourceMessages)
//            if (alsoAddCurMessage) {
//                add(curMessage)
//            }
//            addAll(rightResourceMessages)
//        }

        return  ArrayList()
    }
}