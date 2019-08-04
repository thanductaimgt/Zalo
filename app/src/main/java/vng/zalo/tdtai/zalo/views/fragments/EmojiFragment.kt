package vng.zalo.tdtai.zalo.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.RenderMode
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_emoji.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.EmojiFragmentAdapter
import vng.zalo.tdtai.zalo.models.StickerSetItem
import vng.zalo.tdtai.zalo.viewmodels.EmojiFragmentViewModel


class EmojiFragment : Fragment(), TabLayout.OnTabSelectedListener {
    private lateinit var viewModel: EmojiFragmentViewModel
    private lateinit var adapter: EmojiFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emoji, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProvider(this).get(EmojiFragmentViewModel::class.java)
        viewModel.liveStickerSetItems.observe(viewLifecycleOwner, Observer {
            initTabs(it)
        })
    }

    private fun initView() {
        adapter = EmojiFragmentAdapter(childFragmentManager)

        viewPager.apply {
            adapter = this@EmojiFragment.adapter
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
            offscreenPageLimit = 5
        }

        tabLayout.addOnTabSelectedListener(this)
    }

    private fun initTabs(stickerSets: List<StickerSetItem>) {
        stickerSets.forEach {
            tabLayout.addTab(tabLayout.newTab().setCustomView(createTabItemView(it.stickerUrl)))
            adapter.stickerSetItems.add(it)
        }
        // must call this when add tab at runtime
        adapter.notifyDataSetChanged()
    }

    private fun createTabItemView(url: String?): View {
        val animView = LottieAnimationView(context)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        params.width = params.height
        animView.layoutParams = params

        url.let {
            animView.apply {
                setAnimationFromUrl(url)
                enableMergePathsForKitKatAndAbove(true)
                setRenderMode(RenderMode.SOFTWARE)
            }
        }

        return animView
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {

    }

    override fun onTabReselected(tab: TabLayout.Tab) {

    }
}