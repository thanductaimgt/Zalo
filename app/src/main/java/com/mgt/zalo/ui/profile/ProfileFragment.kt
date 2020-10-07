package com.mgt.zalo.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.common.StoryGroupPreviewAdapter
import com.mgt.zalo.data_model.User
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.story.StoryGroup
import com.mgt.zalo.ui.home.HomeActivity
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.viewPager
import kotlinx.android.synthetic.main.item_story_group_preview.view.*
import javax.inject.Inject

class ProfileFragment(
        val userId: String
) : BaseFragment() {
    private val viewModel: ProfileViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var pagerAdapter: ProfilePagerAdapter

    @Inject
    lateinit var storyGroupPreviewAdapter: StoryGroupPreviewAdapter

    @Inject
    lateinit var suggestUserAdapter: SuggestUserAdapter

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
                }
        ).attach()

        storyGroupRecyclerView.adapter = storyGroupPreviewAdapter
        suggestedUsersRecyclerView.adapter = suggestUserAdapter

        if (isOnTop) {
            backImgView.visibility = View.VISIBLE
            backImgView.setOnClickListener(this)
        } else {
            backImgView.visibility = View.GONE
            idTextView.updatePadding(left = nameTextView.paddingLeft + utils.dpToPx(16))
        }

        if (userId == sessionManager.curUser!!.id) {
            moreImgView.visibility = View.GONE
            settingsImgView.visibility = View.VISIBLE
            settingsImgView.setOnClickListener(this)
        } else {
            moreImgView.visibility = View.VISIBLE
            moreImgView.setOnClickListener(this)
        }

        avatarImgView.setOnClickListener(this)
        showSuggestImgView.setOnClickListener(this)
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
                storyGroupPreviewAdapter.submitList(storyGroups.toMutableList().apply {
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
                storyGroupPreviewAdapter.submitList(storyGroups)
            }
        })

        viewModel.liveIsAnyStory.observe(viewLifecycleOwner, Observer { isAnyStory ->
            if (isAnyStory) {
                viewModel.liveStoryGroups.value = viewModel.liveStoryGroups.value
            }
        })

        viewModel.liveSuggestedUsers.observe(viewLifecycleOwner, Observer { users ->
            suggestUserAdapter.submitList(users)
        })

        if (activity() is HomeActivity) {
            val homeActivity = activity() as HomeActivity
            homeActivity.liveSelectedPageListener.observe(viewLifecycleOwner, Observer { position ->
                if (position == 4) {
                    appBarLayout.setExpanded(true)
                }
            })

            homeActivity.liveIsRefreshing.observe(viewLifecycleOwner, Observer {
                if (it && homeActivity.viewPager.currentItem == 4) {
                    viewModel.refreshProfile {
                        homeActivity.swipeRefresh.isRefreshing = false
                    }
                }
            })
        }
    }

    private fun showSuggestedUsersLayout() {
        suggestedUsersLayout.visibility = View.VISIBLE
        showSuggestImgView.rotation = 90f
    }

    private fun hideSuggestedUsersLayout() {
        suggestedUsersLayout.visibility = View.GONE
        showSuggestImgView.rotation = -90f
    }

    private fun isSuggestedUsersLayoutShown(): Boolean {
        return suggestedUsersLayout.visibility == View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        if (activity() is HomeActivity) {
            (activity() as HomeActivity).swipeRefresh.isEnabled = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.storyPreviewItemRoot -> {
                view.loadingAnimView.visibility = View.VISIBLE

                val position = storyGroupRecyclerView.getChildAdapterPosition(view)
                val storyGroup = storyGroupPreviewAdapter.currentList[position]

                database.getStories(arrayListOf(storyGroup)) {
                    parentZaloFragmentManager.addStoryFragment(storyGroup, storyGroupPreviewAdapter.currentList)

                    view.loadingAnimView.visibility = View.GONE
                }
            }
            R.id.avatarImgView -> {
                val media = ImageMedia(viewModel.liveUser.value!!.avatarUrl)
                fragmentManager().addMediaFragment(media, arrayListOf(media))
            }
            R.id.backImgView -> parent.onBackPressed()
            R.id.showSuggestImgView -> {
                if (isSuggestedUsersLayoutShown()) {
                    hideSuggestedUsersLayout()
                } else {
                    showSuggestedUsersLayout()
                }
            }
            R.id.rootUserItemView -> {
                val position = suggestedUsersRecyclerView.getChildAdapterPosition(view)
                val user = suggestUserAdapter.currentList[position]
                fragmentManager().addProfileFragment(user.id!!)
            }
            R.id.followTextView -> {
                val position = suggestedUsersRecyclerView.getChildAdapterPosition(view.parent.parent as View)
                val user = suggestUserAdapter.currentList[position]
                if (suggestUserAdapter.followingUserIds.contains(user.id)) {
                    suggestUserAdapter.followingUserIds.remove(user.id)
                } else {
                    suggestUserAdapter.followingUserIds.add(user.id!!)
                }
                suggestUserAdapter.notifyItemChanged(position, arrayListOf(User.PAYLOAD_FOLLOW))
            }
            R.id.closeImgView -> {
                val position = suggestedUsersRecyclerView.getChildAdapterPosition(view.parent.parent as View)
                val user = suggestUserAdapter.currentList[position]
                viewModel.liveSuggestedUsers.value!!.remove(user)
                suggestUserAdapter.submitList(viewModel.liveSuggestedUsers.value)
            }
        }
    }

    override fun onBackPressedCustomized(): Boolean {
        parent.onFragmentResult(BaseView.FRAGMENT_PROFILE, null)
        return false
    }

//    override fun getInstanceTag(): String {
//        return "${super.getInstanceTag()}$userId"
//    }
}