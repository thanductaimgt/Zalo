package com.mgt.zalo.ui.chat.emoji

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.RenderMode
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import kotlinx.android.synthetic.main.fragment_emoji.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.data_model.StickerSetItem
import javax.inject.Inject


class EmojiFragment : BaseFragment() {
    private val viewModel: EmojiViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var adapter: EmojiAdapter

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_emoji, container, false)
    }

    override fun onBindViews() {
        viewPager.apply {
            adapter = this@EmojiFragment.adapter
            offscreenPageLimit = 5
        }
    }

    override fun onViewsBound() {
        viewModel.liveStickerSetItems.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()) {
                initTabs(it)
            }
        })
    }

    private fun initTabs(stickerSets: List<StickerSetItem>) {
        stickerSets.forEachIndexed {index, it->
            tabLayout.addTab(tabLayout.newTab(), index)//.setCustomView(createTabItemView(it.stickerUrl!!)))
            adapter.stickerSetItems.add(it)
        }

        // must call this when add tab at runtime
        adapter.notifyDataSetChanged()

        TabLayoutMediator(tabLayout, viewPager,
                TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                    tab.customView = createTabItemView(stickerSets[position].stickerUrl!!)
                }
        ).attach()
    }

    private fun createTabItemView(url: String): View {
        val animView = LottieAnimationView(context)
        animView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)

        animView.apply {
            setAnimationFromUrl(url)
            enableMergePathsForKitKatAndAbove(true)
            setRenderMode(RenderMode.SOFTWARE)
        }

        return animView
    }
}