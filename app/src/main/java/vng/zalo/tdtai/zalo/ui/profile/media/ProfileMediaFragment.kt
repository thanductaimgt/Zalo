package vng.zalo.tdtai.zalo.ui.profile.media

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_preview_media.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseFragment
import vng.zalo.tdtai.zalo.common.MediaPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.ui.profile.ProfileFragment
import vng.zalo.tdtai.zalo.ui.profile.ProfileViewModel
import vng.zalo.tdtai.zalo.util.ResourceDiffCallback
import javax.inject.Inject

class ProfileMediaFragment : BaseFragment() {
    @Inject
    lateinit var profileFragment: ProfileFragment

    private val viewModel: ProfileViewModel by viewModels({ profileFragment }, { viewModelFactory })

    lateinit var mediaPreviewAdapter: MediaPreviewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_preview_media, container, false)
    }

    override fun onBindViews() {
        mediaPreviewAdapter = MediaPreviewAdapter(this, resourceManager, utils, ResourceDiffCallback())
        recyclerView.apply {
            adapter = this@ProfileMediaFragment.mediaPreviewAdapter
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    override fun onViewsBound() {
        viewModel.liveDiaries.observe(viewLifecycleOwner, Observer { diaries ->
            val resources = ArrayList<Media>().apply {
                diaries.forEach { diary ->
                    if (diary.videosUrl.isNotEmpty()) {
                        add(VideoMedia(diary.videosUrl.first()))
                    } else if (diary.imagesUrl.isNotEmpty()) {
                        add(ImageMedia(diary.imagesUrl.first()))
                    }
                }
            }

            mediaPreviewAdapter.submitList(resources)
        })
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.rootItemView -> {
                val position = recyclerView.getChildAdapterPosition(view)
                val resource = mediaPreviewAdapter.currentList[position]
                val diary = viewModel.liveDiaries.value!!.first {
                    it.videosUrl.firstOrNull() == resource.uri || it.imagesUrl.firstOrNull() == resource.uri
                }

                viewModel.liveSelectedDiary.value = diary
            }
        }
    }
}