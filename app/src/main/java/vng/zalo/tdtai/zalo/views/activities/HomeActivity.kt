package vng.zalo.tdtai.zalo.views.activities

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_home.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.HomeAdapter

class HomeActivity : AppCompatActivity() {
    private lateinit var prevMenuItem: MenuItem
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        initView()
    }

    private fun initView() {
        setSupportActionBar(toolbar)

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
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_home_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
