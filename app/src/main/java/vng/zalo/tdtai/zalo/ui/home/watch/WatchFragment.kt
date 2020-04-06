package vng.zalo.tdtai.zalo.ui.home.watch

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.exoplayer2.SimpleExoPlayer
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_watch.*
import kotlinx.android.synthetic.main.fragment_watch.viewPager
import kotlinx.android.synthetic.main.item_watch.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.data_model.post.Watch
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.util.TAG
import javax.inject.Inject


class WatchFragment : BaseFragment() {
    private val viewModel: WatchViewModel by viewModels({ requireActivity() }, { viewModelFactory })

    @Inject
    lateinit var watchAdapter: WatchAdapter

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var homeActivity: HomeActivity

    var isPaused = false

    private var lastItem: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_watch, container, false)
    }

    override fun onResume() {
        super.onResume()
        homeActivity.apply {
            setStatusBarMode(false)
            bottomNavigationView.background = null
            if (!isPaused) {
                startOrResumeCurrentWatch()
            }
        }
    }

    private fun startOrResumeCurrentWatch() {
        getCurrentItemView()?.let {
            if (it.playerView.player != null) {
                watchAdapter.onWatchResume(it)
            } else {
                watchAdapter.onWatchFocused(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        homeActivity.apply {
            setStatusBarMode(true)
            bottomNavigationView.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            getCurrentItemView()?.let {
                watchAdapter.onWatchPause(it, false)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        releaseCurrentFocus()
    }

    private fun releaseCurrentFocus() {
        getCurrentItemView()?.let {
            watchAdapter.onWatchNotFocused(it)
        }
        isPaused = false
    }

    override fun onBindViews() {
        viewPager.apply {
            adapter = watchAdapter
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE && lastItem != viewPager.currentItem) {
                        getCurrentItemView()?.let {
                            watchAdapter.onWatchFocused(it)
                        }

                        Log.d(TAG, "${viewPager.currentItem}, ${watchAdapter.itemCount}")
                        if (viewPager.currentItem == watchAdapter.itemCount - 1) {
                            viewModel.loadMoreWatches()
                        }

                        lastItem = viewPager.currentItem
                    }

                    enableDisableHomeViewPager(state == ViewPager2.SCROLL_STATE_IDLE)
                }
            })
        }

        swipeRefresh.setOnRefreshListener {
            viewModel.loadWatches()
        }

        fullscreenImgView.setImageResource(if (sharedPrefsManager.isWatchTabFullScreen()) {
            R.drawable.ic_fullscreen_exit_black_24dp
        } else {
            R.drawable.ic_fullscreen_black_24dp
        })
        fullscreenImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveWatches.observe(viewLifecycleOwner, Observer {
            watchAdapter.submitList(it) {
                if (swipeRefresh.isRefreshing) {
                    getCurrentItemView()?.let { itemView ->
                        watchAdapter.onWatchFocused(itemView)
                    }
                    swipeRefresh.isRefreshing = false
                }
            }
        })

        application.liveShouldReleaseVideoFocus.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "liveShouldReleaseVideoFocus: $it")
            if (it) {
                releaseCurrentFocus()
            }
        })
    }

    fun enableDisableHomeViewPager(enable: Boolean) {
        homeActivity.viewPager.isUserInputEnabled = enable
    }

    fun getCurrentItemView(): View? {
        val recyclerView = viewPager[0] as RecyclerView
        return recyclerView.findViewHolderForAdapterPosition(viewPager.currentItem)?.itemView
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.fullscreenImgView -> {
                if (sharedPrefsManager.isWatchTabFullScreen()) {
                    sharedPrefsManager.setWatchTabFullScreen(false)
                    fullscreenImgView.setImageResource(R.drawable.ic_fullscreen_black_24dp)
                    homeActivity.showStatusBar()
                    homeActivity.showBottomNavigation()
                } else {
                    sharedPrefsManager.setWatchTabFullScreen(true)
                    fullscreenImgView.setImageResource(R.drawable.ic_fullscreen_exit_black_24dp)
                    homeActivity.hideStatusBar()
                    homeActivity.hideBottomNavigation()
                }

                watchAdapter.notifyItemRangeChanged(0, watchAdapter.itemCount, arrayListOf(Watch.PAYLOAD_FULLSCREEN))
            }
            R.id.musicIcon -> showMusic()
            R.id.musicOwnerAvatarImgView -> showMusic()
            R.id.musicNameTextView -> showMusic()
            R.id.shareImgView -> share()
            R.id.commentImgView -> comment()
            R.id.emojiImgView -> emoji()
            R.id.watchOwnerAvatarImgView -> avatar()
            R.id.nameTextView -> avatar()
        }
    }

    private fun showMusic() {

    }

    private fun share() {

    }

    private fun comment() {

    }

    private fun emoji() {

    }

    private fun avatar() {}
}