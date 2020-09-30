package com.mgt.zalo.base

import android.view.View


interface BaseOnEventListener : View.OnClickListener, View.OnLongClickListener{
    override fun onClick(view: View) {
    }

    override fun onLongClick(view: View): Boolean {
        return false
    }
}