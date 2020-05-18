package vng.zalo.tdtai.zalo.ui.home.diary

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.android.synthetic.main.fragment_diary.view.*
import kotlinx.android.synthetic.main.item_story_preview.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.common.MediaPreviewAdapter
import vng.zalo.tdtai.zalo.common.StoryPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.ui.create_post.CreatePostActivity
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.util.smartLoad
import vng.zalo.tdtai.zalo.widget.AppBarStateChangeListener
import vng.zalo.tdtai.zalo.widget.MediaGridView
import javax.inject.Inject

class DiaryFragment : BaseFragment() {
    private val viewModel: DiaryViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var diaryAdapter: DiaryAdapter
    lateinit var diaryLayoutManager: LinearLayoutManager

    @Inject
    lateinit var storyPreviewAdapter: StoryPreviewAdapter

    @Inject
    lateinit var homeActivity: HomeActivity

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_diary, container, false).apply {
            makeRoomForStatusBar(requireActivity(), this.swipeRefresh)
        }
    }

    override fun onBindViews() {
        diaryRecyclerView.adapter = diaryAdapter
        diaryRecyclerView.setItemViewCacheSize(20)
        diaryLayoutManager = diaryRecyclerView.layoutManager as LinearLayoutManager

        storyRecyclerView.adapter = storyPreviewAdapter

        Picasso.get().smartLoad(sessionManager.curUser!!.avatarUrl, resourceManager, avatarImgView2) {
            it.fit().centerCrop()
        }

        appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(state: State) {
                when (state) {
                    State.EXPANDED -> swipeRefresh.isEnabled = true
                    else -> swipeRefresh.isEnabled = false
                }
            }
        })

        swipeRefresh.setOnRefreshListener {
            refreshRecentDiaries()
        }

        diaryRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                homeActivity.setViewPagerScrollable(newState == RecyclerView.SCROLL_STATE_IDLE)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val lastVisiblePosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
                if (viewModel.shouldLoadMoreDiaries() && lastVisiblePosition > diaryAdapter.itemCount - 5) {
                    viewModel.loadMoreDiaries()
                }
            }
        })

        avatarImgView2.setOnClickListener(this)
        createPostTV.setOnClickListener(this)
        cameraImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveDiaries.observe(viewLifecycleOwner, Observer {
            val shouldScrollToTop = swipeRefresh.isRefreshing
            diaryAdapter.submitList(it) {
                if (shouldScrollToTop) {
                    diaryLayoutManager.scrollToPositionWithOffset(0, 0)
                }
            }
        })

        var isFirstTime = true
        viewModel.liveStoryGroups.observe(viewLifecycleOwner, Observer { storyGroups ->
            if (isFirstTime) {
                isFirstTime = false
                return@Observer
            }

            val myStoryGroup = storyGroups.firstOrNull { it.ownerId == sessionManager.curUser!!.id }
            if (myStoryGroup == null) {
                storyPreviewAdapter.submitList(storyGroups.toMutableList().apply {
                    add(0, StoryGroup(id = StoryGroup.ID_CREATE_STORY))
                })
            } else {
                storyPreviewAdapter.submitList(storyGroups)
            }
        })

        viewModel.liveIsAnyStory.observe(viewLifecycleOwner, Observer {
            if (it) {
                viewModel.refreshRecentStoryGroup()
            }
        })

        homeActivity.liveSelectedPageListener.observe(viewLifecycleOwner, Observer { position ->
            if (position == 1) {
                diaryRecyclerView.apply {
                    val firstVisiblePosition = diaryLayoutManager.findFirstVisibleItemPosition()
                    if (firstVisiblePosition < 10) {
                        smoothScrollToPosition(0)
                    } else {
                        diaryLayoutManager.scrollToPositionWithOffset(0, 0)
                    }
                }
            }
        })
    }

    private fun refreshRecentDiaries() {
        viewModel.refreshRecentDiaries {
            swipeRefresh.isRefreshing = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.storyPreviewItemRoot -> {
                val position = storyRecyclerView.getChildAdapterPosition(view)
                val storyGroup = storyPreviewAdapter.currentList[position] as StoryGroup

                if (storyGroup.id == StoryGroup.ID_CREATE_STORY) {
                    homeActivity.openCamera()
                } else {
                    view.loadingAnimView.visibility = View.VISIBLE

                    database.getStories(arrayListOf(storyGroup)) {
//                        var loadedCount = 0
//                        var loadCount = 0
//                        val preLoadImage = { url: String ->
//                            Picasso.get().smartLoad(url, resourceManager, object : Target {
//                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
//                                    loadCount++
//                                }
//
//                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
//                                    view.loadingAnimView.visibility = View.GONE
//                                }
//
//                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
//                                    loadedCount++
//                                    Log.d(TAG, "loadedCount: $loadedCount, loadCount: $loadCount")
//                                    if (loadedCount >= loadCount) {
//                                        homeActivity.addStoryFragment(storyGroup, viewModel.liveStoryGroups.value!!)
//
//                                        view.apply {
//                                            borderView.visibility = View.VISIBLE
//                                            progressBar.visibility = View.GONE
//                                            loadingAnimView.visibility = View.GONE
//                                        }
//                                    }
//                                }
//                            })
//                        }
//
//                        storyGroup.stories!!.forEach {
//                            when (it) {
//                                is ImageStory -> preLoadImage(it.imageUrl!!)
//                                is VideoStory -> {
//                                    resourceManager.getVideoThumbUri(it.videoUrl!!) { uri ->
//                                        preLoadImage(uri)
//                                    }
//                                }
//                            }
//                        }
                        homeActivity.zaloFragmentManager.addStoryFragment(storyGroup, viewModel.liveStoryGroups.value!!)

                        view.loadingAnimView.visibility = View.GONE
                    }
                }
            }
            R.id.cameraImgView -> homeActivity.openCamera()
            R.id.avatarImgView -> {
                addProfileFragment(view)
            }
            R.id.avatarImgView2 -> {
                homeActivity.navigateProfile()
            }
            R.id.nameTextView -> {
                addProfileFragment(view)
            }
            R.id.createPostTV -> {
                startActivity(Intent(requireActivity(), CreatePostActivity::class.java))
            }
            R.id.rootItemView -> {
                val mediaGridView = view.parent as MediaGridView

                val diaryPosition = diaryRecyclerView.getChildAdapterPosition(mediaGridView.parent as View)
                val diary = diaryAdapter.currentList[diaryPosition]

                val mediaPosition = mediaGridView.getChildAdapterPosition(view)
                val media = (mediaGridView.adapter as MediaPreviewAdapter).currentList[mediaPosition]

                if (mediaGridView.adapter!!.itemCount > 1) {
                    parentZaloFragmentManager.addPostDetailFragment(diary, mediaPosition)
                } else {
                    if (media is VideoMedia) {
                        (mediaGridView.findViewHolderForAdapterPosition(mediaPosition) as MediaPreviewAdapter.VideoMediaPreviewHolder).playVideo(media)
                    } else {
                        parentZaloFragmentManager.addMediaFragment(diary.medias[0], diary.medias)
                    }
                }
            }
        }
    }

    private fun addProfileFragment(view: View) {
        val position = diaryRecyclerView.getChildAdapterPosition(view.parent as View)
        val post = diaryAdapter.currentList[position]
        homeActivity.zaloFragmentManager.addProfileFragment(post.ownerId!!)
    }
}