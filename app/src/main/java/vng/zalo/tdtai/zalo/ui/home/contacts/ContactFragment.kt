package vng.zalo.tdtai.zalo.ui.home.contacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_contact.*
import vng.zalo.tdtai.zalo.R
import javax.inject.Inject

class ContactFragment : DaggerFragment() {
    @Inject lateinit var adapter:ContactFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {
        viewPager.adapter = adapter

        tabLayout.setupWithViewPager(viewPager)
    }
}