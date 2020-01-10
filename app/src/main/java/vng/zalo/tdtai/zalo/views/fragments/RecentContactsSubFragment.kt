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
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity

class RecentContactsSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: CreateGroupActivityViewModel
    private lateinit var adapter: SelectRoomItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_recent_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(activity!!, ViewModelFactory.getInstance()).get(CreateGroupActivityViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer {roomItems->
            adapter.submitList(roomItems.sortedByDescending { it.lastMsgTime })
        })

        (activity as CreateGroupActivity).viewModel.liveSelectedRoomItems.observe(viewLifecycleOwner, Observer {
            adapter.notifyDataSetChanged()
        })
    }

    private fun initView() {
        adapter = SelectRoomItemAdapter(RoomItemDiffCallback(), true, this)

        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@RecentContactsSubFragment.adapter
        }
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.itemRecentContactsLayout -> {
                val position = recyclerView.getChildLayoutPosition(v)
                val roomItem = adapter.currentList[position]

                if(viewModel.liveSelectedRoomItems.value!!.contains(roomItem))
                    viewModel.liveSelectedRoomItems.value = viewModel.liveSelectedRoomItems.value!!.apply { remove(roomItem) }
                else
                    viewModel.liveSelectedRoomItems.value = viewModel.liveSelectedRoomItems.value!!.apply { add(roomItem) }
            }
        }
    }
}