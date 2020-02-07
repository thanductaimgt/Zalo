package vng.zalo.tdtai.zalo.views.activities

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_share.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.NotScrollableLinearLayoutManager
import vng.zalo.tdtai.zalo.adapters.ChoosenListRecyclerViewAdapter
import vng.zalo.tdtai.zalo.adapters.SelectRoomItemAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.viewmodels.ShareActivityViewModel

class ShareActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var recentRecyclerViewAdapter: SelectRoomItemAdapter
    private lateinit var groupRecyclerViewAdapter: SelectRoomItemAdapter
    private lateinit var peerRecyclerViewAdapter: SelectRoomItemAdapter

    private lateinit var selectedRecyclerViewAdapter: ChoosenListRecyclerViewAdapter

    lateinit var viewModel: ShareActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(intent = intent)).get(ShareActivityViewModel::class.java)

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
            groupRecyclerViewAdapter.submitList(roomItems.filter { it.roomType == Room.TYPE_GROUP }.sortedBy { it.getDisplayName() }.take(6))
            peerRecyclerViewAdapter.submitList(roomItems.filter { it.roomType == Room.TYPE_PEER }.sortedBy { it.getDisplayName() }.take(5))
        })
    }

    private fun initView() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        selectedRecyclerViewAdapter = ChoosenListRecyclerViewAdapter(RoomItemDiffCallback())
        recyclerView.apply {
            adapter = selectedRecyclerViewAdapter
            layoutManager = LinearLayoutManager(this@ShareActivity, RecyclerView.HORIZONTAL, false)
        }

        recentRecyclerViewAdapter = SelectRoomItemAdapter(RoomItemDiffCallback(), false, viewModel.liveSelectedRoomItems)
        recentRecyclerView.apply {
            adapter = recentRecyclerViewAdapter
            layoutManager = NotScrollableLinearLayoutManager(this@ShareActivity)
            isNestedScrollingEnabled = false
        }

        groupRecyclerViewAdapter = SelectRoomItemAdapter(RoomItemDiffCallback(), false, viewModel.liveSelectedRoomItems)
        groupRecyclerView.apply {
            adapter = groupRecyclerViewAdapter
            layoutManager = NotScrollableLinearLayoutManager(this@ShareActivity)
            isNestedScrollingEnabled = false
        }

        peerRecyclerViewAdapter = SelectRoomItemAdapter(RoomItemDiffCallback(), false, viewModel.liveSelectedRoomItems)
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
                viewModel.addNewMessagesToFirestore(this) { isSuccess ->
                    ZaloApplication.processingDialog.dismiss()
                    Toast.makeText(this,
                            if (isSuccess) {
                                "Đã gửi"
                            } else {
                                "Có lỗi xảy ra"
                            }, Toast.LENGTH_SHORT).show()
                    finish()
                }
                ZaloApplication.processingDialog.show(supportFragmentManager)
            }
            R.id.backImgView -> {
                Utils.hideKeyboard(this, rootView)
                finish()
            }
        }
    }
}