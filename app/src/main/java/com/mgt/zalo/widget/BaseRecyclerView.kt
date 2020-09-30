package com.mgt.zalo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.forEach
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class BaseRecyclerView : RecyclerView {
    private val recyclerViewPos = IntArray(2)
    private val childViewPos = IntArray(2)
    private var lastFocusPos = -1
    private var focusChangeListeners = arrayListOf<(lastFocusPos: Int, curFocusPos: Int) -> Any?>()

    constructor(context: Context) : this(context, null, 0)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        post {
            addOnScrollListener(object : OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val curFocusPos = getFocusPosition()
                    if (curFocusPos != -1 && curFocusPos != lastFocusPos) {
                        focusChangeListeners.forEach { it(lastFocusPos, curFocusPos) }
                        lastFocusPos = curFocusPos
                    }
                }
            })
        }
    }

    fun addChildFocusChangeListener(onChildFocusChange: (lastFocusPos: Int, curFocusPos: Int) -> Any?) {
        focusChangeListeners.add(onChildFocusChange)
    }

    fun removeChildFocusChangeListener(onChildFocusChange: (lastFocusPos: Int, curFocusPos: Int) -> Any?) {
        focusChangeListeners.remove(onChildFocusChange)
    }

    private fun getFocusPosition(): Int {
        getLocationInWindow(recyclerViewPos)
        val recyclerViewY = recyclerViewPos[1]
        val recyclerViewCenterY = recyclerViewY + height / 2

        var curFocusView: View? = null
        var minGap = Int.MAX_VALUE
        forEach {
            if (it.width != 0 && it.height != 0) {
                it.getLocationInWindow(childViewPos)
                val childY = childViewPos[1]
                val childCenterY = childY + it.height / 2

                if (childCenterY < recyclerViewY + height) {
                    val curGap = abs(recyclerViewCenterY - childCenterY)
                    if (curGap < minGap) {
                        curFocusView = it
                        minGap = curGap
                    }
                }
            }
        }

        return curFocusView?.let { getChildAdapterPosition(it) } ?: -1
    }
}