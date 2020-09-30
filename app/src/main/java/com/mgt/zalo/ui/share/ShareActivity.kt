package com.mgt.zalo.ui.share

import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_share.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity
import com.mgt.zalo.base.NotScrollableLinearLayoutManager
import com.mgt.zalo.common.ChooseRoomItemAdapter
import com.mgt.zalo.common.SelectRoomItemAdapter
import com.mgt.zalo.data_model.room.Room
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.util.RoomItemDiffCallback
import javax.inject.Inject

class ShareActivity : BaseActivity() {
    private lateinit var recentRecyclerViewAdapter: SelectRoomItemAdapter
    private lateinit var groupRecyclerViewAdapter: SelectRoomItemAdapter
    private lateinit var peerRecyclerViewAdapter: SelectRoomItemAdapter

    @Inject lateinit var selectedRecyclerViewAdapter: ChooseRoomItemAdapter

    private val viewModel: ShareViewModel by viewModels { viewModelFactory }

    override fun onBindViews() {
        requestFullScreen()
        setContentView(R.layout.activity_share, true)

        selectedRecyclerViewAdapter = ChooseRoomItemAdapter(this, resourceManager, RoomItemDiffCallback())
        recyclerView.apply {
            adapter = selectedRecyclerViewAdapter
            layoutManager = LinearLayoutManager(this@ShareActivity, RecyclerView.HORIZONTAL, false)
        }

        recentRecyclerViewAdapter = SelectRoomItemAdapter(RoomItemDiffCallback(), false, viewModel.liveSelectedRoomItems, utils, resourceManager)
        recentRecyclerView.apply {
            adapter = recentRecyclerViewAdapter
            layoutManager = NotScrollableLinearLayoutManager(this@ShareActivity)
            isNestedScrollingEnabled = false
        }

        groupRecyclerViewAdapter = SelectRoomItemAdapter(RoomItemDiffCallback(), false, viewModel.liveSelectedRoomItems, utils, resourceManager)
        groupRecyclerView.apply {
            adapter = groupRecyclerViewAdapter
            layoutManager = NotScrollableLinearLayoutManager(this@ShareActivity)
            isNestedScrollingEnabled = false
        }

        peerRecyclerViewAdapter = SelectRoomItemAdapter(RoomItemDiffCallback(), false, viewModel.liveSelectedRoomItems, utils, resourceManager)
        peerRecyclerView.apply {
            adapter = peerRecyclerViewAdapter
            layoutManager = NotScrollableLinearLayoutManager(this@ShareActivity)
            isNestedScrollingEnabled = false
        }

        sendImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveSelectedRoomItems.observe(this, Observer {
            val oldSize = selectedRecyclerViewAdapter.currentList.size
            selectedRecyclerViewAdapter.submitList(it) {
                if (it.size > oldSize) {
                    recyclerView.scrollToPosition(selectedRecyclerViewAdapter.itemCount - 1)
                }
            }

            recentRecyclerViewAdapter.submitList(it)
            groupRecyclerViewAdapter.submitList(it)
            peerRecyclerViewAdapter.submitList(it)

            countTextView.text = String.format(getString(R.string.description_selected_count), it.size)
            if (it.isEmpty()) {
                selectedListLayout.visibility = View.GONE
            } else {
                selectedListLayout.visibility = View.VISIBLE
            }
        })

        viewModel.liveRoomItems.observe(this, Observer { roomItems ->
            recentRecyclerViewAdapter.submitList(roomItems.sortedByDescending { it.lastMsgTime }.take(6))
            groupRecyclerViewAdapter.submitList(roomItems.filter { it.roomType == Room.TYPE_GROUP }.sortedBy { it.getDisplayName(resourceManager) }.take(6))
            peerRecyclerViewAdapter.submitList(roomItems.filter { it.roomType == Room.TYPE_PEER }.sortedBy { it.getDisplayName(resourceManager) }.take(5))
        })
    }

    private fun processNewClickOnItem(item: RoomItem) {
        viewModel.liveSelectedRoomItems.value = viewModel.liveSelectedRoomItems.value!!.apply {
            if (contains(item)) {
                remove(item)
            } else {
                add(item)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.itemUserIconRootLayout -> {
                val itemPosition = recyclerView.getChildAdapterPosition(view)
                val item = selectedRecyclerViewAdapter.currentList[itemPosition]
                processNewClickOnItem(item)
            }
            R.id.sendImgView -> {
                viewModel.addNewMessagesToFirestore { isSuccess ->
                    processingDialog.dismiss()
                    Toast.makeText(this,
                            if (isSuccess) {
                                "Đã gửi"
                            } else {
                                "Có lỗi xảy ra"
                            }, Toast.LENGTH_SHORT).show()
                    finish()
                }
                processingDialog.show(supportFragmentManager)
            }
            R.id.backImgView -> {
                utils.hideKeyboard(rootView)
                finish()
            }
        }
    }
}