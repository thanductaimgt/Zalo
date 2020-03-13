package vng.zalo.tdtai.zalo.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.PopupMenu
import androidx.viewpager.widget.ViewPager
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_home.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupActivity
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject


class HomeActivity : DaggerAppCompatActivity(), View.OnClickListener {
    @Inject lateinit var utils: Utils
    @Inject lateinit var database: Database

    @Inject lateinit var adapter: HomeAdapter

    override fun onClick(v: View) {
        when (v.id) {
//            R.id.searchImgView -> {
//                utils.showKeyboard(searchEditText)
//            }
//            R.id.createGroupImgView -> {
//                startActivity(Intent(this, CreateGroupActivity::class.java))
//            }
//            R.id.moreImgView -> displayPopupMenu(v)
        }
    }

    private lateinit var prevMenuItem: MenuItem

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

        database.setCurrentUserOnlineState(true)
    }

    private fun initView() {
        setContentView(R.layout.activity_home)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        prevMenuItem = bottomNavigationView.menu.getItem(0)

        viewPager.apply {
            adapter = this@HomeActivity.adapter
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
                R.id.navigation_chat -> viewPager.currentItem = 1
//                R.id.navigation_contacts -> viewPager.currentItem = 1
//                R.id.navigation_groups -> viewPager.currentItem = 2
                R.id.navigation_diary -> viewPager.currentItem = 0
                R.id.navigation_more -> viewPager.currentItem = 2
                R.id.navigation_test -> viewPager.currentItem = 3
            }
            false
        }

//        searchImgView.setOnClickListener(this)
//        createGroupImgView.setOnClickListener(this)
//        moreImgView.setOnClickListener(this)
    }

//    private fun displayPopupMenu(view: View) {
//        //Creating the instance of PopupMenu
//        val popupMenu = PopupMenu(this, view)
//
//        popupMenu.menuInflater.inflate(R.menu.menu_home_activity, popupMenu.menu)
//        popupMenu.setOnMenuItemClickListener {
//            when (it.itemId) {
//
//            }
//            true
//        }
//
//        popupMenu.show() //showing popup menu
//    }
}