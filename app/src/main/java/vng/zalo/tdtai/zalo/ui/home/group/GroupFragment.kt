package vng.zalo.tdtai.zalo.ui.home.group

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_group.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BoldSelectedSpinnerAdapter
import vng.zalo.tdtai.zalo.common.RoomItemAdapter
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.managers.SharedPrefsManager
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.model.room.RoomItemGroup
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import vng.zalo.tdtai.zalo.ui.create_group.CreateGroupActivity
import vng.zalo.tdtai.zalo.ui.home.RoomItemsViewModel
import vng.zalo.tdtai.zalo.utils.Constants
import java.util.*
import javax.inject.Inject

class GroupFragment: DaggerFragment(), View.OnClickListener {
    @Inject lateinit var sharedPrefsManager: SharedPrefsManager
    @Inject lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RoomItemsViewModel by viewModels { viewModelFactory }

    @Inject lateinit var adapter: RoomItemAdapter

    private var groupSortType = 0
    private val roomItemsObserver = RoomItemsObserver()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        viewModel.liveRoomItemMap.observe(viewLifecycleOwner, roomItemsObserver)
    }

    private fun initView() {
        recyclerView.apply {
            adapter = this@GroupFragment.adapter
            layoutManager = LinearLayoutManager(activity)
            isNestedScrollingEnabled = false
            setItemViewCacheSize(50)
        }

        createNewGroupLayout.setOnClickListener(this)

        initGroupSortTypeSpinner()
    }

    private fun initGroupSortTypeSpinner() {
        val spinnerAdapter = BoldSelectedSpinnerAdapter<String>(requireContext(), sortGroupSpinner)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val groupSortTypes = resources.getStringArray(R.array.sort_group_options)
        spinnerAdapter.addAll(groupSortTypes.toList())

        sortGroupSpinner.adapter = spinnerAdapter

        groupSortType = sharedPrefsManager.getGroupSortType()
        sortGroupSpinner.setSelection(groupSortType)

        sortGroupSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
            ) {
                groupSortType = position
                sharedPrefsManager.setGroupSortType(position)

                roomItemsObserver.onChanged(viewModel.liveRoomItemMap.value!!)
            }
        }
    }

    private fun sortByGroupSortType(roomItems: List<RoomItem>): List<RoomItem> {
        return when (groupSortType) {
            0 -> roomItems.sortedByDescending { it.lastMsgTime }
            else -> roomItems.sortedBy { it.getDisplayName(resourceManager).toLowerCase(Locale.getDefault()) }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val itemPosition = recyclerView.getChildAdapterPosition(v)
                val roomItem = adapter.currentList[itemPosition] as RoomItemGroup

                startActivity(
                        Intent(activity, ChatActivity::class.java).apply {
                            putExtra(Constants.ROOM_NAME, roomItem.name)
                            putExtra(Constants.ROOM_AVATAR, roomItem.avatarUrl)
                            putExtra(Constants.ROOM_ID, roomItem.roomId)
                        }
                )
            }
            R.id.createNewGroupLayout -> startActivity(Intent(activity, CreateGroupActivity::class.java))
        }
    }

    inner class RoomItemsObserver:Observer<HashMap<String, RoomItem>> {
        override fun onChanged(t: HashMap<String, RoomItem>) {
            adapter.submitList(
                    sortByGroupSortType(
                            t.values.filter { it.roomType == Room.TYPE_GROUP }
                    )
            ){
                if (recyclerView.isNotEmpty()) {
                    // save index and top position
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val index = layoutManager.findFirstVisibleItemPosition()
                    val firstView = recyclerView[0]
                    val top = firstView.top - recyclerView.paddingTop
                    layoutManager.scrollToPositionWithOffset(index, top)
                }
            }
        }
    }
}