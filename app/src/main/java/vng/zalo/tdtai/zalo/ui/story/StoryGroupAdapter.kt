package vng.zalo.tdtai.zalo.ui.story

import android.animation.ObjectAnimator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story_base.view.*
import kotlinx.android.synthetic.main.item_story_other.view.*
import kotlinx.android.synthetic.main.item_story_self.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.data_model.story.ImageStory
import vng.zalo.tdtai.zalo.data_model.story.Story
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.data_model.story.VideoStory
import vng.zalo.tdtai.zalo.manager.PlaybackManager
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.manager.SessionManager
import vng.zalo.tdtai.zalo.util.*
import vng.zalo.tdtai.zalo.widget.StoriesProgressView
import javax.inject.Inject

class StoryGroupAdapter @Inject constructor(
        private val storyFragment: StoryFragment,
        private val resourceManager: ResourceManager,
        private val utils: Utils,
        private val sessionManager: SessionManager,
        private val playbackManager: PlaybackManager,
        diffCallback: StoryGroupDiffCallback
) : BaseListAdapter<StoryGroup, StoryGroupAdapter.StoryGroupViewHolder>(diffCallback) {
    override fun onViewDetachedFromWindow(holder: StoryGroupViewHolder) {
        Log.d(TAG, "detach")
        onStoryGroupNotFocused(holder.itemView)
    }

    override fun getItemViewType(position: Int): Int {
        return if (currentList[position].ownerId == sessionManager.curUser!!.id) {
            VIEW_TYPE_SELF
        } else {
            VIEW_TYPE_OTHER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryGroupViewHolder {
        return when (viewType) {
            VIEW_TYPE_SELF -> SelfStoryGroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_story_self, parent, false))
            else -> OtherStoryGroupViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_story_other, parent, false))
        }.apply { bindListeners() }
    }

    override fun onBindViewHolder(holder: StoryGroupViewHolder, position: Int) {
        holder.bind(position)
    }

    abstract inner class StoryGroupViewHolder(itemView: View) : BindableViewHolder(itemView) {
        override fun bind(position: Int) {
            val storyGroup = currentList[position]
            itemView.apply {
                Picasso.get().smartLoad(storyGroup.avatarUrl
                        ?: storyGroup.ownerAvatarUrl, resourceManager, avatarImgView) {
                    it.fit().centerCrop()
                }

                storiesProgressView.setStoriesCountWithDurations(storyGroup.stories!!.map { it.duration!! * 1000 })

                nameTextView.text = storyGroup.name ?: storyGroup.ownerName

                bindChanges(storyGroup.stories!![storyGroup.curPosition])
            }
        }

        private fun bindChanges(story: Story) {
            when (story) {
                is ImageStory -> bindImage(story)
                is VideoStory -> bindVideo(story)
            }

            itemView.apply {
                imageView.visibility = View.VISIBLE

                if (story.audioName != null) {
                    musicNameTextView.text = story.audioName
                    musicNameLayout.visibility = View.VISIBLE
                    musicIcon.visibility = View.VISIBLE
                } else {
                    musicNameLayout.visibility = View.GONE
                    musicIcon.visibility = View.GONE
                }

                timeTextView.text = utils.getTimeDiffFormat(story.createdTime!!)
            }
        }

        private fun bindImage(imageStory: ImageStory) {
            Picasso.get().smartLoad(imageStory.imageUrl, resourceManager, itemView.imageView) {
                it.fit().centerInside()
            }
        }

        private fun bindVideo(videoStory: VideoStory) {
            itemView.apply {
                resourceManager.getVideoThumbUri(videoStory.videoUrl!!) { uri ->
                    Picasso.get().smartLoad(uri, resourceManager, imageView) {
                        it.fit().centerInside()
                    }
                }
            }
        }

        fun nextStory(curGroupPosition: Int = storyFragment.getCurrentItemPosition()) {
            val curStoryGroup = currentList[curGroupPosition]
            val curStories = curStoryGroup.stories!!

            recycleItemView()

            curStoryGroup.curPosition++

            bindChanges(curStories[curStoryGroup.curPosition])
            onStoryGroupFocused(itemView, curGroupPosition)
        }

        fun previousStory(curGroupPosition: Int = storyFragment.getCurrentItemPosition()) {
            val curStoryGroup = currentList[curGroupPosition]
            val curStories = curStoryGroup.stories!!

            recycleItemView()

            curStoryGroup.curPosition--

            bindChanges(curStories[curStoryGroup.curPosition])
            onStoryGroupFocused(itemView, curGroupPosition)
        }

        private fun recycleItemView() {
            itemView.apply {
                imageView.setImageDrawable(null)
                musicNameTextView.isSelected = false
                playerView.player = null
                playbackManager.pause()

                imageView.visibility = View.VISIBLE
            }
        }

        open fun bindListeners() {
            itemView.apply {
                playerView.videoSurfaceView?.setOnClickListener {
                    storyFragment.onClick(playerView)
                }
                closeImgView.setOnClickListener(storyFragment)
                musicNameTextView.setOnClickListener(storyFragment)

                leftView.setOnClickListener {
                    val groupPosition = storyFragment.getCurrentItemPosition()
                    val storyGroup = currentList[groupPosition]
                    if (storyGroup.curPosition > 0) {
                        storiesProgressView.previous()
                    } else {
                        storyFragment.previousStoryGroup()
                    }
                }

                rightView.setOnClickListener {
                    val groupPosition = storyFragment.getCurrentItemPosition()
                    val storyGroup = currentList[groupPosition]
                    val storyNum = storyGroup.stories!!.size
                    if (storyGroup.curPosition < storyNum - 1) {
                        storiesProgressView.next()
                    } else {
                        storyFragment.nextStoryGroup()
                    }
                }

                val onHoldListener = { isHold: Boolean ->
                    if (isHold) {
                        storyFragment.pauseCurrentItem()
                        hideAllViews()
                        storyFragment.setEnableViewPager(false)
                    } else {
                        storyFragment.resumeCurrentItem()
                        showAllViews()
                        storyFragment.setEnableViewPager(true)
                    }
                }

                leftView.setOnHoldListener(onHoldListener)
                rightView.setOnHoldListener(onHoldListener)

                storiesProgressView.setStoriesListener(object : StoriesProgressView.StoriesListener {
                    override fun onNext() {
                        nextStory()
                    }

                    override fun onPrev() {
                        previousStory()
                    }

                    override fun onComplete() {
                        Log.d(TAG, "onComplete")
                        storyFragment.nextStoryGroup()
                    }
                })

                avatarImgView.setOnClickListener(storyFragment)
                nameTextView.setOnClickListener(storyFragment)
            }
        }

        private val hideHeaderAnimator = getNewHideAnimator(itemView.headerLayout)

        open fun hideAllViews() {
            hideHeaderAnimator.start()
        }

        open fun showAllViews() {
            hideHeaderAnimator.reverse()
        }

        protected fun getNewHideAnimator(target: Any): ObjectAnimator {
            return ObjectAnimator().apply {
                this.target = target
                setPropertyName("alpha")
                setFloatValues(1f, 0f)
                duration = 400
            }
        }
    }

    private inner class SelfStoryGroupViewHolder(itemView: View) : StoryGroupViewHolder(itemView) {
        override fun bind(position: Int) {
            super.bind(position)

            val storyGroup = currentList[position]
            val story = storyGroup.stories!![storyGroup.curPosition]

            itemView.apply {
                viewCountTextView.text = utils.getMetricFormat(story.viewCount)
                reactCountTextView.text = utils.getMetricFormat(story.reactCount)
            }
        }

        override fun bindListeners() {
            super.bindListeners()

            itemView.apply {
                viewIcon.setOnClickListener(storyFragment)
                reactIcon.setOnClickListener(storyFragment)
                shareImgView.setOnClickListener(storyFragment)
                markImgView.setOnClickListener(storyFragment)
            }
        }

        private val hideFooterAnimator = getNewHideAnimator(itemView.footerLayoutSelf)

        override fun hideAllViews() {
            super.hideAllViews()

            hideFooterAnimator.start()
        }

        override fun showAllViews() {
            super.showAllViews()

            hideFooterAnimator.reverse()
        }
    }

    private inner class OtherStoryGroupViewHolder(itemView: View) : StoryGroupViewHolder(itemView) {
        override fun bindListeners() {
            super.bindListeners()

            itemView.apply {
                cameraImgView.setOnClickListener(storyFragment)
                reactImgView.setOnClickListener(storyFragment)
            }
        }

        private val hideFooterAnimator = getNewHideAnimator(itemView.footerLayoutOther)

        override fun hideAllViews() {
            super.hideAllViews()

            hideFooterAnimator.start()
        }

        override fun showAllViews() {
            super.showAllViews()

            hideFooterAnimator.reverse()
        }
    }

    private fun getStory(groupPosition: Int): Story {
        val storyGroup = currentList[groupPosition]
        Log.d(TAG, "getStory: storyPosition ${storyGroup.curPosition}")

        return storyGroup.stories!![storyGroup.curPosition]
    }

    fun onStoryGroupResume(itemView: View, groupPosition: Int) {
        Log.d(TAG, "resume")

        itemView.apply {
            musicNameTextView.isSelected = true
            if (storiesProgressView.isStarted) {
                storiesProgressView.resume()
            } else {
                storiesProgressView.startStories(currentList[groupPosition].curPosition)
            }
        }

        val curStory = getStory(groupPosition)
        if (curStory is VideoStory) {
            onVideoResume(itemView)
        }
    }

    private fun onVideoResume(itemView: View) {
        Log.d(TAG, "onVideoResume")
        itemView.apply {
            playbackManager.play()
            imageView.visibility = View.INVISIBLE
        }
    }

    fun onStoryGroupPause(itemView: View, groupPosition: Int) {
        Log.d(TAG, "pause")

        itemView.apply {
            musicNameTextView.isSelected = false
            storiesProgressView.pause()
        }

        val curStory = getStory(groupPosition)
        if (curStory is VideoStory) {
            onVideoPause(itemView)
        }
    }

    private fun onVideoPause(itemView: View) {
        Log.d(TAG, "onVideoPause")
        itemView.apply {
            playbackManager.pause()
        }
    }

    fun onStoryGroupFocused(itemView: View, groupPosition: Int) {
        Log.d(TAG, "onStoryGroupFocused")

        itemView.storiesProgressView.pause()
        playbackManager.pause()
        when (val curStory = getStory(groupPosition)) {
            is VideoStory -> onVideoFocus(itemView, curStory)
            is ImageStory -> {
                onStoryGroupResume(itemView, groupPosition)
            }
        }
    }

    private val onReady = {
        storyFragment.resumeCurrentItem()
    }
    private val onLostFocus = {
        storyFragment.getCurrentItemView()?.let { onStoryGroupNotFocused(it) }
    }

    private fun onVideoFocus(itemView: View, videoStory: VideoStory) {
        Log.d(TAG, "onVideoFocus")
        itemView.apply {
            playbackManager.prepare(videoStory.videoUrl!!, true, onReady, onLostFocus)
            playerView.player = playbackManager.exoPlayer
        }
    }

    private fun onStoryGroupNotFocused(itemView: View) {
        Log.d(TAG, "onStoryGroupNotFocused")

        itemView.apply {
            musicNameTextView.isSelected = false
            playerView.player = null
//            exoPlayer.playWhenReady = false

            imageView.visibility = View.VISIBLE

            storiesProgressView.destroy()
        }
    }

    companion object {
        const val VIEW_TYPE_SELF = 0
        const val VIEW_TYPE_OTHER = 1
    }
}