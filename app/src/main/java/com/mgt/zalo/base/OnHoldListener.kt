package com.mgt.zalo.base

import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration


abstract class OnHoldListener : View.OnTouchListener {
    private var isPressed = false

    private val handler = Handler(Looper.getMainLooper())
    private var mLongPressed = Runnable {
        onHoldStateChange(true)
        isPressed = true
    }

    private var initX = 0f
    private var initY = 0f

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initX = event.x
                initY = event.y
                handler.postDelayed(mLongPressed, ViewConfiguration.getLongPressTimeout().toLong()/2)
                true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_MOVE -> {
                if(event.x - initX > 10 || event.y - initY > 10){
                    handler.removeCallbacks(mLongPressed)
                }
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