package vng.zalo.tdtai.zalo.views.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_sticker_set.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.StickerSetFragmentAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.viewmodels.StickerSetViewModel
import vng.zalo.tdtai.zalo.views.activities.RoomActivity

class StickerSetFragment(private val bucketName: String) : Fragment(), View.OnClickListener {
    private lateinit var viewModel: StickerSetViewModel
    private lateinit var adapter: StickerSetFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sticker_set, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(bucketName = bucketName)).get(StickerSetViewModel::class.java)
        viewModel.liveStickerSet.observe(viewLifecycleOwner, Observer {
            adapter.stickers = it.stickers!!
            adapter.notifyDataSetChanged()
            Log.d("onViewCreated", "adapter.notifyDataSetChanged()")
        })
    }

    private fun initView() {
        adapter = StickerSetFragmentAdapter(this)
        with(recyclerView) {
            layoutManager = GridLayoutManager(activity, COLUMN_NUM)
            adapter = this@StickerSetFragment.adapter
            setHasFixedSize(true)
            setItemViewCacheSize(25)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.animView -> {
                val position = recyclerView.getChildLayoutPosition(v)
                val sticker = adapter.stickers[position]
                (activity as RoomActivity).sendMessages(ArrayList<String>().apply { add(sticker.url!!) }, Message.TYPE_STICKER)
            }
        }
    }

    companion object {
        const val COLUMN_NUM = 5
    }
}