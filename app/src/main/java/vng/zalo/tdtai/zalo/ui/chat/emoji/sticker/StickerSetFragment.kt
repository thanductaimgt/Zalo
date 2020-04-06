package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_sticker_set.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import javax.inject.Inject

class StickerSetFragment(val bucketName: String) : BaseFragment() {
    private val viewModel: StickerSetViewModel by viewModels { viewModelFactory }

    @Inject lateinit var adapter: StickerSetAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sticker_set, container, false)
    }

    override fun onBindViews() {
        recyclerView.apply {
            layoutManager = GridLayoutManager(activity, COLUMN_NUM)
            adapter = this@StickerSetFragment.adapter
            setHasFixedSize(true)
            setItemViewCacheSize(25)
        }
    }

    override fun onViewsBound() {
        viewModel.liveStickerSet.observe(viewLifecycleOwner, Observer {
            adapter.stickers = it.stickers!!
            adapter.notifyDataSetChanged()
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.animView -> {
                val position = recyclerView.getChildAdapterPosition(view)
                val sticker = adapter.stickers[position]
                (activity as ChatActivity).sendMessages(ArrayList<String>().apply { add(sticker.url!!) }, Message.TYPE_STICKER)
            }
        }
    }

    companion object {
        const val COLUMN_NUM = 5
    }
}