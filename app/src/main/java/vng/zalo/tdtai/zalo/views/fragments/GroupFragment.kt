package vng.zalo.tdtai.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.viewmodels.UserRoomItemsViewModel
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity
import vng.zalo.tdtai.zalo.views.activities.RoomActivity

class GroupFragment : Fragment(), View.OnClickListener {

    private lateinit var viewModel: UserRoomItemsViewModel
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

        initView()

        viewModel = ViewModelProvider(activity!!, ViewModelFactory.getInstance()).get(UserRoomItemsViewModel::class.java)
        viewModel.liveRoomIdRoomItemMap.observe(viewLifecycleOwner, roomItemsObserver)
    }

    private fun initView() {
        adapter = RoomItemAdapter(this, RoomItemDiffCallback())

        with(recyclerView) {
            adapter = this@GroupFragment.adapter
            layoutManager = LinearLayoutManager(activity)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                isNestedScrollingEnabled = false
            }
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

                roomItemsObserver.onChanged(viewModel.liveRoomIdRoomItemMap.value!!)
            }
        }
    }

    private fun sortByGroupSortType(roomItems: List<RoomItem>): List<RoomItem> {
        return when (groupSortType) {
            0 -> roomItems.sortedByDescending { it.lastMsgTime }
            1 -> roomItems.sortedBy { it.getDisplayName() }
            else -> roomItems
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val itemPosition = recyclerView.getChildAdapterPosition(v)
                startActivity(
                        Intent(activity, RoomActivity::class.java).apply {
                            putExtra(Constants.ROOM_NAME, adapter.currentList[itemPosition].getDisplayName())
                            putExtra(Constants.ROOM_AVATAR, adapter.currentList[itemPosition].avatarUrl)
                            putExtra(Constants.ROOM_TYPE, adapter.currentList[itemPosition].roomType)
                            putExtra(Constants.ROOM_ID, adapter.currentList[itemPosition].roomId)
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
            )
        }
    }
}