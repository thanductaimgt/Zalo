package com.mgt.zalo.ui.story.story_detail

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.data_model.story.StoryGroup
import kotlinx.android.synthetic.main.fragment_story_detail.*
import javax.inject.Inject
import kotlin.math.abs

class StoryDetailFragment: BaseFragment() {
    @Inject
    lateinit var reactAdapter: ReactAdapter

    @Inject
    lateinit var storyPreviewAdapter: StoryPreviewAdapter

    private val viewModel: StoryDetailViewModel by viewModels { viewModelFactory }

    lateinit var storyGroup: StoryGroup

    override fun applyArguments(args: Bundle) {
        storyGroup = args.getParcelable(ARG_1)!!
    }

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        activity().showStatusBar()
        return layoutInflater.inflate(R.layout.fragment_story_detail, container, false).apply {
            makeRoomForStatusBar(requireContext(), this)
        }
    }

    override fun onBindViews() {
        viewPager.apply {
            adapter = storyPreviewAdapter
            offscreenPageLimit = 1

            val nextItemVisiblePx = 375
            val currentItemHorizontalMarginPx = 400
            val pageTranslationX = nextItemVisiblePx + currentItemHorizontalMarginPx
            val pageTransformer = ViewPager2.PageTransformer { page: View, position: Float ->
                page.translationX = -pageTranslationX * position
                // Next line scales the item's height. You can remove it if you don't want this effect
                page.scaleY = 1 - (0.25f * abs(position))
                page.scaleX = 1 - (0.25f * abs(position))
                // If you want a fading effect uncomment the next line:
                // page.alpha = 0.25f + (1 - abs(position))
            }
            setPageTransformer(pageTransformer)

            val itemDecoration = HorizontalMarginItemDecoration(
                    context,
                    currentItemHorizontalMarginPx
            )
            addItemDecoration(itemDecoration)

//            setPageTransformer(DepthTransformer())

            currentItem = storyGroup.curPosition

            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    loadReactData(position)
                }
            })
        }

        storyPreviewAdapter.submitList(storyGroup.stories)

        recyclerView.adapter = reactAdapter
        loadReactData(viewPager.currentItem)

        closeImgView.setOnClickListener(this)
    }

    private fun loadReactData(position:Int){
        val reacts = storyGroup.stories!![position].reacts
        val usersId = reacts.keys.toList()
        if(usersId.isNotEmpty()){
            database.getUsers(usersId){users->
                users.forEach {user->
                    reacts[user.id]?.apply {
                        ownerName = user.name
                        ownerAvatarUrl = user.avatarUrl
                    }
                }
                reactAdapter.submitList(reacts.values.sortedByDescending { it.createdTime })
            }
        }else{
            reactAdapter.submitList(arrayListOf())
        }
    }

    class HorizontalMarginItemDecoration(context: Context, private val horizontalMarginInPx: Int) :
            RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
                outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
        ) {
            outRect.right = horizontalMarginInPx
            outRect.left = horizontalMarginInPx
        }

    }
//    override fun onViewsBound() {
//        viewModel.liveStoryGroups.observe(viewLifecycleOwner, Observer { storyGroups ->
//            reactAdapter.submitList(storyGroups) {
//                if (lastPosition == null) {
//                    val curPosition = viewModel.liveStoryGroups.value!!.indexOfFirst { it.ownerId == curStoryGroup.ownerId && it.id == curStoryGroup.id }
//                    viewPager.setCurrentItem(curPosition, false)
//                    focusCurrentItem()
//                }
//            }
//        })
//    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.closeImgView -> parent.onBackPressed()
            R.id.imageView->{
                val position = (viewPager[0] as RecyclerView).getChildAdapterPosition(view)
                viewPager.currentItem = position
            }
        }
    }

    override fun onBackPressedCustomized(): Boolean {
        parent.onFragmentResult(BaseView.FRAGMENT_STORY_DETAIL, null)
        return false
    }
}