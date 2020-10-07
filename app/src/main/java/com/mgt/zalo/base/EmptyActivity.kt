package com.mgt.zalo.base

import android.view.ViewGroup
import com.mgt.zalo.R
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.post.Diary

class EmptyActivity : BaseActivity() {
    override fun onBindViews() {
        requestFullScreen()
        findViewById<ViewGroup>(android.R.id.content)?.background = null
        setContentView(R.layout.activity_empty)
    }

    override fun onViewsBound() {
        when(intent.extras?.getInt(KEY_FRAGMENT)){
            BaseView.FRAGMENT_PROFILE->{
                val userId = intent.extras!!.getString(KEY_USER_ID)!!
                zaloFragmentManager.addProfileFragment(userId)
            }
            BaseView.FRAGMENT_MEDIA->{
                val media = intent.extras!!.getParcelable<Media>(KEY_MEDIA)!!
                val medias = intent.extras!!.getParcelableArrayList<Media>(KEY_MEDIAS)!!

                zaloFragmentManager.addMediaFragment(media, medias)
            }
            BaseView.FRAGMENT_POST_DETAIL->{
                val diary = intent.extras!!.getParcelable<Diary>(KEY_DIARY)!!

                zaloFragmentManager.addPostDetailFragment(diary)
            }
        }
    }

    override fun onBackPressed() {
        if (!zaloFragmentManager.popTopFragmentExceptLast()) {
            finish()
        }
    }

    companion object{
        const val KEY_FRAGMENT = "KEY_FRAGMENT"

        const val KEY_USER_ID = "KEY_USER_ID"

        const val KEY_MEDIA = "KEY_MEDIA"
        const val KEY_MEDIAS = "KEY_MEDIAS"

        const val KEY_DIARY = "KEY_DIARY"
    }
}