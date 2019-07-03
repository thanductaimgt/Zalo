package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_create_group.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.models.RoomItem

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener, TabLayout.OnTabSelectedListener {
    val selectedList = HashSet<RoomItem>()
    lateinit var adapter: CreateGroupActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initView()
    }

    private fun initView(){
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = CreateGroupActivityAdapter(supportFragmentManager)
        with(viewPager){
            adapter = this@CreateGroupActivity.adapter
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        tabLayout.addOnTabSelectedListener(this)
        searchView.setOnClickListener(this)
    }

    fun proceedNewClickOnItem(item:RoomItem){
        selectedList.apply {
            if(contains(item)){
                remove(item)
            } else {
                add(item)
            }
            Log.d(TAG, this.toString())
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager.currentItem = tab.position
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.searchView -> {
                searchView.isIconified = false
                searchViewDescTV.visibility = View.GONE
            }
        }
    }

    companion object{
        val TAG = CreateGroupActivity::class.java.simpleName
    }
}