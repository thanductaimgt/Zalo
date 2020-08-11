package vng.zalo.tdtai.zalo.base

import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration


abstract class OnHoldListener : View.OnTouchListener {
    private var isPressed = false

    private val handler = Handler()
    private var mLongPressed = Runnable {
        onHoldStateChange(true)
        isPressed = true
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout().toLong())
                true
            }
            MotionEvent.ACTION_CANCEL -> {
                handler.removeCallbacks(mLongPressed)
                false
            }
            MotionEvent.ACTION_UP -> {
                if (isPressed) {
                    onHoldStateChange(false)
                    isPressed = false
                } else {
                    handler.removeCallbacks(mLongPressed)
                    v.callOnClick()
                }
                true
            }
            else -> {
                false
            }
        }
    }

    abstract fun onHoldStateChange(isHold: Boolean)
}