package com.mgt.zalo.manager

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.mgt.zalo.ZaloApplication
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
import com.mgt.zalo.ui.sign_up.validate_phone.ValidatePhoneFragment
import com.mgt.zalo.ui.story.StoryFragment
import com.mgt.zalo.ui.story.story_detail.StoryDetailFragment
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.Utils
import javax.inject.Inject
import kotlin.reflect.KClass


class ZaloFragmentManager @Inject constructor(
        private val utils: Utils,
        private val application: ZaloApplication
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

    fun<T:BaseFragment> addFragment(fragmentClass: KClass<T>, args: Bundle?, addToBackStack: Boolean = true, @IdRes parentId: Int? = null): T {
        val fragment = (fragmentManager.fragmentFactory.instantiate(application.classLoader, fragmentClass.java.name) as T).apply {
            arguments = args
            init(baseView)
        }

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

    fun addStoryFragment(curStoryGroup: StoryGroup, storyGroups: ArrayList<StoryGroup>): StoryFragment {
        return addFragment(StoryFragment::class, Bundle().apply {
            putParcelable(BaseFragment.ARG_1, curStoryGroup)
            putParcelableArrayList(BaseFragment.ARG_2, storyGroups)
        })
    }

    fun removeStoryFragment() {
        removeFragment((null as StoryFragment?).TAG)
    }

    fun addEditMediaFragment(bitmap: Bitmap, type: Int): EditMediaFragment {
        return addFragment(EditMediaFragment::class, Bundle().apply {
            putParcelable(BaseFragment.ARG_1, bitmap)
            putInt(BaseFragment.ARG_2, type)
        })
    }

    fun addEditMediaFragment(videoUri: String, type: Int): EditMediaFragment {
        return addFragment(EditMediaFragment::class, Bundle().apply {
            putString(BaseFragment.ARG_1, videoUri)
            putInt(BaseFragment.ARG_3, type)
        })
    }

    fun removeEditMediaFragment() {
        removeFragment((null as EditMediaFragment?).TAG)
    }

    fun addMediaFragment(media: Media, medias: ArrayList<Media>): MediaFragment {
        return addFragment(MediaFragment::class, Bundle().apply {
            putParcelable(BaseFragment.ARG_1, media)
            putParcelableArrayList(BaseFragment.ARG_2, medias)
        })
    }

    fun removeMediaFragment() {
        removeFragment((null as MediaFragment?).TAG)
    }

    fun addProfileFragment(userId: String): ProfileFragment {
        return addFragment(ProfileFragment::class, Bundle().apply {
            putString(BaseFragment.ARG_1, userId)
        })
    }

    fun removeProfileFragment() {
        removeFragment((null as ProfileFragment?).TAG)
    }

    fun addCameraFragment(): CameraFragment {
        return addFragment(CameraFragment::class, Bundle())
    }

    fun removeCameraFragment() {
        removeFragment((null as CameraFragment?).TAG)
    }

    fun addPostDetailFragment(diary: Diary, position: Int? = null): PostDetailFragment {
        return addFragment(PostDetailFragment::class, Bundle().apply {
            putParcelable(BaseFragment.ARG_1, diary)
            position?.let { putInt(BaseFragment.ARG_2, it) }
        })
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
        return addFragment(ReactFragment::class, Bundle().apply {
            putSerializable(BaseFragment.ARG_1, reacts)
        })
    }

    fun removeReactFragment() {
        removeFragment((null as ReactFragment?).TAG)
    }

    fun addReplyFragment(comment: Comment, isReplying:Boolean = false): ReplyFragment {
        return addFragment(ReplyFragment::class, Bundle().apply {
            putParcelable(BaseFragment.ARG_1, comment)
            putBoolean(BaseFragment.ARG_2, isReplying)
        })
    }

    fun removeReplyFragment() {
        removeFragment((null as ReplyFragment?).TAG)
    }

    fun addStoryDetailFragment(storyGroup: StoryGroup): StoryDetailFragment {
        return addFragment(StoryDetailFragment::class, Bundle().apply {
            putParcelable(BaseFragment.ARG_1, storyGroup)
        })
    }

    fun removeStoryDetailFragment() {
        removeFragment((null as StoryDetailFragment?).TAG)
    }

    fun addValidatePhoneFragment(phone:String): ValidatePhoneFragment {
        return addFragment(ValidatePhoneFragment::class, Bundle().apply {
            putString(BaseFragment.ARG_1, phone)
        })
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