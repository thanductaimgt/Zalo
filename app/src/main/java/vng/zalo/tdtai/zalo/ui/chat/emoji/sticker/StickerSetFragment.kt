package vng.zalo.tdtai.zalo.ui.chat.emoji.sticker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.fragment_sticker_set.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.di.ViewModelFactory
import vng.zalo.tdtai.zalo.model.message.Message
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import javax.inject.Inject

class StickerSetFragment(private val bucketName: String) : Fragment(), View.OnClickListener, HasAndroidInjector {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: StickerSetViewModel by viewModels { viewModelFactory }

    @Inject lateinit var adapter: StickerSetAdapter

    override fun androidInjector(): AndroidInjector<Any> {
        return DaggerStickerSetComponent.factory().create(bucketName)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sticker_set, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel.liveStickerSet.observe(viewLifecycleOwner, Observer {
            adapter.stickers = it.stickers!!
            adapter.notifyDataSetChanged()
            Log.d("onViewCreated", "adapter.notifyDataSetChanged()")
        })
    }

    private fun initView() {
        adapter = StickerSetAdapter(this)
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
                val position = recyclerView.getChildAdapterPosition(v)
                val sticker = adapter.stickers[position]
                (activity as ChatActivity).sendMessages(ArrayList<String>().apply { add(sticker.url!!) }, Message.TYPE_STICKER)
            }
        }
    }

    companion object {
        const val COLUMN_NUM = 5
    }
}