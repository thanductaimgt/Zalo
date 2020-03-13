package vng.zalo.tdtai.zalo.ui.create_group

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sub_fragment_recent_contacts.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject

class RecentContactsSubFragment : DaggerFragment() {
    @Inject lateinit var utils: Utils
    @Inject lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: CreateGroupViewModel by viewModels ({requireActivity()}, { viewModelFactory })

    private lateinit var adapter: SelectRoomItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_recent_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer { roomItems ->
            adapter.submitList(roomItems.sortedByDescending { it.lastMsgTime })
        })

        viewModel.liveSelectedRoomItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })
    }

    private fun initView() {
        adapter = SelectRoomItemAdapter(RoomItemDiffCallback(), true, viewModel.liveSelectedRoomItems, utils, resourceManager)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@RecentContactsSubFragment.adapter
            isNestedScrollingEnabled = false
        }
    }
}