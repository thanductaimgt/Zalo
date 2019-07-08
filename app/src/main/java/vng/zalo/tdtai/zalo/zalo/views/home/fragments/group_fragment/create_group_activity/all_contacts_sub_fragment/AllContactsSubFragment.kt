package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity.all_contacts_sub_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import kotlinx.android.synthetic.main.item_select_contact.view.*
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
        initView()

        viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(RecentContactsSubFragmentViewModel::class.java)
        viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer{ contacts ->
            adapter.submitList(contacts)
            Log.d(TAG, "onChanged livedata")
        })

        (activity as CreateGroupActivity).viewModel.liveRoomItems.observe(viewLifecycleOwner, Observer {
            updateSelectedListOnScreen()
        })
    }

    private fun initView(){
        adapter = AllContactsSubFragmentAdapter(this, RoomItemDiffCallback())

        with(recyclerView){
            layoutManager = LinearLayoutManager(activity)
            adapter = this@AllContactsSubFragment.adapter
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.itemRecentContactsLayout -> {
                v.radioButton.isChecked = !v.radioButton.isChecked

                val itemPosition = recyclerView.getChildLayoutPosition(v)
                val item = adapter.currentList[itemPosition]

                (activity as CreateGroupActivity).proceedNewClickOnItem(item)
            }
        }
    }

    private fun updateSelectedListOnScreen() {
        recyclerView.apply {
            forEach {
                val itemPosition = getChildLayoutPosition(it)
                val item = this@AllContactsSubFragment.adapter.currentList[itemPosition]
                it.radioButton.isChecked = (activity as CreateGroupActivity).viewModel.liveRoomItems.value!!.contains(item)
            }
        }
    }

    companion object {
        private val TAG = AllContactsSubFragment::class.java.simpleName
    }
}