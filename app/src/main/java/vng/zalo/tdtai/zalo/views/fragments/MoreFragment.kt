package vng.zalo.tdtai.zalo.views.fragments

import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_more.*

import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication

class MoreFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Picasso.get()
                .load(ZaloApplication.currentUser!!.avatarUrl)
                .fit()
                .into(avatarImgView)

        nameTextView.text = ZaloApplication.currentUser!!.phone

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val outValue = TypedValue()
            context!!.theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            postStatusImgView.setBackgroundResource(outValue.resourceId)
        }
    }
}