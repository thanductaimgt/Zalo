package vng.zalo.tdtai.zalo.ui.home.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_more.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.utils.smartLoad
import javax.inject.Inject

class MoreFragment : DaggerFragment() {
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var resourceManager: ResourceManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Picasso.get()
                .smartLoad(sessionManager.curUser!!.avatarUrl, resourceManager, avatarImgView) {
                    it.fit()
                            .centerCrop()
                }

        nameTextView.text = sessionManager.curUser!!.name
    }
}