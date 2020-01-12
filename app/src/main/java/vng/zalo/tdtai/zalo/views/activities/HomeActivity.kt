package vng.zalo.tdtai.zalo.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.viewpager.widget.ViewPager
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelection
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_home.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.adapters.HomeAdapter
import vng.zalo.tdtai.zalo.factories.CacheDataSourceFactory
import vng.zalo.tdtai.zalo.services.NotificationService
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils


class HomeActivity : AppCompatActivity(), View.OnClickListener {
    override fun onClick(v: View) {
        when (v.id) {
            R.id.searchImgView -> {
                Utils.showKeyboard(this, searchEditText)
            }
            R.id.createGroupImgView -> {
                startActivity(Intent(this, CreateGroupActivity::class.java))
            }
            R.id.moreImgView -> displayPopupMenu(v)
        }
    }

    private lateinit var prevMenuItem: MenuItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            startService(Intent(this, NotificationService::class.java))
        }catch (e:Throwable){
            e.printStackTrace()
        }

        initView()
    }

    private fun initView() {
        setContentView(R.layout.activity_home)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        prevMenuItem = bottomNavigationView.menu.getItem(0)

        viewPager.apply {
            adapter = HomeAdapter(supportFragmentManager)
            viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                }

                override fun onPageSelected(position: Int) {
                    prevMenuItem.isChecked = false
                    prevMenuItem = bottomNavigationView.menu.getItem(position).apply { isChecked = true }
                }

                override fun onPageScrollStateChanged(state: Int) {

                }
            })
            offscreenPageLimit = 5
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chat -> viewPager.currentItem = 0
                R.id.navigation_contacts -> viewPager.currentItem = 1
                R.id.navigation_groups -> viewPager.currentItem = 2
                R.id.navigation_diary -> viewPager.currentItem = 3
                R.id.navigation_more -> viewPager.currentItem = 4
            }
            false
        }

        searchImgView.setOnClickListener(this)
        createGroupImgView.setOnClickListener(this)
        moreImgView.setOnClickListener(this)
    }

    private fun displayPopupMenu(view: View) {
        //Creating the instance of PopupMenu
        val popupMenu = PopupMenu(this, view)

        popupMenu.menuInflater.inflate(R.menu.menu_home_activity, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {

            }
            true
        }

        popupMenu.show() //showing popup menu
    }
}