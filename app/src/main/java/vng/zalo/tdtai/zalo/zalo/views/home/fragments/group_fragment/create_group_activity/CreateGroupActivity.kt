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
    val selectedPeople = ArrayList<RoomItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        with(viewPager){
            adapter = CreateGroupActivityAdapter(supportFragmentManager)
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        tabLayout.addOnTabSelectedListener(this)
        searchView.setOnClickListener(this)
    }

    fun proceedNewClickOnItem(item:RoomItem){
        if(selectedPeople.contains(item)){
            selectedPeople.remove(item)
        } else {
            selectedPeople.add(item)
        }
        Log.d(TAG, selectedPeople.toString())
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
            R.id.searchView -> searchView.isIconified = false
        }
    }

    companion object{
        val TAG = CreateGroupActivity::class.java.simpleName
    }
}