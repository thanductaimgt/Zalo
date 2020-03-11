package vng.zalo.tdtai.zalo.ui.home.contacts.contact_sub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sub_fragment_contact.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.managers.CallService
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.model.room.RoomItemPeer
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import vng.zalo.tdtai.zalo.ui.home.RoomItemsViewModel
import vng.zalo.tdtai.zalo.utils.Constants
import javax.inject.Inject


class ContactSubFragment : DaggerFragment(), View.OnClickListener {
    @Inject lateinit var callService: CallService
    @Inject lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: RoomItemsViewModel by viewModels { viewModelFactory }

    @Inject lateinit var adapter: ContactSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel.liveRoomItemMap.observe(viewLifecycleOwner, Observer { roomItemMap ->
            adapter.submitList(roomItemMap.values.filter { it.roomType == Room.TYPE_PEER }.sortedBy { it.getDisplayName(resourceManager) })
        })
    }

    private fun initView() {
        allContactRecyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@ContactSubFragment.adapter
            isNestedScrollingEnabled = false
            setItemViewCacheSize(50)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemContactSubFragmentConstraintLayout -> {
                val position = allContactRecyclerView.getChildAdapterPosition(v)
                val roomItem = adapter.currentList[position] as RoomItemPeer

                startActivity(
                        Intent(activity, ChatActivity::class.java).apply {
                            putExtra(Constants.ROOM_ID, roomItem.roomId)
                            putExtra(Constants.ROOM_NAME, roomItem.name)
                            putExtra(Constants.ROOM_AVATAR, roomItem.avatarUrl)
                            putExtra(Constants.ROOM_PHONE, roomItem.phone)
                        }
                )
            }
            R.id.voiceCallImgView -> {
                if (callService.isInit()) {
                    val position = allContactRecyclerView.getChildAdapterPosition(v.parent as View)
                    val roomItem = adapter.currentList[position] as RoomItemPeer

                    callService.startCallActivity(requireContext(), roomItem.roomId!!, roomItem.name!!, roomItem.phone!!, roomItem.avatarUrl!!)
                } else {
                    Toast.makeText(context, "Real device only", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
