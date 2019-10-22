package vng.zalo.tdtai.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.sub_fragment_contact.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.ContactSubFragmentAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.viewmodels.UserRoomItemsViewModel
import vng.zalo.tdtai.zalo.views.activities.RoomActivity


class ContactSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: UserRoomItemsViewModel
    private lateinit var adapter: ContactSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(activity!!, ViewModelFactory.getInstance()).get(UserRoomItemsViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer { roomItems ->
            adapter.submitList(roomItems.filter { it.roomType == RoomItem.TYPE_PEER }.sortedBy { it.name })
        })
    }

    private fun initView() {
        adapter = ContactSubFragmentAdapter(this, RoomItemDiffCallback())
        with(allContactRecyclerView) {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@ContactSubFragment.adapter
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemContactSubFragmentConstraintLayout -> {
                val position = allContactRecyclerView.getChildLayoutPosition(v)

                startActivity(
                        Intent(activity, RoomActivity::class.java).apply {
                            putExtra(Constants.ROOM_ID, adapter.currentList[position].roomId)
                            putExtra(Constants.ROOM_NAME, adapter.currentList[position].name)
                            putExtra(Constants.ROOM_AVATAR, adapter.currentList[position].avatarUrl)
                        }
                )
            }
        }
    }
}
