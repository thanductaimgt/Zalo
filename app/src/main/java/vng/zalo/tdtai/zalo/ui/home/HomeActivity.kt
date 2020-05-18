package vng.zalo.tdtai.zalo.ui.home

import android.animation.ObjectAnimator
import android.os.Handler
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.activity_home.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.ui.camera.CameraFragment
import javax.inject.Inject


class HomeActivity : BaseActivity() {
    @Inject
    lateinit var pagerAdapter: HomeAdapter

    private lateinit var prevMenuItem: MenuItem

    private lateinit var bottomNavigationAnimator: ObjectAnimator

    val liveSelectedPageListener = MutableLiveData<Int>()

    var cameraFragment: CameraFragment? = null

    override fun onBindViews() {
        requestFullScreen()
        setContentView(R.layout.activity_home)

        prevMenuItem = bottomNavigationView.menu.getItem(0)

        viewPager.apply {
            adapter = this@HomeActivity.pagerAdapter
            setCurrentItem(1, false)
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                    if (position == 0) {
                        if (cameraFragment?.isCameraBinding == false) {
                            showOrHideStatusBar(position)

                            cameraFragment!!.bindUseCases()
                        }
                    } else if (position == 1) {
                        if (cameraFragment?.isCameraBinding == true) {
                            showOrHideStatusBar(position)

                            cameraFragment!!.unbindAllUseCases()
                        }
                    }
                }

                override fun onPageSelected(position: Int) {
                    if (position > 0) {
                        prevMenuItem.isChecked = false
                        prevMenuItem = bottomNavigationView.menu.getItem(position - 1).apply { isChecked = true }

                        showOrHideStatusBar(position)
                    }
                }
            })
            offscreenPageLimit = 5
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            val position: Int = when (item.itemId) {
                R.id.navigation_diary -> 1
                R.id.navigation_chat -> 2
                R.id.navigation_watch -> 3
                R.id.navigation_more -> 4
                else -> 5
            }
            if(viewPager.currentItem == position){
                liveSelectedPageListener.value = position
            }else{
                viewPager.setCurrentItem(position, false)
            }
            false
        }

        bottomNavigationAnimator = ObjectAnimator().apply {
            target = bottomNavigationView
            setPropertyName("translationY")
            setFloatValues(0f, resources.getDimension(R.dimen.sizeBottomNavigationHeight))
            duration = 250
        }
    }

    override fun onViewsBound() {
        database.setCurrentUserOnlineState(true)
    }

    fun showOrHideStatusBar(position: Int) {
        if (position == 0 || position == 3 && sharedPrefsManager.isWatchTabFullScreen()) {
            hideStatusBar()
            hideBottomNavigation()
        } else {
            showBottomNavigation()
            showStatusBar()
        }
    }

    private var isBottomNavigationShown = true

    fun hideBottomNavigation() {
        if (isBottomNavigationShown) {
            isBottomNavigationShown = false
            bottomNavigationAnimator.start()
        }
    }

    fun showBottomNavigation() {
        if (!isBottomNavigationShown) {
            isBottomNavigationShown = true
            bottomNavigationAnimator.reverse()
        }
    }

    private fun navigateHome() {
        viewPager.currentItem = 1
    }

    fun navigateProfile() {
        viewPager.currentItem = 4
    }

    fun openCamera() {
        viewPager.currentItem = 0
    }

    override fun onFragmentResult(fragmentType: Int, result: Any?) {
        zaloFragmentManager.removeEditMediaFragment()
        navigateHome()
    }

    private var doubleBackToExitPressedOnce = false

    override fun onBackPressedCustomized() {
        when {
            viewPager.currentItem != 1 -> {
                navigateHome()
            }
            doubleBackToExitPressedOnce -> {
                super.onBackPressedCustomized()
            }
            else -> {
                this.doubleBackToExitPressedOnce = true
                Toast.makeText(this, getString(R.string.description_back_again_to_exit), Toast.LENGTH_SHORT).show()

                Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
            }
        }
    }

    fun setViewPagerScrollable(isScrollable: Boolean) {
        viewPager.isUserInputEnabled = isScrollable
    }
}