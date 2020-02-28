package vng.zalo.tdtai.zalo.ui.chat.emoji

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.RenderMode
import com.google.android.material.tabs.TabLayout
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.fragment_emoji.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.model.StickerSetItem
import javax.inject.Inject


class EmojiFragment : Fragment(), TabLayout.OnTabSelectedListener, HasAndroidInjector {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: EmojiViewModel by viewModels { viewModelFactory }

    @Inject lateinit var adapter: EmojiAdapter

    override fun androidInjector(): AndroidInjector<Any> {
        return DaggerEmojiComponent.factory().create(childFragmentManager)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emoji, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel.liveStickerSetItems.observe(viewLifecycleOwner, Observer {
            initTabs(it)
        })
    }

    private fun initView() {
        adapter = EmojiAdapter(childFragmentManager)

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
                ViewGroup.LayoutParams.MATCH_PARENT)
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