package com.mgt.zalo.base

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.mgt.zalo.R

class BoldSelectedSpinnerAdapter<T>(
        context: Context,
        private val spinner: Spinner
) : ArrayAdapter<T>(
        context,
        android.R.layout.simple_spinner_item
) {
    override fun getDropDownView(
            position: Int,
            convertView: View?,
            parent: ViewGroup
    ): View {
        val res = super.getDropDownView(
                position,
                convertView,
                parent
        )
        val textView = res.findViewById<View>(android.R.id.text1) as TextView
        when (position) {
            spinner.selectedItemPosition -> {
                textView.setTypeface(null, Typeface.BOLD)
                textView.setTextColor(ContextCompat.getColor(context, R.color.lightPrimary))
            }
            else -> {
                textView.setTypeface(null, Typeface.NORMAL)
                textView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
            }
        }
        return res
    }
}