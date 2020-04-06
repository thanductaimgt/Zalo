package vng.zalo.tdtai.zalo.base

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment

abstract class BaseDialog : DialogFragment(), BaseView, BaseOnEventListener {
//    private var isShown = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onBindViews()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onBindViews() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
    }

    override fun dismiss() {
        try {
            super.dismiss()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }
}