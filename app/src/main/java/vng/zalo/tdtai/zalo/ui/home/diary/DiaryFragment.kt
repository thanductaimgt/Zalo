package vng.zalo.tdtai.zalo.ui.home.diary

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.android.synthetic.main.fragment_diary.view.*
import kotlinx.android.synthetic.main.item_diary.view.*
import kotlinx.android.synthetic.main.item_story_group_preview.view.loadingAnimView
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.common.MediaPreviewAdapter
import vng.zalo.tdtai.zalo.common.StoryGroupPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.Comment
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.data_model.post.Post
import vng.zalo.tdtai.zalo.data_model.react.React
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.ui.create_post.CreatePostActivity
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.smartLoad
import vng.zalo.tdtai.zalo.widget.MediaGridView
import javax.inject.Inject


class DiaryFragment : BaseFragment() {
    private val viewModel: DiaryViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var diaryAdapter: DiaryAdapter
    lateinit var diaryLayoutManager: LinearLayoutManager

    @Inject
    lateinit var storyGroupPreviewAdapter: StoryGroupPreviewAdapter

    @Inject
    lateinit var homeActivity: HomeActivity

    private lateinit var appBarStateListener: HomeActivity.AppBarStateListener

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_diary, container, false).apply {
            makeRoomForStatusBar(requireActivity(), this.coordinatorLayout)
        }
    }

    override fun onBindViews() {
        diaryRecyclerView.apply {
            adapter = diaryAdapter
            setItemViewCacheSize(20)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

            addChildFocusChangeListener(childFocusChangeListener)
        }
        diaryLayoutManager = diaryRecyclerView.layoutManager as LinearLayoutManager

        storyRecyclerView.adapter = storyGroupPreviewAdapter

        Picasso.get().smartLoad(sessionManager.curUser!!.avatarUrl, resourceManager, avatarImgView2) {
            it.fit().centerCrop()
        }

        appBarStateListener = homeActivity.AppBarStateListener(diaryRecyclerView)
        appBarLayout.addOnOffsetChangedListener(appBarStateListener)

        avatarImgView2.setOnClickListener(this)
        createPostTV.setOnClickListener(this)
        cameraImgView.setOnClickListener(this)
    }

    private val childFocusChangeListener = { lastFocusPos: Int, curFocusPos: Int ->
        onPositionNotFocus(lastFocusPos)
        onPositionFocus(curFocusPos)
        this.lastFocusPos = curFocusPos
        Log.d(TAG, "lastFocusPos: $lastFocusPos, curFocusPos: $curFocusPos")
    }

    override fun onViewsBound() {
        viewModel.liveDiaries.observe(viewLifecycleOwner, Observer {
            val shouldScrollToTop = homeActivity.swipeRefresh.isRefreshing
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
                storyGroupPreviewAdapter.submitList(storyGroups.toMutableList().apply {
                    add(0, StoryGroup(id = StoryGroup.ID_CREATE_STORY))
                })
            } else {
                storyGroupPreviewAdapter.submitList(storyGroups)
            }
            loadingAnimView.visibility = View.GONE
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
                appBarLayout.setExpanded(true)
            }
        })

        homeActivity.liveIsRefreshing.observe(viewLifecycleOwner, Observer {
            if (it && homeActivity.viewPager.currentItem == 1) {
                refreshRecentDiaries()
            }
        })
    }

    private fun refreshRecentDiaries() {
        viewModel.refreshRecentDiaries {
            homeActivity.swipeRefresh.isRefreshing = false
        }
    }

    private var lastFocusPos: Int = -1

    override fun onResume() {
        super.onResume()
        onPositionFocus(lastFocusPos)
        homeActivity.adjustSwipeRefresh(appBarStateListener, diaryRecyclerView)
    }

    override fun onPause() {
        super.onPause()
        onPositionNotFocus(lastFocusPos)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.storyPreviewItemRoot -> {
                val position = storyRecyclerView.getChildAdapterPosition(view)
                val storyGroup = storyGroupPreviewAdapter.currentList[position] as StoryGroup

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
                        playVideoAtPosition(diaryPosition, mediaPosition)
                    } else {
                        parentZaloFragmentManager.addMediaFragment(diary.medias[0], diary.medias)
                    }
                }
            }
            R.id.commentImgView -> {
                val position = diaryRecyclerView.getChildAdapterPosition(view.parent.parent as View)
                val diary = diaryAdapter.currentList[position]
                fragmentManager().addCommentFragment(diary)
                fragmentManager().commentFragment!!.addDismissListener {
                    diaryAdapter.notifyItemChanged(position, arrayListOf(Post.PAYLOAD_METRICS))
                }
            }
            R.id.reactImgView -> {
                val position = diaryRecyclerView.getChildAdapterPosition(view.parent.parent as View)
                val diary = diaryAdapter.currentList[position]
                val reactedType = React.TYPE_LOVE

                val curUser = sessionManager.curUser!!
                if (diary.reacts[curUser.id] == null) {
                    database.reactPost(diary, reactedType)
                    diary.apply {
                        reacts[curUser.id!!] = React(
                                ownerId = curUser.id,
                                ownerName = curUser.name,
                                ownerAvatarUrl = curUser.avatarUrl,
                                type = reactedType,
                                createdTime = System.currentTimeMillis()
                        )
                        reactCount++
                    }
                } else {
                    database.unReactPost(diary)
                    diary.reacts.remove(curUser.id!!)
                    diary.reactCount--
                }
                diaryAdapter.notifyItemChanged(position, arrayListOf(Comment.PAYLOAD_METRICS))
            }
            R.id.moreImgView->{
                val position = diaryRecyclerView.getChildAdapterPosition(view.parent as View)
                val diary = diaryAdapter.currentList[position]

                displayPopupMenu(view, diary)
            }
            R.id.postActionsLayout->{
                val position = diaryRecyclerView.getChildAdapterPosition(view.parent as View)
                val diary = diaryAdapter.currentList[position]
                fragmentManager().addCommentFragment(diary)
                fragmentManager().commentFragment!!.addDismissListener {
                    diaryAdapter.notifyItemChanged(position, arrayListOf(Post.PAYLOAD_METRICS))
                }
            }
        }
    }

    private fun playVideoAtPosition(diaryPosition: Int, mediaPosition: Int) {
        diaryRecyclerView.findViewHolderForAdapterPosition(diaryPosition)?.itemView?.mediaGridView?.let { mediaGridView ->
            val media = (mediaGridView.adapter as MediaPreviewAdapter).currentList[mediaPosition] as VideoMedia
            (mediaGridView.findViewHolderForAdapterPosition(mediaPosition) as MediaPreviewAdapter.VideoMediaPlayableHolder?)?.playResumeVideo(media)
        }
    }

    private fun onPositionNotFocus(position: Int) {
        diaryRecyclerView.findViewHolderForAdapterPosition(position)?.itemView?.mediaGridView?.let { mediaGridView ->
            mediaGridView.findViewHolderForAdapterPosition(0)?.let { holder ->
                (mediaGridView.adapter as MediaPreviewAdapter).onPause(holder)
            }
        }
    }

    private fun onPositionFocus(position: Int) {
        if (position >= 0) {
            val medias = diaryAdapter.currentList[position].medias
            if (medias.size == 1 && medias[0] is VideoMedia)
                playVideoAtPosition(position, 0)
        }
    }

    private fun addProfileFragment(view: View) {
        val position = diaryRecyclerView.getChildAdapterPosition(view.parent as View)
        val post = diaryAdapter.currentList[position]
        homeActivity.zaloFragmentManager.addProfileFragment(post.ownerId!!)
    }

    private fun displayPopupMenu(view: View, diary: Diary) {
        //Creating the instance of PopupMenu
        val popupMenu = PopupMenu(context!!, view)

        val deleteId = 0
        popupMenu.menu.add(
                0,
                deleteId,
                1,
                "Delete"
        )

        //registering popup with OnMenuItemClickListener
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                deleteId -> {
                    database.deletePost(diary){isSuccess->
                        if(isSuccess){
                            Toast.makeText(requireContext(), "Delete successful", Toast.LENGTH_SHORT).show()
                            storage.deletePostData(diary)
                        }else{
                            Toast.makeText(requireContext(), "Delete fail", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            true
        }

        popupMenu.show() //showing popup menu
    }
}