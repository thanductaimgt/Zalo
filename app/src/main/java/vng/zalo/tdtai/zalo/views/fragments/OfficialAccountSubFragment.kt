package vng.zalo.tdtai.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.sub_fragment_official_account.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.OfficialAccountSubFragmentAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.viewmodels.OfficialAccountViewModel
import vng.zalo.tdtai.zalo.views.activities.RoomActivity

class OfficialAccountSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: OfficialAccountViewModel
    private lateinit var subFragmentAdapter: OfficialAccountSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_official_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(OfficialAccountViewModel::class.java)
        viewModel.liveOfficialAccounts.observe(viewLifecycleOwner, Observer { rooms ->
            subFragmentAdapter.submitList(rooms)
            Log.d(TAG, "onViewCreated.$rooms")
        })
    }

    private fun initView() {
        subFragmentAdapter = OfficialAccountSubFragmentAdapter(this, RoomItemDiffCallback())
        with(recyclerView) {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@OfficialAccountSubFragment.subFragmentAdapter
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
                val position = recyclerView.getChildLayoutPosition(v)

                startActivity(
                        Intent(activity, RoomActivity::class.java).apply {
                            putExtra(Constants.ROOM_ID, subFragmentAdapter.currentList[position].roomId)
                            putExtra(Constants.ROOM_NAME, subFragmentAdapter.currentList[position].name)
                            putExtra(Constants.ROOM_AVATAR, subFragmentAdapter.currentList[position].avatarUrl)
                        }
                )
            }
        }
    }
}