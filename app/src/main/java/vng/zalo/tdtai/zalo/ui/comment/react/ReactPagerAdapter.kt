package vng.zalo.tdtai.zalo.ui.comment.react

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_react_page.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.*
import vng.zalo.tdtai.zalo.data_model.react.ReactPage
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.ui.story.story_detail.ReactAdapter
import vng.zalo.tdtai.zalo.util.ReactDiffCallback
import vng.zalo.tdtai.zalo.util.ReactPageDiffCallback
import javax.inject.Inject

class ReactPagerAdapter @Inject constructor(
        private val resourceManager: ResourceManager,
        diffCallback: ReactPageDiffCallback
) : BaseListAdapter<ReactPage, BaseViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return ReactPageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_react_page, parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class ReactPageViewHolder(itemView: View) : BaseViewHolder(itemView), BaseOnEventListener {
        private val reactAdapter:ReactAdapter = ReactAdapter(this, resourceManager, ReactDiffCallback())

        override fun bind(position: Int) {
            itemView.recyclerView.adapter = reactAdapter
            reactAdapter.submitList(currentList[position].reacts)
        }

        override fun onClick(view: View) {
            when(view.id){
                R.id.rootItemView->{
                    itemView.apply {
                        val position = recyclerView.getChildAdapterPosition(view)
                        val react = reactAdapter.currentList[position]
                        context.startActivity(Intent(context, EmptyActivity::class.java).apply {
                            putExtra(EmptyActivity.KEY_FRAGMENT, BaseView.FRAGMENT_PROFILE)
                            putExtra(EmptyActivity.KEY_USER_ID, react.ownerId)
                        })
                    }
                }
            }
        }
    }
}