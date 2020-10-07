package com.mgt.zalo.ui.profile

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseListAdapter
import com.mgt.zalo.base.BaseOnEventListener
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.data_model.User
import com.mgt.zalo.util.diff_callback.UserDiffCallback
import com.mgt.zalo.util.smartLoad
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.item_suggest_user.view.*
import javax.inject.Inject

class SuggestUserAdapter @Inject constructor(
        private val eventListener: BaseOnEventListener,
        diffCallback: UserDiffCallback
) : BaseListAdapter<User, SuggestUserAdapter.UserViewHolder>(diffCallback) {
    val followingUserIds = hashSetOf("0123456789")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val holder = UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_suggest_user, parent, false))
        return holder.apply { bindOnClick() }
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun onBindViewHolder(
            holder: UserViewHolder,
            position: Int,
            payloads: ArrayList<*>
    ) {
        val user = currentList[position]
        payloads.forEach {
            when (it) {
                User.PAYLOAD_FOLLOW -> holder.bindFollow(user)
            }
        }
    }

    inner class UserViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bind(position: Int) {
            val user = currentList[position]

            itemView.apply {
                idTextView.text = user.id
                nameTextView.text = user.name
                Picasso.get().smartLoad(user.avatarUrl, resourceManager, avatarImgView) {
                    it.fit().centerCrop()
                }
            }

            bindFollow(user)
        }

        fun bindFollow(user: User) {
            itemView.apply {
                if (followingUserIds.contains(user.id)) {
                    followTextView.apply {
                        background = context.getDrawable(R.drawable.transparent_black_border_bg)
                        setTextColor(ContextCompat.getColor(context, android.R.color.black))
                        text = context.getString(R.string.description_following)
                    }
                } else {
                    followTextView.apply {
                        text = context.getString(R.string.label_follow)
                        setTextColor(ContextCompat.getColor(context, android.R.color.white))
                        background = context.getDrawable(R.drawable.follow_button_bg)
                    }
                }
            }
        }

        fun bindOnClick() {
            itemView.apply {
                setOnClickListener(eventListener)
                followTextView.setOnClickListener(eventListener)
                closeImgView.setOnClickListener(eventListener)
            }
        }
    }
}