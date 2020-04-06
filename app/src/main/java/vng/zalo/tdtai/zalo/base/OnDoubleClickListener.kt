package vng.zalo.tdtai.zalo.base

import android.view.MotionEvent
import android.view.View


abstract class OnDoubleClickListener : View.OnTouchListener {
    private var lastClickTime: Long? = null

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val now = System.currentTimeMillis()
        if (lastClickTime != null && now - lastClickTime!! < DOUBLE_CLICK_TIME_DELTA) {
            onDoubleClick()
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            lastClickTime = now
        }
        return false
    }

    abstract fun onDoubleClick()

    companion object{
        private const val DOUBLE_CLICK_TIME_DELTA: Long = 300
    }
}