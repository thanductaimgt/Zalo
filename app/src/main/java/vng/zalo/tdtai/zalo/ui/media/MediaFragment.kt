package vng.zalo.tdtai.zalo.ui.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.fragment_media.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.data_model.message.ImageMessage
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.data_model.message.ResourceMessage
import javax.inject.Inject

class MediaFragment(
        private val activity: BaseActivity,
        private val curMessage: Message,
        val messages: List<Message>
) : BaseFragment() {
    private val viewModel: MediaViewModel by viewModels{ viewModelFactory }

    @Inject
    lateinit var mediaAdapter: MediaAdapter

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    private var lastItem: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.hideStatusBar()
        return layoutInflater.inflate(R.layout.fragment_media, container, false)
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

                        imageSenderNameTextView.text = mediaAdapter.currentList[viewPager.currentItem].senderId!!//viewModel.getNameFromPhone(mediaAdapter.currentList[viewPager.currentItem].senderPhone!!)

                        loadMoreMediaIfNeeded(viewPager.currentItem)

                        lastItem = viewPager.currentItem
                    }
                }

                private fun loadMoreMediaIfNeeded(position: Int) {
                    val threshold = 5
                    val isLeftReach = position < threshold
                    val isRightReach = mediaAdapter.itemCount - position < threshold - 1

                    if (mediaAdapter.currentList[position] is ImageMessage && (isLeftReach || isRightReach)) {
                        val newList = mediaAdapter.currentList.toMutableList()

                        if (isLeftReach) {
                            val moreLeftMessages = viewModel.getMoreResourceMessages(mediaAdapter.currentList.first() as ResourceMessage, r = 0)

                            newList.addAll(0, moreLeftMessages)
                        }

                        if (isRightReach) {
                            val moreRightMessages = viewModel.getMoreResourceMessages(mediaAdapter.currentList.last() as ResourceMessage, l = 0)

                            newList.addAll(moreRightMessages)
                        }

                        if (mediaAdapter.currentList.size != newList.size) {
                            mediaAdapter.submitList(newList)
                        }
                    }
                }
            })

            mediaAdapter.submitList(messages) {
                val curPosition = messages.indexOfFirst { it.id == curMessage.id }
                viewPager.setCurrentItem(curPosition, false)
                getCurrentItemView()?.let {
                    mediaAdapter.onMediaFocused(it)
                }
            }
        }

        backImgView.setOnClickListener(this)
        downloadImgView.setOnClickListener(this)
        moreImgView.setOnClickListener(this)

        imageSenderNameTextView.text = curMessage.senderId!!//viewModel.getNameFromPhone(curMessage.senderPhone!!)
    }

    override fun onResume() {
        super.onResume()
//            if (!isPaused) {
        startOrResumeCurrentWatch()
//            }
    }

    private fun startOrResumeCurrentWatch() {
        getCurrentItemView()?.let {
            if (!mediaAdapter.isPreparing && exoPlayer.playbackState == ExoPlayer.STATE_READY) {
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
                activity.removeMediaFragment()
            }
            R.id.downloadImgView -> {
            }
            R.id.moreImgView -> {
            }
        }
    }

    private fun toggleTitleVisibility() {
        if (zoomTitleLayout.visibility == View.VISIBLE) {
            zoomTitleLayout.visibility = View.GONE
        } else {
            zoomTitleLayout.visibility = View.VISIBLE
        }
    }

    fun getCurrentItemView(): View? {
        val recyclerView = viewPager[0] as RecyclerView
        return recyclerView.findViewHolderForAdapterPosition(viewPager.currentItem)?.itemView
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.stop(true)
        activity.showStatusBar()
    }

//    override fun getInstanceTag(): String {
//        return "${super.getInstanceTag()}${curMessage.id}"
//    }
}