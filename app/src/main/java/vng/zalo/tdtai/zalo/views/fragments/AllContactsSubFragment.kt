package vng.zalo.tdtai.zalo.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.item_select_contact.view.*
import kotlinx.android.synthetic.main.sub_fragment_recent_contacts.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.AllContactsSubFragmentAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.viewmodels.AllContactsSubFragmentViewModel
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity

class AllContactsSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: AllContactsSubFragmentViewModel
    private lateinit var adapter: AllContactsSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_recent_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(AllContactsSubFragmentViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer {
            adapter.roomItems = it
            adapter.notifyDataSetChanged()
        })

        (activity as CreateGroupActivity).viewModel.liveSelectedRoomItems.observe(viewLifecycleOwner, Observer {
            updateRoomItemsOnScreen(it)
        })
    }

    private fun initView() {
        adapter = AllContactsSubFragmentAdapter(this)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@AllContactsSubFragment.adapter
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
        recyclerView.apply {
            forEach {
                val itemPosition = getChildLayoutPosition(it)
                val item = this@AllContactsSubFragment.adapter.roomItems[itemPosition]
                it.radioButton.isChecked = newRoomItems.contains(item)
            }
        }
    }
}