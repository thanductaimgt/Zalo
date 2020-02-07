package vng.zalo.tdtai.zalo.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.sub_fragment_recent_contacts.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.SelectRoomItemAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.viewmodels.CreateGroupActivityViewModel

class AllContactsSubFragment : Fragment() {
    private lateinit var viewModel: CreateGroupActivityViewModel
    private lateinit var adapter: SelectRoomItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_recent_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(activity!!, ViewModelFactory.getInstance()).get(CreateGroupActivityViewModel::class.java)

        initView()

        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer {roomItems->
            adapter.submitList(roomItems.sortedBy { it.getDisplayName() })
        })

        viewModel.liveSelectedRoomItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun initView() {
        adapter = SelectRoomItemAdapter(RoomItemDiffCallback(),false, viewModel.liveSelectedRoomItems)

        with(recyclerView) {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@AllContactsSubFragment.adapter
            isNestedScrollingEnabled = false
        }
    }
}