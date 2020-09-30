package com.mgt.zalo.common

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseDialog
import com.mgt.zalo.util.TAG
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProcessingDialog @Inject constructor() : BaseDialog() {
    private var lastShowTime: Long? = null

    init {
        isCancelable = false
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_processing, container, false)
    }

    fun show(fm: FragmentManager) {
        lastShowTime = System.currentTimeMillis()
        show(fm, TAG)
    }

    override fun dismiss() {
        lastShowTime?.let {
            val showTime = System.currentTimeMillis() - lastShowTime!!
            if (showTime >= MIN_SHOW_TIME) {
                super.dismiss()
            } else {
                Handler().postDelayed({
                    super.dismiss()
                }, MIN_SHOW_TIME - showTime)
            }
        }
    }

    companion object {
        const val MIN_SHOW_TIME = 1000
    }
}