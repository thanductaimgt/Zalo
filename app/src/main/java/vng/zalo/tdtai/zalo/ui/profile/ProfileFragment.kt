package vng.zalo.tdtai.zalo.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.item_story_preview.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.base.BaseView
import vng.zalo.tdtai.zalo.common.StoryPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class ProfileFragment(
        val userId: String
) : BaseFragment() {
    private val viewModel: ProfileViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var pagerAdapter: ProfilePagerAdapter

    @Inject
    lateinit var storyPreviewAdapter: StoryPreviewAdapter

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        // Inflate the layout for this fragment
        activity().showStatusBar()
        return inflater.inflate(R.layout.fragment_profile, container, false).apply {
            makeRoomForStatusBar(requireContext(), this)
        }
    }

    override fun onBindViews() {
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = 2
        TabLayoutMediator(tabLayout, viewPager,
                TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                    tab.icon = ContextCompat.getDrawable(requireContext(),
                            when (position) {
                                0 -> R.drawable.diary
                                else -> R.drawable.grid
                            }
                    )
                    /*tab.text = "OBJECT " + (position + 1)*/
                }
        ).attach()

        storyGroupRecyclerView.apply {
            adapter = storyPreviewAdapter
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        }

        if (isOnTop) {
            backImgView.visibility = View.VISIBLE
            backImgView.setOnClickListener(this)
        } else {
            backImgView.visibility = View.GONE
            idTextView.updatePadding(left = nameTextView.paddingLeft + utils.dpToPx(16).toInt())
        }

//        appBarLayout.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
//            override fun onStateChanged(state: State) {
//                when (state) {
//                    State.EXPANDED -> swipeRefresh.isEnabled = true
//                    else -> swipeRefresh.isEnabled = false
//                }
//            }
//        })
//
//        swipeRefresh.setOnRefreshListener {
//            viewModel.refreshProfile {
//                swipeRefresh.isRefreshing = false
//            }
//        }

        if (userId == sessionManager.curUser!!.id) {
            moreImgView.visibility = View.GONE
            settingsImgView.visibility = View.VISIBLE
            settingsImgView.setOnClickListener(this)
        } else {
            moreImgView.visibility = View.VISIBLE
            moreImgView.setOnClickListener(this)
        }

        avatarImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveUser.observe(viewLifecycleOwner, Observer { user ->
            Picasso.get().smartLoad(user.avatarUrl, resourceManager, avatarImgView) {
                it.fit().centerCrop()
            }

            idTextView.text = user.id
            nameTextView.text = user.name
            postNumTextView.text = 400.toString()
            followerNumTextView.text = 400.toString()
            followingNumTextView.text = 400.toString()
            descTextView.text = "what a lovely day !"
            followedByTextView.text = "Theo dõi bởi thutrang20"

            viewModel.listenForNewStory()
        })

        viewModel.liveStoryGroups.observe(viewLifecycleOwner, Observer { storyGroups ->
            val recentStoryGroup = storyGroups.firstOrNull { it.id == StoryGroup.ID_RECENT_STORY_GROUP }

            if (recentStoryGroup == null && viewModel.liveIsAnyStory.value == true) {
                storyPreviewAdapter.submitList(storyGroups.toMutableList().apply {
                    viewModel.liveUser.value?.let { user ->
                        add(0, StoryGroup(
                                id = StoryGroup.ID_RECENT_STORY_GROUP,
                                ownerId = user.id,
                                ownerName = user.name,
                                ownerAvatarUrl = user.avatarUrl
                        ))
                    }
                })
            } else {
                storyPreviewAdapter.submitList(storyGroups)
            }
        })

        viewModel.liveIsAnyStory.observe(viewLifecycleOwner, Observer { isAnyStory ->
            if (isAnyStory) {
                viewModel.liveStoryGroups.value = viewModel.liveStoryGroups.value
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.storyPreviewItemRoot -> {
                view.loadingAnimView.visibility = View.VISIBLE

                val position = storyGroupRecyclerView.getChildAdapterPosition(view)
                val storyGroup = storyPreviewAdapter.currentList[position]

                database.getStories(arrayListOf(storyGroup)) {
                    parentZaloFragmentManager.addStoryFragment(storyGroup, storyPreviewAdapter.currentList)

                    view.loadingAnimView.visibility = View.GONE
                }
            }
            R.id.avatarImgView -> {
//                (requireActivity() as BaseActivity).addMediaFragment(R.id.rootView, )
            }
            R.id.backImgView -> onBackPressedCustomized()
        }
    }

    override fun onBackPressedCustomized(): Boolean {
        parentZaloFragmentManager.removeProfileFragment()
        parent.onFragmentResult(BaseView.FRAGMENT_PROFILE, null)
        return true
    }

//    override fun getInstanceTag(): String {
//        return "${super.getInstanceTag()}$userId"
//    }
}