package com.mgt.zalo.ui.profile.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile_diary.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.common.MediaPreviewAdapter
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.ui.home.HomeActivity
import com.mgt.zalo.ui.home.diary.DiaryAdapter
import com.mgt.zalo.ui.profile.ProfileFragment
import com.mgt.zalo.ui.profile.ProfileViewModel
import com.mgt.zalo.util.DiaryDiffCallback
import com.mgt.zalo.widget.MediaGridView
import javax.inject.Inject

class ProfileDiaryFragment : BaseFragment() {
    @Inject
    lateinit var profileFragment: ProfileFragment

    private val viewModel: ProfileViewModel by viewModels({ profileFragment }, { viewModelFactory })

    private lateinit var diaryAdapter: DiaryAdapter
    private lateinit var recyclerViewLayoutManager: LinearLayoutManager

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_profile_diary, container, false)
    }

    override fun onBindViews() {
        recyclerViewLayoutManager = LinearLayoutManager(requireContext())
        diaryAdapter = DiaryAdapter(this, resourceManager, utils, playbackManager, sessionManager, DiaryDiffCallback())
        recyclerView.apply {
            adapter = diaryAdapter
            layoutManager = recyclerViewLayoutManager
        }
    }

    override fun onViewsBound() {
        viewModel.liveDiaries.observe(viewLifecycleOwner, Observer {
            diaryAdapter.submitList(it)
        })

        viewModel.liveSelectedDiary.observe(viewLifecycleOwner, Observer { post ->
            val position = diaryAdapter.currentList.indexOfFirst { it.id == post.id }
            if (position != -1) {
                recyclerViewLayoutManager.scrollToPositionWithOffset(position, 0)
                profileFragment.viewPager.currentItem = 0
            } else {
                Toast.makeText(requireContext(), "post not loaded", Toast.LENGTH_SHORT).show()
            }
        })

        if (activity() is HomeActivity) {
            val homeActivity = activity() as HomeActivity
            homeActivity.liveSelectedPageListener.observe(viewLifecycleOwner, Observer { position ->
                if (position == 4) {
                    recyclerView.apply {
                        val firstVisiblePosition = recyclerViewLayoutManager.findFirstVisibleItemPosition()
                        if (firstVisiblePosition < 10) {
                            smoothScrollToPosition(0)
                        } else {
                            recyclerViewLayoutManager.scrollToPositionWithOffset(0, 0)
                        }
                    }
                }
            })
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imageView -> {
//                homeActivity.addMediaFragment()
            }
            R.id.avatarImgView -> {
                addProfileFragment(view)
            }
            R.id.nameTextView -> {
                addProfileFragment(view)
            }
            R.id.rootItemView -> {
                val mediaGridView = view.parent as MediaGridView

                val diaryPosition = recyclerView.getChildAdapterPosition(mediaGridView.parent as View)
                val diary = diaryAdapter.currentList[diaryPosition]

                val mediaPosition = mediaGridView.getChildAdapterPosition(view)
                val media = (mediaGridView.adapter as MediaPreviewAdapter).currentList[mediaPosition]

                if (mediaGridView.adapter!!.itemCount > 1) {
                    if (profileFragment.isOnTop) {
                        parentZaloFragmentManager
                    } else {
                        activity().zaloFragmentManager
                    }.addPostDetailFragment(diary, mediaPosition)
                } else {
                    if (media is VideoMedia) {
                        (mediaGridView.findViewHolderForAdapterPosition(mediaPosition) as MediaPreviewAdapter.VideoMediaPlayableHolder).playResumeVideo(media)
                    } else {
                        if (profileFragment.isOnTop) {
                            parentZaloFragmentManager
                        } else {
                            activity().zaloFragmentManager
                        }.addMediaFragment(diary.medias[0], diary.medias)
                    }
                }
            }
        }
    }

    private fun addProfileFragment(view: View) {
        val position = recyclerView.getChildAdapterPosition(view.parent as View)
        val post = diaryAdapter.currentList[position]
        profileFragment.fragmentManager().addProfileFragment(post.ownerId!!)
    }
}