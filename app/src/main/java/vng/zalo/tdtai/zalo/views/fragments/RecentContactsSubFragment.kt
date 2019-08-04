package vng.zalo.tdtai.zalo.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.item_select_contact.view.*
import kotlinx.android.synthetic.main.sub_fragment_recent_contacts.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.RecentContactsSubFragmentAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.viewmodels.RecentContactsSubFragmentViewModel
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity

class RecentContactsSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: RecentContactsSubFragmentViewModel
    private lateinit var adapter: RecentContactsSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_recent_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(RecentContactsSubFragmentViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer {
            adapter.roomItems = it
            adapter.notifyDataSetChanged()
        })

        (activity as CreateGroupActivity).viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer {
            updateRoomItemsOnScreen(it as List<RoomItem>)
        })
    }

    private fun initView() {
        adapter = RecentContactsSubFragmentAdapter(this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@RecentContactsSubFragment.adapter
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRecentContactsLayout -> {
                v.radioButton.isChecked = !v.radioButton.isChecked

                val itemPosition = recyclerView.getChildLayoutPosition(v)
                val item = adapter.roomItems[itemPosition]

                (activity as CreateGroupActivity).proceedNewClickOnItem(item)
            }
        }
    }

    private fun updateRoomItemsOnScreen(newRoomItems: List<RoomItem>) {
        Log.d(TAG, "updateRoomItemsOnScreen.recyclerView size: " + recyclerView.size)
        recyclerView.forEach {
            val itemPosition = recyclerView.getChildLayoutPosition(it)
            val item = this@RecentContactsSubFragment.adapter.roomItems[itemPosition]
            it.radioButton.isChecked = newRoomItems.contains(item)
        }
    }
}