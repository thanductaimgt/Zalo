package com.mgt.zalo.manager

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.data_model.Comment
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.post.Diary
import com.mgt.zalo.data_model.post.Post
import com.mgt.zalo.data_model.react.React
import com.mgt.zalo.data_model.story.StoryGroup
import com.mgt.zalo.ui.camera.CameraFragment
import com.mgt.zalo.ui.comment.CommentFragment
import com.mgt.zalo.ui.comment.react.ReactFragment
import com.mgt.zalo.ui.comment.reply.ReplyFragment
import com.mgt.zalo.ui.edit_media.EditMediaFragment
import com.mgt.zalo.ui.media.MediaFragment
import com.mgt.zalo.ui.post_detail.PostDetailFragment
import com.mgt.zalo.ui.profile.ProfileFragment
import com.mgt.zalo.ui.story.StoryFragment
import com.mgt.zalo.ui.story.story_detail.StoryDetailFragment
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.Utils
import javax.inject.Inject


class ZaloFragmentManager @Inject constructor(
        private val utils: Utils
) {
    private var rootView: View? = null
    private lateinit var fragmentManager: FragmentManager
    private lateinit var baseView: BaseView

    fun init(
            fragmentManager: FragmentManager,
            baseView: BaseView
    ) {
        this.fragmentManager = fragmentManager
        this.baseView = baseView
    }

    fun setRootView(rootView: View?) {
        this.rootView = rootView
    }

    fun addFragment(fragment: BaseFragment, addToBackStack: Boolean = true, @IdRes parentId: Int? = null): Fragment {
        fragment.init(baseView)

        rootView?.let { rootView ->
            val parentView = parentId?.let { rootView.findViewById<ViewGroup>(parentId) }
                    ?: rootView

            fragmentManager.beginTransaction()
                    .add(parentView.id, fragment, fragment.getInstanceTag())
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .apply {
                        if (addToBackStack) {
                            addToBackStack(fragment.getInstanceTag())
                        }
                    }
                    .commit()
            Log.d(TAG, "added fragment ${fragment.getInstanceTag()}")

            utils.hideKeyboard(parentView)
        } ?: Log.d(TAG, "rootView is null")
        return fragment
    }

    private fun removeFragment(tag: String, popBackStack: Boolean = true) {
        val fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            fragmentManager.beginTransaction()
                    .remove(fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE)
                    .commit()

            if (popBackStack) {
                fragmentManager.popBackStack()
            }
        }
    }

    fun addStoryFragment(curStoryGroup: StoryGroup, storyGroups: List<StoryGroup>): StoryFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as StoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = StoryFragment(curStoryGroup, storyGroups)
        return addFragment(fragment) as StoryFragment
//        }
    }

    fun removeStoryFragment() {
        removeFragment((null as StoryFragment?).TAG)
    }

    fun addEditMediaFragment(bitmap: Bitmap, type: Int): EditMediaFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = EditMediaFragment(bitmap, type)
        return addFragment(fragment) as EditMediaFragment
//        }
    }

    fun addEditMediaFragment(videoUri: String, type: Int): EditMediaFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = EditMediaFragment(videoUri, type)
        return addFragment(fragment) as EditMediaFragment
//        }
    }

    fun removeEditMediaFragment() {
        removeFragment((null as EditMediaFragment?).TAG)
    }

    fun addMediaFragment(media: Media, medias: ArrayList<Media>): MediaFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as MediaFragment?).TAG)
//        if (fragment == null) {
        val fragment = MediaFragment(media, medias)
        return addFragment(fragment) as MediaFragment
//        }
    }

    fun removeMediaFragment() {
        removeFragment((null as MediaFragment?).TAG)
    }

    fun addProfileFragment(userId: String): ProfileFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as ProfileFragment?).TAG)
//        if (fragment == null) {
        val fragment = ProfileFragment(userId)
        return addFragment(fragment) as ProfileFragment
//        }
    }

    fun removeProfileFragment() {
        removeFragment((null as ProfileFragment?).TAG)
    }

    fun addCameraFragment(): CameraFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = CameraFragment()
        return addFragment(fragment) as CameraFragment
//        }
    }

    fun removeCameraFragment() {
        removeFragment((null as CameraFragment?).TAG)
    }

    fun addPostDetailFragment(diary: Diary, position: Int? = null): PostDetailFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = PostDetailFragment(diary, position)
        return addFragment(fragment) as PostDetailFragment
//        }
    }

    fun removePostDetailFragment() {
        removeFragment((null as PostDetailFragment?).TAG)
    }

    var commentFragment: CommentFragment? = null

    fun addCommentFragment(post: Post): CommentFragment {
        commentFragment = CommentFragment()
        commentFragment!!.init(baseView)
        return commentFragment!!.apply {
            this.post = post
            show(this@ZaloFragmentManager.fragmentManager, commentFragment!!.getInstanceTag())
        }
    }

    fun addReactFragment(reacts: HashMap<String, React>): ReactFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = ReactFragment(reacts)
        return addFragment(fragment) as ReactFragment
//        }
    }

    fun removeReactFragment() {
        removeFragment((null as ReactFragment?).TAG)
    }

    fun addReplyFragment(comment: Comment, isReplying:Boolean = false): ReplyFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = ReplyFragment(comment)
        fragment.isReplying = isReplying
        return addFragment(fragment) as ReplyFragment
//        }
    }

    fun removeReplyFragment() {
        removeFragment((null as ReplyFragment?).TAG)
    }

    fun addStoryDetailFragment(storyGroup: StoryGroup): StoryDetailFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = StoryDetailFragment(storyGroup)
        return addFragment(fragment) as StoryDetailFragment
    }

    fun removeStoryDetailFragment() {
        removeFragment((null as StoryDetailFragment?).TAG)
    }

    // return true if any fragment popped out of stack
    fun popTopFragment(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            val fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 1).name
            val fragment = fragmentManager.findFragmentByTag(fragmentTag)

            if (fragment is BaseFragment && !fragment.onBackPressed()) {
                fragmentManager.popBackStack()
                fragment.parent.onFragmentResult(BaseView.FRAGMENT_ANY, null)
                rootView?.let { utils.hideKeyboard(it) }
            }
            true
        } else {
            false
        }
    }

    // pop top fragment, except it's last fragment in stack and its fragment stack is empty
    // return true if any fragment popped
    fun popTopFragmentExceptLast(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            val fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 1).name
            val fragment = fragmentManager.findFragmentByTag(fragmentTag)

            if (fragment is BaseFragment && !fragment.onBackPressed()) {
                if (fragmentManager.backStackEntryCount == 1) {
                    return false
                } else {
                    fragmentManager.popBackStack()
                    fragment.parent.onFragmentResult(BaseView.FRAGMENT_ANY, null)
                    rootView?.let { utils.hideKeyboard(it) }
                }
            }
            true
        } else {
            false
        }
    }
}