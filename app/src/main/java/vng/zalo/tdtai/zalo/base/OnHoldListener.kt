package vng.zalo.tdtai.zalo.base

import android.os.Handler
import android.view.MotionEvent
import android.view.View


abstract class OnHoldListener : View.OnTouchListener {
    private var isPressed = false

    private val handler = Handler()
    private var mLongPressed = Runnable {
        isPressed = true
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onHoldStateChange(true)
                handler.postDelayed(mLongPressed, 250)
                false
            }
            MotionEvent.ACTION_UP -> {
                onHoldStateChange(false)
                if (!isPressed) {
                    handler.removeCallbacks(mLongPressed)
                    v.callOnClick()
                }
                isPressed = false
                true
            }
            else -> {
                false
            }
        }
    }

    abstract fun onHoldStateChange(isHold: Boolean)
}