package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.all_contacts_sub_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import kotlinx.android.synthetic.main.item_recent_contacts.view.*
import kotlinx.android.synthetic.main.sub_fragment_recent_contacts.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.dependency_factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.zalo.viewmodels.RecentContactsSubFragmentViewModel
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.CreateGroupActivity

class AllContactsSubFragment : Fragment(), View.OnClickListener {
    private lateinit var viewModel: RecentContactsSubFragmentViewModel
    private lateinit var adapter: ListAdapter<RoomItem, AllContactsSubFragmentAdapter.AllContactsViewHolder>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.sub_fragment_recent_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(RecentContactsSubFragmentViewModel::class.java)

        adapter = AllContactsSubFragmentAdapter(this, RoomItemDiffCallback())

        with(recyclerView){
            layoutManager = LinearLayoutManager(activity)
            adapter = this@AllContactsSubFragment.adapter
        }

        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer{ contacts ->
            adapter.submitList(contacts)
            Log.d(TAG, "onChanged livedata")
        })
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRecentContactsLayout -> {
                v.radioButton.isChecked = !v.radioButton.isChecked

                val curItemPosition = recyclerView.getChildLayoutPosition(v)
                val curItem = adapter.currentList[curItemPosition]

                (activity as CreateGroupActivity).proceedNewClickOnItem(curItem)
            }
        }
    }

    companion object {
        private val TAG = AllContactsSubFragment::class.java.simpleName
    }
}