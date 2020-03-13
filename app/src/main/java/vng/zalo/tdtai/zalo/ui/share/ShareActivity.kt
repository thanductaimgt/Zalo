package vng.zalo.tdtai.zalo.ui.share

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_share.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.NotScrollableLinearLayoutManager
import vng.zalo.tdtai.zalo.common.ChooseRoomItemAdapter
import vng.zalo.tdtai.zalo.common.ProcessingDialog
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.ui.create_group.SelectRoomItemAdapter
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import javax.inject.Inject

class ShareActivity : DaggerAppCompatActivity(), View.OnClickListener {
    @Inject lateinit var resourceManager: ResourceManager
    @Inject lateinit var processingDialog: ProcessingDialog
    @Inject lateinit var utils: Utils

    private lateinit var recentRecyclerViewAdapter: SelectRoomItemAdapter
    private lateinit var groupRecyclerViewAdapter: SelectRoomItemAdapter
    private lateinit var peerRecyclerViewAdapter: SelectRoomItemAdapter

    @Inject lateinit var selectedRecyclerViewAdapter: ChooseRoomItemAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ShareViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        initView()

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

    private fun initView() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

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

        searchView.setOnClickListener(this)
        sendImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
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
            R.id.searchView -> {
                searchView.isIconified = false
            }
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