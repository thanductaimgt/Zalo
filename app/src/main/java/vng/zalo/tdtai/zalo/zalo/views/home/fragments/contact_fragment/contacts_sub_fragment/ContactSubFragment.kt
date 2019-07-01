package vng.zalo.tdtai.zalo.zalo.views.home.fragments.contact_fragment.contacts_sub_fragment

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
import vng.zalo.tdtai.zalo.zalo.dependency_factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.utils.Constants.*
import vng.zalo.tdtai.zalo.zalo.utils.ContactModelDiffCallback
import vng.zalo.tdtai.zalo.zalo.viewmodels.ContactSubFragmentViewModel
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity

class ContactSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: ContactSubFragmentViewModel
    private lateinit var adapter: AllContactAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(ContactSubFragmentViewModel::class.java)

        adapter = AllContactAdapter(this, ContactModelDiffCallback())
        with(allContactRecyclerView) {
            layoutManager = LinearLayoutManager(activity)
            adapter = this@ContactSubFragment.adapter
        }

        viewModel.liveContacts.observe(viewLifecycleOwner, Observer{ contacts ->
            adapter.submitList(contacts)
            Log.d(TAG, "onChanged livedata")
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemContactSubFragmentConstraintLayout -> {
                val position = allContactRecyclerView.getChildLayoutPosition(v)
                val intent = Intent(activity, RoomActivity::class.java)

                intent.putExtra(ROOM_ID, adapter.currentList[position].roomId)
                intent.putExtra(ROOM_NAME, adapter.currentList[position].name)
                intent.putExtra(ROOM_AVATAR, adapter.currentList[position].avatar)

                startActivity(intent)
            }
        }
    }

    companion object {
        private val TAG = ContactSubFragment::class.java.simpleName
    }
}
