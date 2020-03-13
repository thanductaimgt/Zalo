package vng.zalo.tdtai.zalo.ui.home.diary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.ZaloListAdapter
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.model.story.Story
import vng.zalo.tdtai.zalo.utils.StoryDiffCallback
import vng.zalo.tdtai.zalo.utils.smartLoad
import javax.inject.Inject

class StoryAdapter @Inject constructor(
        private val resourceManager: ResourceManager,
        diffCallback: StoryDiffCallback
):ZaloListAdapter<Story, StoryAdapter.StoryViewHolder>(diffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val holder = StoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_story, parent, false))
        return holder.apply { bindOnClick() }
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class StoryViewHolder(itemView:View):BindableViewHolder(itemView){
        override fun bind(position: Int) {
            val story = currentList[position]

            bindOwner(story)
        }

        private fun bindOwner(story:Story){
            itemView.apply {
                Picasso.get().smartLoad(story.ownerAvatarUrl, resourceManager, avatarImgView){
                    it.fit().centerCrop()
                }

                nameTextView.text = story.ownerName
            }
        }

        fun bindOnClick(){

        }
    }
}