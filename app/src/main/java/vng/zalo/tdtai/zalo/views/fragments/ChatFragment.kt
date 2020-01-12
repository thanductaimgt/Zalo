package vng.zalo.tdtai.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.RoomItemAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItemPeer
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.viewmodels.UserRoomItemsViewModel
import vng.zalo.tdtai.zalo.views.activities.RoomActivity


class ChatFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: UserRoomItemsViewModel
    private lateinit var adapter: RoomItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(UserRoomItemsViewModel::class.java)
        viewModel.liveRoomIdRoomItemMap.observe(viewLifecycleOwner, Observer { roomItems ->
            adapter.submitList(roomItems.values.toList()) {
                if (recyclerView.isNotEmpty()) {
                    // save index and top position
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val index = layoutManager.findFirstVisibleItemPosition()
                    val firstView = recyclerView[0]
                    val top = firstView.top - recyclerView.paddingTop
                    layoutManager.scrollToPositionWithOffset(index, top)
                }
            }
        })
    }

    private fun initView() {
        adapter = RoomItemAdapter(this, RoomItemDiffCallback())

        with(recyclerView) {
            adapter = this@ChatFragment.adapter
            layoutManager = LinearLayoutManager(activity)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val position = recyclerView.getChildAdapterPosition(v)
                val roomItem = adapter.currentList[position]

                startActivity(
                        Intent(activity, RoomActivity::class.java).apply {
                            putExtra(Constants.ROOM_NAME, roomItem.getDisplayName())
                            putExtra(Constants.ROOM_AVATAR, roomItem.avatarUrl)
                            putExtra(Constants.ROOM_ID, roomItem.roomId)

                            val roomType = roomItem.roomType
                            putExtra(Constants.ROOM_TYPE, roomItem.roomType)
                            if (roomType == Room.TYPE_PEER) {
                                putExtra(Constants.ROOM_PHONE, (roomItem as RoomItemPeer).phone)
                            }
                        }
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeListeners()
    }
}