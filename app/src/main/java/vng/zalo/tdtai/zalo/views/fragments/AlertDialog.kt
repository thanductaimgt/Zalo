package vng.zalo.tdtai.zalo.views.fragments

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.dialog_confirm.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.utils.TAG


class AlertDialog : DialogFragment(),
    View.OnClickListener {
    private var title:String?=null
    private var description: String?=null
    private var button1Text:String?=null
    private var button1Action:(()->Any)?=null
    private var button2Text:String?=null
    private var button2Action:(()->Any)?=null
    private var button3Text:String?=null
    private var button3Action:(()->Any)?=null

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.buttonTextView1 -> {
                button1Action?.invoke()
            }
            R.id.buttonTextView2 -> {
                button2Action?.invoke()
            }
            R.id.buttonTextView3 -> {
                button3Action?.invoke()
            }
        }
        dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_confirm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initView() {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)

        titleTextView.text = title
        descTextView.text = description

        if(button1Text!=null) {
            buttonTextView1.text = button1Text
        }else{
            buttonTextView1.text = getString(R.string.label_ok)
        }
        buttonTextView1.setOnClickListener(this)

        button2Action?.let {
            buttonTextView2.text = button2Text
            buttonTextView2.setOnClickListener(this)
            button2Layout.visibility = View.VISIBLE

        }
        button3Action?.let {
            buttonTextView3.text = button3Text
            buttonTextView3.setOnClickListener(this)
            button3Layout.visibility = View.VISIBLE
        }
    }

    fun show(fm:FragmentManager, title:String, description:String,
             button1Text:String?=null,
             button1Action:(()->Any)?=null,
             button2Text:String?=null,
             button2Action:(()->Any)?=null,
             button3Text:String?=null,
             button3Action:(()->Any)?=null) {
        this.title = title
        this.description = description
        this.button1Text = button1Text
        this.button1Action = button1Action
        this.button2Text = button2Text
        this.button2Action = button2Action
        this.button3Text = button3Text
        this.button3Action = button3Action
        show(fm, TAG)
    }
}