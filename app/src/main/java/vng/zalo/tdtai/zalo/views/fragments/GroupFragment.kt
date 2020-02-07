package vng.zalo.tdtai.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.get
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_group.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.SharedPrefsManager
import vng.zalo.tdtai.zalo.adapters.BoldSelectedSpinnerAdapter
import vng.zalo.tdtai.zalo.adapters.RoomItemAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.room.RoomItemGroup
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.viewmodels.RoomItemsViewModel
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity
import vng.zalo.tdtai.zalo.views.activities.RoomActivity
import java.util.*
import kotlin.collections.HashMap

class GroupFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: RoomItemsViewModel
    private lateinit var adapter: RoomItemAdapter
    private var groupSortType = 0
    private val roomItemsObserver = RoomItemsObserver()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(activity!!, ViewModelFactory.getInstance()).get(RoomItemsViewModel::class.java)

        initView()

        viewModel.liveRoomItemMap.observe(viewLifecycleOwner, roomItemsObserver)
    }

    private fun initView() {
        adapter = RoomItemAdapter(this, RoomItemDiffCallback())

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
        val spinnerAdapter = BoldSelectedSpinnerAdapter<String>(context!!, sortGroupSpinner)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val groupSortTypes = resources.getStringArray(R.array.sort_group_options)
        spinnerAdapter.addAll(groupSortTypes.toList())

        sortGroupSpinner.adapter = spinnerAdapter

        groupSortType = SharedPrefsManager.getGroupSortType(context!!)
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
                SharedPrefsManager.setGroupSortType(context!!, position)

                roomItemsObserver.onChanged(viewModel.liveRoomItemMap.value!!)
            }
        }
    }

    private fun sortByGroupSortType(roomItems: List<RoomItem>): List<RoomItem> {
        return when (groupSortType) {
            0 -> roomItems.sortedByDescending { it.lastMsgTime }
            else -> roomItems.sortedBy { it.getDisplayName().toLowerCase(Locale.getDefault()) }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val itemPosition = recyclerView.getChildAdapterPosition(v)
                val roomItem = adapter.currentList[itemPosition] as RoomItemGroup

                startActivity(
                        Intent(activity, RoomActivity::class.java).apply {
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