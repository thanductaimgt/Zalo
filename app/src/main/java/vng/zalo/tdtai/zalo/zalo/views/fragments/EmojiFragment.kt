package vng.zalo.tdtai.zalo.zalo.views.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_emoji.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.adapters.EmojiFragmentAdapter
import vng.zalo.tdtai.zalo.zalo.models.StickerSet
import vng.zalo.tdtai.zalo.zalo.viewmodels.EmojiFragmentViewModel
import android.widget.ImageView
import com.squareup.picasso.Picasso


class EmojiFragment : Fragment(), TabLayout.OnTabSelectedListener {
    private lateinit var viewModel: EmojiFragmentViewModel
    private lateinit var adapter: EmojiFragmentAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_emoji, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()

        viewModel = ViewModelProviders.of(this).get(EmojiFragmentViewModel::class.java)
        viewModel.liveStickerSets.observe(viewLifecycleOwner, Observer {
            initTabs(it)
        })
    }

    private fun initView() {
        adapter = EmojiFragmentAdapter(childFragmentManager)

        viewPager.apply {
            adapter = this@EmojiFragment.adapter
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        tabLayout.addOnTabSelectedListener(this)
    }

    private fun initTabs(stickerSets: List<StickerSet>) {
        stickerSets.forEach {
            tabLayout.addTab(tabLayout.newTab().setCustomView(createTabItemView(it.avatarUrl!!)))
            adapter.stickerSets.add(it)
            // must call this when add tab at runtime
            adapter.notifyDataSetChanged()
        }
    }

    private fun createTabItemView(imgUri: String): View {
        val imageView = ImageView(context)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.layoutParams = params
        if (imgUri != "") {
            Picasso.get().load(imgUri).into(imageView)

        } else {
            imageView.setImageResource(R.drawable.hot_cherry)
        }

        return imageView
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager.currentItem = tab.position
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {

    }

    override fun onTabReselected(tab: TabLayout.Tab) {

    }
}