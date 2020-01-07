package vng.zalo.tdtai.zalo.views.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_zoom_image.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.utils.TAG


class ZoomImageDialog(private val fm: FragmentManager) : DialogFragment(),
        View.OnClickListener {
    private lateinit var message: Message

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_zoom_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {
        // dialog full screen
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        Picasso.get().load(message.content).fit().centerInside().into(expandedImgView)
        imageSenderNameTextView.text = message.senderPhone

        backImgView.setOnClickListener(this)
        expandedImgView.setOnClickListener(this)
        downloadImgView.setOnClickListener(this)
        moreImgView.setOnClickListener(this)
    }

    fun show(message: Message) {
        this.message = message
        show(fm, TAG)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backImgView -> {
                dismiss()
            }
            R.id.expandedImgView -> {
                if (zoomTitleLayout.visibility == View.VISIBLE) {
                    zoomTitleLayout.visibility = View.GONE
                } else {
                    zoomTitleLayout.visibility = View.VISIBLE
                }
            }
            R.id.downloadImgView -> {
            }
            R.id.moreImgView -> {
            }
        }
    }
}