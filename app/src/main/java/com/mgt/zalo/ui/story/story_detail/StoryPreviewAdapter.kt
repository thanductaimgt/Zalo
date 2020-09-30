package com.mgt.zalo.ui.story.story_detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story_preview.view.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.story.ImageStory
import com.mgt.zalo.data_model.story.Story
import com.mgt.zalo.data_model.story.VideoStory
import com.mgt.zalo.manager.ResourceManager
import com.mgt.zalo.util.StoryDiffCallback
import com.mgt.zalo.util.smartLoad
import javax.inject.Inject

class StoryPreviewAdapter @Inject constructor(
        private val storyDetailFragment: StoryDetailFragment,
        private val resourceManager: ResourceManager,
        diffCallback: StoryDiffCallback
) : BaseListAdapter<Story, StoryPreviewAdapter.StoryPreviewHolder>(diffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryPreviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_story_preview, parent, false)
        view.setOnClickListener(storyDetailFragment)
        return StoryPreviewHolder(view)
    }

    override fun onBindViewHolder(holder: StoryPreviewHolder, position: Int) {
        holder.bind(position)
    }

//    override fun onBindViewHolder(
//            holder: StoryPreviewHolder,
//            position: Int,
//            payloads: ArrayList<*>
//    ) {
//        val roomItem = currentList[position]
//        payloads.forEach {
//            when (it) {
//                RoomItem.PAYLOAD_NEW_MESSAGE -> holder.bindLastMessage(roomItem)
//                RoomItem.PAYLOAD_SEEN_MESSAGE -> holder.bindSeenStatus(roomItem)
//                RoomItem.PAYLOAD_ONLINE_STATUS -> holder.bindOnlineStatus(roomItem)
//                RoomItem.PAYLOAD_TYPING -> holder.bindLastMessage(roomItem)
//            }
//        }
//    }

    inner class StoryPreviewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            itemView.apply {
                val story = currentList[position]
                when(story){
                    is ImageStory->{
                        Picasso.get().smartLoad(story.imageUrl!!, resourceManager, imageView){
                            it.fit().centerCrop()
                        }
                    }
                    is VideoStory->{
                        resourceManager.getVideoThumbUri(story.videoUrl!!){uri->
                            Picasso.get().smartLoad(uri, resourceManager, imageView){
                                it.fit().centerCrop()
                            }
                        }
                    }
                }
            }
        }
    }
}