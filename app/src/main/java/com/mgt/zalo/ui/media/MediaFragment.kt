package com.mgt.zalo.ui.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ExoPlayer
import kotlinx.android.synthetic.main.fragment_media.*
import kotlinx.android.synthetic.main.fragment_media.view.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.data_model.media.Media
import javax.inject.Inject

class MediaFragment(
        private val curMedia: Media,
        val medias: ArrayList<Media>
) : BaseFragment() {
//    private val viewModel: MediaViewModel by viewModels{ viewModelFactory }

    @Inject
    lateinit var mediaAdapter: MediaAdapter

    private var lastItem: Int? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        activity().hideStatusBar()
        activity().setStatusBarMode(false)
        return layoutInflater.inflate(R.layout.fragment_media, container, false).apply {
            makeRoomForStatusBar(requireContext(), headerLayout)
        }
    }

    override fun onBindViews() {
        viewPager.apply {
            adapter = mediaAdapter

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    if (lastItem == null) {
                        post {
                            getCurrentItemView()?.let {
                                mediaAdapter.onMediaFocused(it)
                            }
                        }
                        lastItem = position
                    }
                }

                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE && lastItem != viewPager.currentItem) {
                        getCurrentItemView()?.let {
                            mediaAdapter.onMediaFocused(it)
                        }

//                        imageSenderNameTextView.text = mediaAdapter.currentList[viewPager.currentItem].senderId!!//viewModel.getNameFromPhone(mediaAdapter.currentList[viewPager.currentItem].senderPhone!!)

//                        loadMoreMediaIfNeeded(viewPager.currentItem)

                        lastItem = viewPager.currentItem
                    }
                }

//                private fun loadMoreMediaIfNeeded(position: Int) {
//                    val threshold = 5
//                    val isLeftReach = position < threshold
//                    val isRightReach = mediaAdapter.itemCount - position < threshold - 1
//
//                    if (mediaAdapter.currentList[position] is ImageMessage && (isLeftReach || isRightReach)) {
//                        val newList = mediaAdapter.currentList.toMutableList()
//
//                        if (isLeftReach) {
//                            val moreLeftMessages = viewModel.getMoreResourceMessages(mediaAdapter.currentList.first() as ResourceMessage, r = 0)
//
//                            newList.addAll(0, moreLeftMessages)
//                        }
//
//                        if (isRightReach) {
//                            val moreRightMessages = viewModel.getMoreResourceMessages(mediaAdapter.currentList.last() as ResourceMessage, l = 0)
//
//                            newList.addAll(moreRightMessages)
//                        }
//
//                        if (mediaAdapter.currentList.size != newList.size) {
//                            mediaAdapter.submitList(newList)
//                        }
//                    }
//                }
            })

            mediaAdapter.submitList(medias) {
                val curPosition = medias.indexOfFirst { it.uri == curMedia.uri }
                viewPager.setCurrentItem(curPosition, false)
                getCurrentItemView()?.let {
                    mediaAdapter.onMediaFocused(it)
                }
            }
        }

        backImgView.setOnClickListener(this)
        downloadImgView.setOnClickListener(this)
        moreImgView.setOnClickListener(this)

//        imageSenderNameTextView.text = curMedia.senderId!!//viewModel.getNameFromPhone(curMessage.senderPhone!!)
    }

    override fun onResume() {
        super.onResume()
//            if (!isPaused) {
        startOrResumeCurrentWatch()
//            }
    }

    private fun startOrResumeCurrentWatch() {
        getCurrentItemView()?.let {
            if (!playbackManager.isPreparing && playbackManager.exoPlayer.playbackState == ExoPlayer.STATE_READY) {
                mediaAdapter.onMediaResume(it)
            } else {
                mediaAdapter.onMediaFocused(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        getCurrentItemView()?.let {
            mediaAdapter.onMediaPause(it, false)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.playerView -> {
                toggleTitleVisibility()
            }
            R.id.photoView -> {
                toggleTitleVisibility()
            }
            R.id.backImgView -> {
                parent.onBackPressed()
//                parentZaloFragmentManager.removeMediaFragment()
            }
            R.id.downloadImgView -> {
            }
            R.id.moreImgView -> {
            }
        }
    }

    private fun toggleTitleVisibility() {
        if (headerLayout.visibility == View.VISIBLE) {
            headerLayout.visibility = View.GONE
        } else {
            headerLayout.visibility = View.VISIBLE
        }
    }

    fun getCurrentItemView(): View? {
        val recyclerView = viewPager[0] as RecyclerView
        return recyclerView.findViewHolderForAdapterPosition(viewPager.currentItem)?.itemView
    }

//    override fun getInstanceTag(): String {
//        return "${super.getInstanceTag()}${curMessage.id}"
//    }
}