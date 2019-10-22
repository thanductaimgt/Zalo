package vng.zalo.tdtai.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_group.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.RoomItemAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Constants.ROOM_AVATAR
import vng.zalo.tdtai.zalo.utils.Constants.ROOM_ID
import vng.zalo.tdtai.zalo.utils.Constants.ROOM_NAME
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.viewmodels.UserRoomItemsViewModel
import vng.zalo.tdtai.zalo.views.activities.CreateGroupActivity
import vng.zalo.tdtai.zalo.views.activities.RoomActivity

class GroupFragment : Fragment(), AdapterView.OnItemSelectedListener, View.OnClickListener {

    private lateinit var viewModel: UserRoomItemsViewModel
    private lateinit var adapter: RoomItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        viewModel = ViewModelProvider(activity!!, ViewModelFactory.getInstance()).get(UserRoomItemsViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer { roomItems ->
            adapter.submitList(roomItems.filter { it.roomType == RoomItem.TYPE_GROUP })
        })
    }

    private fun initView() {
        adapter = RoomItemAdapter(this, RoomItemDiffCallback())

        with(recyclerView) {
            adapter = this@GroupFragment.adapter
            layoutManager = LinearLayoutManager(activity)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
                isNestedScrollingEnabled = false
            }
        }

        createNewGroupLayout.setOnClickListener(this)

        // Create an ArrayAdapter using the string array and a default spinner layout
        val spinnerAdapter = ArrayAdapter.createFromResource(context!!,
                R.array.sort_group_options, android.R.layout.simple_spinner_item)
        // Specify the layout to use when the list of choices appears
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the viewPagerAdapter to the spinner
        sortGroupSpinner.adapter = spinnerAdapter
    }

    override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {

    }

    override fun onNothingSelected(adapterView: AdapterView<*>) {

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val itemPosition = recyclerView.getChildLayoutPosition(v)
                startActivity(
                        Intent(activity, RoomActivity::class.java).apply {
                            putExtra(ROOM_NAME, adapter.currentList[itemPosition].name)
                            putExtra(ROOM_AVATAR, adapter.currentList[itemPosition].avatarUrl)
                            //room not created in database
                            if (adapter.currentList[itemPosition].roomId != null)
                                putExtra(ROOM_ID, adapter.currentList[itemPosition].roomId)
                        }
                )
            }
            R.id.createNewGroupLayout -> startActivity(Intent(activity, CreateGroupActivity::class.java))
        }
    }
}