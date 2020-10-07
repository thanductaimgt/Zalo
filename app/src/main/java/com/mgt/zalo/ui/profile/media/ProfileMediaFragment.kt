package com.mgt.zalo.ui.profile.media

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.common.MediaPreviewAdapter
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.ui.home.HomeActivity
import com.mgt.zalo.ui.profile.ProfileFragment
import com.mgt.zalo.ui.profile.ProfileViewModel
import com.mgt.zalo.util.diff_callback.MediaDiffCallback
import kotlinx.android.synthetic.main.fragment_profile_media.*
import javax.inject.Inject

class ProfileMediaFragment : BaseFragment() {
    @Inject
    lateinit var profileFragment: ProfileFragment

    private val viewModel: ProfileViewModel by viewModels({ profileFragment }, { viewModelFactory })

    lateinit var mediaPreviewAdapter: MediaPreviewAdapter

    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_profile_media, container, false)
    }

    override fun onBindViews() {
        mediaPreviewAdapter = MediaPreviewAdapter(this, MediaDiffCallback())
        mediaGridView.adapter = mediaPreviewAdapter
    }

    override fun onViewsBound() {
        viewModel.liveDiaries.observe(viewLifecycleOwner, Observer { diaries ->
            val resources = ArrayList<Media>().apply {
                diaries.forEach { diary ->
                    diary.getFirstMedia()?.let { add(it) }
                }
            }

            mediaPreviewAdapter.submitList(resources)
        })

        if(activity() is HomeActivity) {
            val homeActivity = activity() as HomeActivity
            homeActivity.liveSelectedPageListener.observe(viewLifecycleOwner, Observer { position ->
                if (position == 4) {
                    mediaGridView.smoothScrollToPosition(0)
                }
            })
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.rootItemView -> {
                val position = mediaGridView.getChildAdapterPosition(view)
                val media = mediaPreviewAdapter.currentList[position]
                val diary = viewModel.liveDiaries.value!!.first {
                    it.medias.firstOrNull() == media
                }

                viewModel.liveSelectedDiary.value = diary
            }
        }
    }
}