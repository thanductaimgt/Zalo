package vng.zalo.tdtai.zalo.zalo.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_sticker_set.*
import kotlinx.android.synthetic.main.item_sticker.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.adapters.StickerSetFragmentAdapter
import vng.zalo.tdtai.zalo.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.viewmodels.StickerSetViewModel

class StickerSetFragment(private val bucket_name: String) : Fragment(), View.OnClickListener {
    private lateinit var viewModel: StickerSetViewModel
    private lateinit var adapter: StickerSetFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sticker_set, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(bucket_name = bucket_name)).get(StickerSetViewModel::class.java)
        viewModel.liveStickerLinks.observe(viewLifecycleOwner, Observer {
            adapter.stickerLinks = it
            adapter.notifyDataSetChanged()
            Log.d("onViewCreated", "adapter.notifyDataSetChanged()")
        })
    }

    private fun initView() {
        adapter = StickerSetFragmentAdapter(this)
        with(recyclerView) {
            layoutManager = GridLayoutManager(activity, COLUMN_NUM)
            adapter = this@StickerSetFragment.adapter
        }

        animView.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.animView -> {
                if (animView.isAnimating) {
                    animView.resumeAnimation()
                } else {
                    animView.pauseAnimation()
                }
            }
        }
    }

    companion object {
        const val COLUMN_NUM = 5
    }
}