package vng.zalo.tdtai.zalo.ui.create_group

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

class AllContactsFragment : BaseFragment() {
    private val viewModel: CreateGroupViewModel by viewModels({requireActivity()}, { viewModelFactory })

    private lateinit var adapter: SelectRoomItemAdapter

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_select_contacts, container, false)
    }

    override fun onBindViews() {
        adapter = SelectRoomItemAdapter(RoomItemDiffCallback(), false, viewModel.liveSelectedRoomItems, utils, resourceManager)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@AllContactsFragment.adapter
            isNestedScrollingEnabled = false
        }

        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer {roomItems->
            adapter.submitList(roomItems.sortedBy { it.getDisplayName(resourceManager) })
        })

        viewModel.liveSelectedRoomItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }
}