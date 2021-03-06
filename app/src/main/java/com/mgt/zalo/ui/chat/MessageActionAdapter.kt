package com.mgt.zalo.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mgt.zalo.R
import com.mgt.zalo.util.ImageLoader
import kotlinx.android.synthetic.main.item_message_action.view.*
import javax.inject.Inject

class MessageActionAdapter @Inject constructor(
        private val chatActivity: ChatActivity,
        private val imageLoader: ImageLoader
) :RecyclerView.Adapter<MessageActionAdapter.MessageActionViewHolder>(){
    var actions = ArrayList<Pair<Int, Int>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageActionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message_action, parent, false)
        view.setOnClickListener(chatActivity)
        return MessageActionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return actions.size
    }

    override fun onBindViewHolder(holder: MessageActionViewHolder, position: Int) {
        holder.bind(position)
    }

    inner class MessageActionViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        fun bind(position: Int){
            val action = actions[position]
            val actionIconResId = action.first
            val actionTextResId = action.second

            itemView.apply {
                imageLoader.load(actionIconResId, iconImgView)
                labelTextView.text = context.getString(actionTextResId)
            }
        }
    }
}