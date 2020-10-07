package com.mgt.zalo.ui.comment.react

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.data_model.react.ReactPage
import kotlinx.android.synthetic.main.fragment_react.*
import kotlinx.android.synthetic.main.item_react_count.view.*
import javax.inject.Inject


class ReactFragment(val reacts: HashMap<String, React>) : BaseFragment() {
    private val viewModel: ReactViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var reactPagerAdapter: ReactPagerAdapter

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_react, container,
                false)
    }

    override fun onBindViews() {
        viewPager.adapter = reactPagerAdapter

        headerLayout.setOnClickListener(this)

        TabLayoutMediator(tabLayout, viewPager,
                TabLayoutMediator.TabConfigurationStrategy { tab: TabLayout.Tab, position: Int ->
                    tab.customView = createTabItemView(viewModel.liveReactPages.value!![position])
                }
        ).attach()
    }

    private fun createTabItemView(reactPage: ReactPage): View {
        return View.inflate(requireContext(), R.layout.item_react_count, null).apply {
            if (reactPage.reactType == React.TYPE_ALL) {
                reactCountTextView.text = "${requireContext().getString(R.string.description_all)} ${reactPage.reacts.size}"
                reactIcon.visibility = View.GONE
            } else {
                reactCountTextView.text = reactPage.reacts.size.toString()
                reactIcon.setImageResource(React.getDrawableResId(reactPage.reactType))
                reactIcon.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewsBound() {
//        viewModel.liveIsLoading.observe(viewLifecycleOwner, Observer {
//            if (it) {
//                interactFragment.loadingAnimView.visibility = View.VISIBLE
//            } else {
//                interactFragment.loadingAnimView.visibility = View.GONE
//            }
//        })

        viewModel.liveReactPages.observe(viewLifecycleOwner, Observer { reactPages ->
            reactPagerAdapter.submitList(reactPages)
            tabLayout.removeAllTabs()
            reactPages.forEachIndexed { index, _ ->
                tabLayout.addTab(tabLayout.newTab(), index)
            }
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.headerLayout -> {
                parent.onBackPressed()
            }
        }
    }
}