package vng.zalo.tdtai.zalo.ui.create_group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_select_contacts.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.common.SelectRoomItemAdapter
import vng.zalo.tdtai.zalo.util.RoomItemDiffCallback

class RecentContactsFragment : BaseFragment() {
    private val viewModel: CreateGroupViewModel by viewModels ({requireActivity()}, { viewModelFactory })

    private lateinit var adapter: SelectRoomItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_select_contacts, container, false)
    }

    override fun onBindViews() {
        adapter = SelectRoomItemAdapter(RoomItemDiffCallback(), true, viewModel.liveSelectedRoomItems, utils, resourceManager)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@RecentContactsFragment.adapter
            isNestedScrollingEnabled = false
        }
    }

    override fun onViewsBound() {
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer { roomItems ->
            adapter.submitList(roomItems.sortedByDescending { it.lastMsgTime })
        })

        viewModel.liveSelectedRoomItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
}