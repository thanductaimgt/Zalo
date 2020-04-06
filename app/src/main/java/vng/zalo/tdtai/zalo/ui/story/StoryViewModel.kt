package vng.zalo.tdtai.zalo.ui.story

import androidx.lifecycle.MutableLiveData
import vng.zalo.tdtai.zalo.base.BaseViewModel
import vng.zalo.tdtai.zalo.data_model.story.Story
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

class StoryViewModel @Inject constructor(
        curStoryGroup: StoryGroup,
        private val storyGroups: List<StoryGroup>
) : BaseViewModel() {
    val liveStoryGroups: MutableLiveData<List<StoryGroup>> = MutableLiveData(arrayListOf(curStoryGroup))

    fun loadMoreStoryGroups(loadLeft: Boolean = false, loadRight: Boolean = false) {
        val storyGroups: ArrayList<StoryGroup> = ArrayList()
        var leftStoryGroups: List<StoryGroup> = ArrayList()
        var rightStoryGroups: List<StoryGroup> = ArrayList()

        if (loadLeft) {
            val position = this.storyGroups.indexOfFirst { it.ownerId == liveStoryGroups.value!!.first().ownerId && it.id == liveStoryGroups.value!!.first().id }
            if (position > 0) {
                leftStoryGroups = this.storyGroups.subList(max(0, position - LOAD_NUM), position)
                storyGroups.addAll(leftStoryGroups)
            }
        }
        if (loadRight) {
            val position = this.storyGroups.indexOfFirst { it.ownerId == liveStoryGroups.value!!.last().ownerId && it.id == liveStoryGroups.value!!.last().id }
            if (position < this.storyGroups.lastIndex) {
                rightStoryGroups = this.storyGroups.subList(position + 1, min(position + LOAD_NUM + 1, this.storyGroups.size))
                storyGroups.addAll(rightStoryGroups)
            }
        }

        if (storyGroups.isNotEmpty()) {
            database.getStories(storyGroups) {
                leftStoryGroups = leftStoryGroups.filter { it.stories != null }
                rightStoryGroups = rightStoryGroups.filter { it.stories != null }

                liveStoryGroups.value = ArrayList<StoryGroup>().apply {
                    addAll(leftStoryGroups)
                    addAll(liveStoryGroups.value!!)
                    addAll(rightStoryGroups)
                }
            }
        }
    }

    fun addStoryToGroup(story: Story, storyGroup: StoryGroup, alsoCreateGroup: Boolean = false, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        database.addStoryToGroup(story, storyGroup, alsoCreateGroup, callback)
    }

    fun removeStoryFromGroup(story: Story, storyGroup: StoryGroup, callback: ((isSuccess: Boolean) -> Unit)? = null) {
        database.removeStoryFromGroup(story, storyGroup, callback)
    }

    fun getAllMyStoryGroups(callback: (storyGroups: List<StoryGroup>) -> Unit) {
        database.getUserStoryGroups(sessionManager.curUser!!, callback)
    }

    companion object {
        const val LOAD_NUM = 1
    }
}