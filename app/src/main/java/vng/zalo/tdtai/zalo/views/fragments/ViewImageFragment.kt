package vng.zalo.tdtai.zalo.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_view_image.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.message.ImageMessage
import vng.zalo.tdtai.zalo.utils.Utils

class ViewImageFragment(private val clickListener: View.OnClickListener, val imageMessage: ImageMessage) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_view_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {
        Picasso.get().load(
                if (Utils.isContentUri(imageMessage.url) || Utils.isNetworkUri(imageMessage.url)) {
                    imageMessage.url
                } else {
                    "file://${imageMessage.url}"
                }
        ).fit().centerInside().into(photoView)

        photoView.setOnClickListener(clickListener)
    }
}