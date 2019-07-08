package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_chat.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.dependency_factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.shared_adapters.RoomItemAdapter
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import vng.zalo.tdtai.zalo.zalo.viewmodels.ChatFragmentViewModel
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity

class ChatFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: ChatFragmentViewModel
    private lateinit var adapter: RoomItemAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance()).get(ChatFragmentViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer { rooms ->
            adapter.submitList(rooms) { Log.d(Utils.getTag(object {}), viewModel.liveRoomItems.value.toString()) }
            Log.d(Utils.getTag(object {}), "onChanged livedata")
        })
    }

    private fun initView(){
        adapter = RoomItemAdapter(this, RoomItemDiffCallback())

        with(recyclerView) {
            adapter = this@ChatFragment.adapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val position = recyclerView.getChildLayoutPosition(v)

                startActivity(
                        Intent(activity, RoomActivity::class.java).apply {
                            putExtra(Constants.ROOM_NAME, adapter.currentList[position].name)
                            putExtra(Constants.ROOM_AVATAR, adapter.currentList[position].avatar)
                            putExtra(Constants.ROOM_ID, adapter.currentList[position].roomId)
                        }
                )
            }
        }
    }
}