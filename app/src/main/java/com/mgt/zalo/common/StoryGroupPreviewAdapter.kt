package com.mgt.zalo.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story_group_preview.view.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.story.StoryGroup
import com.mgt.zalo.manager.ResourceManager
import com.mgt.zalo.manager.SessionManager
import com.mgt.zalo.util.StoryGroupDiffCallback
import com.mgt.zalo.util.smartLoad
import javax.inject.Inject

class StoryGroupPreviewAdapter @Inject constructor(
        private val eventListener: BaseOnEventListener,
        private val resourceManager: ResourceManager,
        private val sessionManager: SessionManager,
        diffCallback: StoryGroupDiffCallback
) : BaseListAdapter<StoryGroup, StoryGroupPreviewAdapter.StoryViewHolder>(diffCallback) {
    var addedGroupsId: List<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val holder = StoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_story_group_preview, parent, false))
        return holder.apply { bindOnClick() }
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onBindViewHolder(
            holder: StoryViewHolder,
            position: Int,
            payloads: ArrayList<*>
    ) {
        val storyGroup = currentList[position]
        payloads.forEach {
            when (it) {
                StoryGroup.PAYLOAD_NEW_STORY -> holder.bindAddIcon(storyGroup)
                StoryGroup.PAYLOAD_ADDED_STATE -> holder.bindAddedState(storyGroup)
            }
        }
    }

    inner class StoryViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val storyGroup = currentList[position]

            bindOwner(storyGroup)
            bindAddIcon(storyGroup)
            bindAddedState(storyGroup)
        }

        private fun bindOwner(storyGroup: StoryGroup) {
            itemView.apply {
                val loadOwnerAvatar = { avatarUrl: String? ->
                    Picasso.get().smartLoad(avatarUrl, resourceManager, watchOwnerAvatarImgView) {
                        it.fit().centerCrop()
                    }
                }

                borderView.visibility = View.VISIBLE

                when (storyGroup.id) {
                    StoryGroup.ID_RECENT_STORY_GROUP -> {
                        nameTextView.text = context.getString(R.string.description_recent_story)

                        loadOwnerAvatar(storyGroup.ownerAvatarUrl)
                    }
                    StoryGroup.ID_CREATE_STORY -> {
                        nameTextView.text = context.getString(R.string.description_create_story)

                        loadOwnerAvatar(sessionManager.curUser!!.avatarUrl)
                    }
                    StoryGroup.ID_CREATE_STORY_GROUP -> {
                        nameTextView.text = context.getString(R.string.label_create_new)

                        watchOwnerAvatarImgView.setImageResource(R.drawable.add2)

                        borderView.visibility = View.INVISIBLE
                    }
                    StoryGroup.ID_RECENT_STORIES -> {
                        nameTextView.text = if (storyGroup.ownerId == sessionManager.curUser!!.id) {
                            context.getString(R.string.description_your_story)
                        } else {
                            storyGroup.ownerName
                        }

                        loadOwnerAvatar(storyGroup.ownerAvatarUrl)
                    }
                    else -> {
                        nameTextView.text = storyGroup.name

                        loadOwnerAvatar(storyGroup.avatarUrl)
                    }
                }
            }
        }

        fun bindAddIcon(storyGroup: StoryGroup) {
            itemView.addIcon.visibility = if (storyGroup.id == StoryGroup.ID_CREATE_STORY) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        fun bindAddedState(storyGroup: StoryGroup) {
            itemView.addedForegroundView.visibility = if (addedGroupsId.contains(storyGroup.id)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        fun bindOnClick() {
            itemView.setOnClickListener(eventListener)
        }
    }
}