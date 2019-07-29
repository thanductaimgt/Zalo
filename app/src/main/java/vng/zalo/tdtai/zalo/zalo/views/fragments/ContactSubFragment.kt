package vng.zalo.tdtai.zalo.zalo.views.fragments

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
import kotlinx.android.synthetic.main.sub_fragment_contact.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.ContactModelDiffCallback
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import vng.zalo.tdtai.zalo.zalo.viewmodels.ContactSubFragmentViewModel
import vng.zalo.tdtai.zalo.zalo.adapters.ContactSubFragmentAdapter
import vng.zalo.tdtai.zalo.zalo.views.activities.RoomActivity

class ContactSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: ContactSubFragmentViewModel
    private lateinit var adapter: ContactSubFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance()).get(ContactSubFragmentViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer { contacts ->
            adapter.submitList(contacts)
            Log.d(Utils.getTag(object {}), "onChanged livedata")
        })
    }

    private fun initView(){
        adapter = ContactSubFragmentAdapter(this, ContactModelDiffCallback())
        with(allContactRecyclerView) {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@ContactSubFragment.adapter
        }

        Utils.formatTextOnNumberOfLines(statusTextView, 2)
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
