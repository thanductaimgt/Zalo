package vng.zalo.tdtai.zalo.base

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class NotScrollableLinearLayoutManager(context: Context) : LinearLayoutManager(context){
    override fun canScrollVertically(): Boolean {
        return false
    }

    override fun canScrollHorizontally(): Boolean {
        return false
    }
}