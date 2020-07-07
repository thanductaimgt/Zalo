package vng.zalo.tdtai.zalo.ui.comment.react

import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.react.React
import vng.zalo.tdtai.zalo.data_model.react.ReactPage
import javax.inject.Inject


class ReactViewModel @Inject constructor(val reacts:HashMap<String, React>) : BaseViewModel() {
    val liveReactPages = MutableLiveData(ArrayList<ReactPage>())

    init {
        loadReactPages()
    }

    private fun loadReactPages() {
        val reactPages = arrayListOf(
                ReactPage(React.TYPE_ALL, arrayListOf()),
                ReactPage(React.TYPE_LIKE, arrayListOf()),
                ReactPage(React.TYPE_CARE, arrayListOf()),
                ReactPage(React.TYPE_LOVE, arrayListOf()),
                ReactPage(React.TYPE_HAHA, arrayListOf()),
                ReactPage(React.TYPE_SAD, arrayListOf()),
                ReactPage(React.TYPE_ANGRY, arrayListOf()),
                ReactPage(React.TYPE_WOW, arrayListOf())
        )

        database.getUsers(reacts.keys.toList()){users->
            users.forEach {user->
                reacts[user.id]!!.apply {
                    ownerAvatarUrl = user.avatarUrl
                    ownerName = user.name
                }
            }
            reacts.forEach { entry ->
                reactPages.forEach { reactPage ->
                    if (reactPage.reactType == entry.value.type || reactPage.reactType == React.TYPE_ALL) {
                        reactPage.reacts.add(entry.value)
                    }
                }
            }
            liveReactPages.value = reactPages
        }
    }
}