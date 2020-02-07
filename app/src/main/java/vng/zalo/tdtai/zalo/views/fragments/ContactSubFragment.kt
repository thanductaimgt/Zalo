package vng.zalo.tdtai.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.sub_fragment_contact.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.CallStarter
import vng.zalo.tdtai.zalo.adapters.ContactSubFragmentAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.models.room.RoomItemPeer
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.viewmodels.RoomItemsViewModel
import vng.zalo.tdtai.zalo.views.activities.RoomActivity


class ContactSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: RoomItemsViewModel
    private lateinit var adapter: ContactSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(activity!!, ViewModelFactory.getInstance()).get(RoomItemsViewModel::class.java)

        initView()

        viewModel.liveRoomItemMap.observe(viewLifecycleOwner, Observer { roomItemMap ->
            adapter.submitList(roomItemMap.values.filter { it.roomType == Room.TYPE_PEER }.sortedBy { it.getDisplayName() })
        })
    }

    private fun initView() {
        adapter = ContactSubFragmentAdapter(this, RoomItemDiffCallback())
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
                        Intent(activity, RoomActivity::class.java).apply {
                            putExtra(Constants.ROOM_ID, roomItem.roomId)
                            putExtra(Constants.ROOM_NAME, roomItem.name)
                            putExtra(Constants.ROOM_AVATAR, roomItem.avatarUrl)
                            putExtra(Constants.ROOM_PHONE, roomItem.phone)
                        }
                )
            }
            R.id.voiceCallImgView -> {
                if(ZaloApplication.sipManager != null){
                    val position = allContactRecyclerView.getChildAdapterPosition(v.parent as View)
                    val roomItem = adapter.currentList[position] as RoomItemPeer

                    CallStarter.startAudioCall(context!!, roomItem.roomId!!, roomItem.name!!, roomItem.phone!!, roomItem.avatarUrl!!)
                }else{
                    Toast.makeText(context, "Only available for real device", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
