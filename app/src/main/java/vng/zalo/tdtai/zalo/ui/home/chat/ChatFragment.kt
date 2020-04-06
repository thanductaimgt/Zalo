package vng.zalo.tdtai.zalo.ui.home.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.data_model.room.RoomItemPeer
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupActivity
import vng.zalo.tdtai.zalo.util.Constants
import javax.inject.Inject


class ChatFragment: BaseFragment() {
    private val viewModel: ChatFragmentViewModel by viewModels{ viewModelFactory }

    @Inject lateinit var adapter: ChatFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false).apply {
            makeRoomForStatusBar(requireActivity(), this)
        }
    }

    override fun onBindViews() {
        recyclerView.apply {
            adapter = this@ChatFragment.adapter
            layoutManager = LinearLayoutManager(activity)
            setItemViewCacheSize(25)
        }

        createGroupButton.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveRoomItemMap.observe(viewLifecycleOwner, Observer { roomItemMap ->
            adapter.submitList(roomItemMap.values.sortedByDescending { it.lastMsgTime }) {
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

    override fun onClick(view: View) {
        when (view.id) {
            R.id.itemRoomRootLayout -> {
                val position = recyclerView.getChildAdapterPosition(view)
                val roomItem = adapter.currentList[position]

                startActivity(
                        Intent(activity, ChatActivity::class.java).apply {
                            putExtra(Constants.ROOM_NAME, roomItem.name)
                            putExtra(Constants.ROOM_AVATAR, roomItem.avatarUrl)
                            putExtra(Constants.ROOM_ID, roomItem.roomId)

                            if (roomItem is RoomItemPeer) {
                                putExtra(Constants.ROOM_PHONE, roomItem.peerId)
                            }
                        }
                )
            }
            R.id.createGroupButton->startActivity(Intent(requireContext(), CreateGroupActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.removeListeners()
    }
}