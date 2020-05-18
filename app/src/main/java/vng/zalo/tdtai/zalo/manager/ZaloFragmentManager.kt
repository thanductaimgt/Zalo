package vng.zalo.tdtai.zalo.manager

import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.base.BaseView
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.message.Message
import vng.zalo.tdtai.zalo.data_model.post.Diary
import vng.zalo.tdtai.zalo.data_model.story.StoryGroup
import vng.zalo.tdtai.zalo.ui.camera.CameraFragment
import vng.zalo.tdtai.zalo.ui.edit_media.EditMediaFragment
import vng.zalo.tdtai.zalo.ui.media.MediaFragment
import vng.zalo.tdtai.zalo.ui.post_detail.PostDetailFragment
import vng.zalo.tdtai.zalo.ui.profile.ProfileFragment
import vng.zalo.tdtai.zalo.ui.story.StoryFragment
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.Utils
import javax.inject.Inject


class ZaloFragmentManager @Inject constructor(
        private val utils: Utils,
        private val application: ZaloApplication
) {
    private var rootView: View? = null
    private lateinit var fragmentManager: FragmentManager
    private lateinit var baseView: BaseView

    fun init(
            rootView: View?,
            fragmentManager: FragmentManager,
            baseView: BaseView
    ) {
        this.rootView = rootView
        this.fragmentManager = fragmentManager
        this.baseView = baseView
    }

    fun addFragment(fragment: BaseFragment, addToBackStack: Boolean = true, @IdRes parentId: Int? = null): Fragment {
        fragment.init(baseView)

        rootView?.let { rootView ->
            val parentView = parentId?.let { rootView.findViewById<ViewGroup>(parentId) }
                    ?: rootView

            fragmentManager.beginTransaction()
                    .add(parentView.id, fragment, fragment.getInstanceTag())
                    .setTransition(FragmentTransaction.TRANSIT_NONE)
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
                    .setTransition(FragmentTransaction.TRANSIT_NONE)
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

    fun addMediaFragment(media:Media, medias:ArrayList<Media>): MediaFragment {
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

    fun addPostDetailFragment(diary:Diary, position:Int?=null): PostDetailFragment {
//        var fragment = supportFragmentManager.findFragmentByTag((null as CreateStoryFragment?).TAG)
//        if (fragment == null) {
        val fragment = PostDetailFragment(diary, position)
        return addFragment(fragment) as PostDetailFragment
//        }
    }

    fun removePostDetailFragment() {
        removeFragment((null as PostDetailFragment?).TAG)
    }

    // return true if any fragment popped out of stack
    fun popTopFragment(): Boolean {
        return if (fragmentManager.backStackEntryCount > 0) {
            val fragmentTag = fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount - 1).name
            val fragment = fragmentManager.findFragmentByTag(fragmentTag)

            if (fragment !is BaseFragment || !fragment.onBackPressed()) {
                fragmentManager.popBackStack()
            }
            true
        } else {
            false
        }
    }
}