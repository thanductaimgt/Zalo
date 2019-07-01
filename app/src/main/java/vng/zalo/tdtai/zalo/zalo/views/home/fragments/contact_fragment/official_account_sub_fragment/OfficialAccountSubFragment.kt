package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.official_account_sub_fragment

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
import kotlinx.android.synthetic.main.sub_fragment_official_account.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.dependency_factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.utils.Constants.*
import vng.zalo.tdtai.zalo.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.zalo.viewmodels.OfficialAccountViewModel
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity

class OfficialAccountSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: OfficialAccountViewModel
    private lateinit var adapter: OfficialAccountAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_official_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(OfficialAccountViewModel::class.java)

        adapter = OfficialAccountAdapter(this, RoomItemDiffCallback())
        with(recyclerView) {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@OfficialAccountSubFragment.adapter
        }

        viewModel.liveOfficialAccounts.observe(viewLifecycleOwner, Observer{ rooms ->
            adapter.submitList(rooms)
            Log.d(TAG, "onChanged livedata")
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val position = recyclerView.getChildLayoutPosition(v)

                val intent = Intent(activity, RoomActivity::class.java)
                intent.putExtra(ROOM_ID, adapter.currentList[position].roomId)
                intent.putExtra(ROOM_NAME, adapter.currentList[position].name)
                intent.putExtra(ROOM_AVATAR, adapter.currentList[position].avatar)

                startActivity(intent)
            }
        }
    }

    companion object {
        private val TAG = OfficialAccountSubFragment::class.java.simpleName
    }
}