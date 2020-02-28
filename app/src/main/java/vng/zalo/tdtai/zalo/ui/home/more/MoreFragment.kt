package vng.zalo.tdtai.zalo.ui.home.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_more.*

import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.utils.loadCompat

class MoreFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Picasso.get()
                .loadCompat(ZaloApplication.curUser!!.avatarUrl)
                .fit()
                .centerCrop()
                .into(avatarImgView)

        nameTextView.text = ZaloApplication.curUser!!.name
    }
}