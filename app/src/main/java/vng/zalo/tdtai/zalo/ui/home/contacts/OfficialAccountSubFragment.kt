package vng.zalo.tdtai.zalo.ui.home.contacts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.sub_fragment_official_account.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.di.ViewModelFactory
import vng.zalo.tdtai.zalo.ui.home.RoomItemsViewModel
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.TAG
import javax.inject.Inject

class OfficialAccountSubFragment : DaggerFragment(), View.OnClickListener {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: OfficialAccountViewModel by viewModels { viewModelFactory }

    @Inject lateinit var subFragmentAdapter: OfficialAccountSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_official_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel.liveOfficialAccounts.observe(viewLifecycleOwner, Observer { rooms ->
            subFragmentAdapter.submitList(rooms)
            Log.d(TAG, "onViewCreated.$rooms")
        })
    }

    private fun initView() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@OfficialAccountSubFragment.subFragmentAdapter
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRoomRootLayout -> {
//                val position = recyclerView.getChildLayoutPosition(v)
//
//                startActivity(
//                        Intent(activity, RoomActivity::class.java).apply {
//                            putExtra(Constants.ROOM_ID, subFragmentAdapter.currentList[position].roomId)
//                            putExtra(Constants.ROOM_NAME, subFragmentAdapter.currentList[position].phone)
//                            putExtra(Constants.ROOM_AVATAR, subFragmentAdapter.currentList[position].avatarUrl)
//                            putExtra(Constants.ROOM_TYPE, subFragmentAdapter.currentList[position].roomType)
//                        }
//                )
            }
        }
    }
}