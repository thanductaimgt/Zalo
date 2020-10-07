package com.mgt.zalo.ui.home.watch

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
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.data_model.post.Watch
import com.mgt.zalo.ui.home.HomeActivity
import com.mgt.zalo.util.TAG
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.fragment_watch.*
import kotlinx.android.synthetic.main.fragment_watch.viewPager
import kotlinx.android.synthetic.main.item_watch.view.*
import javax.inject.Inject


class WatchFragment : BaseFragment() {
    private val viewModel: WatchViewModel by viewModels({ requireActivity() }, { viewModelFactory })

    @Inject
    lateinit var watchAdapter: WatchAdapter

    @Inject
    lateinit var homeActivity: HomeActivity

    var isPaused = false

    private var lastItem: Int? = null

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
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
        homeActivity.swipeRefresh.isEnabled = true
    }

    fun startOrResumeCurrentWatch() {
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
                        if (viewModel.shouldLoadMoreWatches() && viewPager.currentItem > watchAdapter.itemCount - 3) {
                            viewModel.loadMoreWatches()
                        }

                        lastItem = viewPager.currentItem
                    }

                    homeActivity.setViewPagerScrollable(state == ViewPager2.SCROLL_STATE_IDLE)
                }
            })
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
                if (homeActivity.swipeRefresh.isRefreshing) {
                    getCurrentItemView()?.let { itemView ->
                        watchAdapter.onWatchFocused(itemView)
                    }
                    homeActivity.swipeRefresh.isRefreshing = false
                }
            }
        })

        homeActivity.liveSelectedPageListener.observe(viewLifecycleOwner, Observer { position ->
            if (position == 3) {
                viewPager.apply {
                    if (currentItem < 10) {
                        currentItem = 0
                    } else {
                        setCurrentItem(0, false)
                    }
                }
            }
        })

        homeActivity.liveIsRefreshing.observe(viewLifecycleOwner, Observer {
            if(it && homeActivity.viewPager.currentItem == 3){
                viewModel.refreshRecentWatches()
            }
        })
    }

    fun getCurrentItemView(): View? {
        val recyclerView = viewPager[0] as RecyclerView
        return recyclerView.findViewHolderForAdapterPosition(viewPager.currentItem)?.itemView
    }

    fun getCurrentItem(): Int {
        return viewPager.currentItem
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
            R.id.reactImgView -> emoji()
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

    private fun avatar() {
        isPaused = true
        getCurrentItemView()?.let { watchAdapter.onWatchPause(it) }
        playbackManager.pause()
        val watch = watchAdapter.currentList[getCurrentItem()]
        parentZaloFragmentManager.addProfileFragment(watch.ownerId!!)
    }
}