package vng.zalo.tdtai.zalo.common

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_story_preview.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseListAdapter
import vng.zalo.tdtai.zalo.base.BindableViewHolder
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.manager.ResourceManager
import vng.zalo.tdtai.zalo.manager.SessionManager
import vng.zalo.tdtai.zalo.util.StoryGroupDiffCallback
import vng.zalo.tdtai.zalo.util.smartLoad
import javax.inject.Inject

class StoryPreviewAdapter @Inject constructor(
        private val clickListener: View.OnClickListener,
        private val resourceManager: ResourceManager,
        private val sessionManager: SessionManager,
        diffCallback: StoryGroupDiffCallback
) : BaseListAdapter<StoryGroup, StoryPreviewAdapter.StoryViewHolder>(diffCallback) {
    var addedGroupsId: List<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val holder = StoryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_story_preview, parent, false))
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

    inner class StoryViewHolder(itemView: View) : BindableViewHolder(itemView) {
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

                        circleLayout.visibility = View.GONE
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
            itemView.setOnClickListener(clickListener)
        }
    }
}