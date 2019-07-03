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
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.zalo.viewmodels.OfficialAccountViewModel
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity

class OfficialAccountSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: OfficialAccountViewModel
    private lateinit var subFragmentAdapter: OfficialAccountSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_official_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(OfficialAccountViewModel::class.java)
        viewModel.liveOfficialAccounts.observe(viewLifecycleOwner, Observer { rooms ->
            subFragmentAdapter.submitList(rooms)
            Log.d(TAG, "onChanged livedata")
        })
    }

    private fun initView(){
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
                            putExtra(Constants.ROOM_AVATAR, subFragmentAdapter.currentList[position].avatar)
                        }
                )
            }
        }
    }

    companion object {
        private val TAG = OfficialAccountSubFragment::class.java.simpleName
    }
}