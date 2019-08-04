package vng.zalo.tdtai.zalo.utils

import android.view.View
import androidx.recyclerview.widget.RecyclerView

abstract class BindableViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
    abstract fun bind(position:Int)
}