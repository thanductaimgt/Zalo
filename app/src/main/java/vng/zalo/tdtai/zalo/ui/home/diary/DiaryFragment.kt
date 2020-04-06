package vng.zalo.tdtai.zalo.ui.home.diary

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_diary.*
import kotlinx.android.synthetic.main.item_story_preview.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.common.StoryPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.ui.create_post.CreatePostActivity
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class DiaryFragment : BaseFragment() {
    private val viewModel: DiaryViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var diaryAdapter: DiaryAdapter

    @Inject
    lateinit var storyPreviewAdapter: StoryPreviewAdapter

    @Inject
    lateinit var homeActivity: HomeActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_diary, container, false).apply {
            makeRoomForStatusBar(requireActivity(), this)
        }
    }

    override fun onBindViews() {
        postRecyclerView.adapter = diaryAdapter
        storyRecyclerView.adapter = storyPreviewAdapter

        Picasso.get().smartLoad(sessionManager.curUser!!.avatarUrl, resourceManager, avatarImgView2) {
            it.fit().centerCrop()
        }

        avatarImgView2.setOnClickListener(this)
        createPostTV.setOnClickListener(this)
        cameraImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveDiaries.observe(viewLifecycleOwner, Observer {
            diaryAdapter.submitList(it)
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
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.storyPreviewItemRoot -> {
                val position = storyRecyclerView.getChildAdapterPosition(view)
                val storyGroup = storyPreviewAdapter.currentList[position] as StoryGroup

                if (storyGroup.id == StoryGroup.ID_CREATE_STORY) {
                    homeActivity.openCamera()
                } else {
                    view.apply {
                        borderView.visibility = View.GONE
                        progressBar.visibility = View.VISIBLE
                        loadingAnimView.visibility = View.VISIBLE
                    }

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
                        homeActivity.addStoryFragment(storyGroup, viewModel.liveStoryGroups.value!!)

                        view.apply {
                            borderView.visibility = View.VISIBLE
                            progressBar.visibility = View.GONE
                            loadingAnimView.visibility = View.GONE
                        }
                    }
                }
            }
            R.id.cameraImgView -> homeActivity.openCamera()
            R.id.avatarImgView -> {
                addProfileFragment(view)
            }
            R.id.avatarImgView2 -> {
                homeActivity.addProfileFragment(sessionManager.curUser!!.id!!)
            }
            R.id.nameTextView -> {
                addProfileFragment(view)
            }
            R.id.createPostTV -> {
                startActivity(Intent(requireActivity(), CreatePostActivity::class.java))
            }
            R.id.playerView -> {

            }
            R.id.imageView -> {

            }
        }
    }

    private fun addProfileFragment(view: View) {
        val position = postRecyclerView.getChildAdapterPosition(view.parent as View)
        val post = diaryAdapter.currentList[position]
        homeActivity.addProfileFragment(post.ownerId!!)
    }
}