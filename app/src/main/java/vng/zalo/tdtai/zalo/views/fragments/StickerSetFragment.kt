package vng.zalo.tdtai.zalo.views.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.android.synthetic.main.fragment_sticker_set.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.StickerSetFragmentAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.Sticker
import vng.zalo.tdtai.zalo.viewmodels.StickerSetViewModel
import vng.zalo.tdtai.zalo.views.activities.RoomActivity
import java.lang.ref.WeakReference

class StickerSetFragment(private val bucket_name: String) : Fragment(), View.OnClickListener {
    private lateinit var viewModel: StickerSetViewModel
    private lateinit var adapter: StickerSetFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sticker_set, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(bucket_name = bucket_name)).get(StickerSetViewModel::class.java)
        viewModel.liveStickerSet.observe(viewLifecycleOwner, Observer {
            adapter.stickers = it.stickers!!
            adapter.notifyDataSetChanged()
//            ParseTask(this).execute(it.stickers)
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
                (activity as RoomActivity).sendStickerMessage(sticker)
            }
        }
    }

    companion object {
        const val COLUMN_NUM = 5
    }

    class ParseTask(fragment: Fragment) : AsyncTask<List<Sticker>, Void, List<Sticker>?>() {
        private var weakReference: WeakReference<Fragment> = WeakReference(fragment)

        override fun doInBackground(vararg params: List<Sticker>?): List<Sticker>? {
            val fragment = weakReference.get()
            if(fragment != null && params.isNotEmpty()){
                if(params[0] != null){
                    val stickers = params[0]!!
                    var loadedNum = 0
                    stickers.forEach {
                        LottieCompositionFactory.fromUrl(fragment.context, it.url).addListener {lottieComposition->
                            it.lottieComposition = lottieComposition
                            synchronized(loadedNum){
                                loadedNum++
                            }
                        }
                    }
                    while (loadedNum < stickers.size){}
                    return stickers
                }
            }
            return null
        }

        override fun onPostExecute(result: List<Sticker>?) {
            val fragment = weakReference.get()
            if(fragment != null){
                (fragment as StickerSetFragment).apply {
                    adapter.stickers = result!!
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }
}