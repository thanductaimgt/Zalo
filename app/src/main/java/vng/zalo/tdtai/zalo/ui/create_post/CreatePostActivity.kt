package vng.zalo.tdtai.zalo.ui.create_post

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_post.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.base.NotScrollableLinearLayoutManager
import vng.zalo.tdtai.zalo.common.MediaPreviewAdapter
import vng.zalo.tdtai.zalo.data_model.media.ImageMedia
import vng.zalo.tdtai.zalo.data_model.media.Media
import vng.zalo.tdtai.zalo.data_model.media.VideoMedia
import vng.zalo.tdtai.zalo.manager.ExternalIntentManager
import vng.zalo.tdtai.zalo.util.Constants
import vng.zalo.tdtai.zalo.util.TAG
import vng.zalo.tdtai.zalo.util.smartLoad
import vng.zalo.tdtai.zalo.widget.SpannedGridLayoutManager
import javax.inject.Inject

class CreatePostActivity : BaseActivity() {
    private val viewModel: CreatePostViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var mediaPreviewAdapter: MediaPreviewAdapter

    private lateinit var gridLayoutManager: SpannedGridLayoutManager
    private lateinit var linearLayoutManager: NotScrollableLinearLayoutManager

    override fun onBindViews() {
        setContentView(R.layout.activity_create_post)

        Picasso.get().smartLoad(sessionManager.curUser!!.avatarUrl, resourceManager, avatarImgView) {
            it.fit().centerCrop().error(R.drawable.default_peer_avatar)
        }
        nameTextView.text = sessionManager.curUser!!.name

        editText.setMinTextSize(resources.getDimension(R.dimen.sizeTextBig))
        editText.addTextChangedListener {
            checkEnablePostButton()
        }

        recyclerView.adapter = mediaPreviewAdapter
        gridLayoutManager = object :SpannedGridLayoutManager(
                SpannedGridLayoutManager.GridSpanLookup { position ->
                    val spanCount = when (mediaPreviewAdapter.itemCount) {
                        1 -> 6
                        2 -> return@GridSpanLookup SpannedGridLayoutManager.SpanInfo(3, 6)
                        3 -> {
                            when (position) {
                                0 -> return@GridSpanLookup SpannedGridLayoutManager.SpanInfo(4, 6)
                                else -> return@GridSpanLookup SpannedGridLayoutManager.SpanInfo(2, 3)
                            }
                        }
                        4 -> {
                            when (position) {
                                0 -> return@GridSpanLookup SpannedGridLayoutManager.SpanInfo(4, 6)
                                else -> 2
                            }
                        }
                        else -> {
                            when {
                                position < 2 -> 3
                                else -> 2
                            }
                        }
                    }
                    return@GridSpanLookup SpannedGridLayoutManager.SpanInfo(spanCount, spanCount)
                },
                6,
                1f
        ){
            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        linearLayoutManager = NotScrollableLinearLayoutManager(this)

        imageImgView.setOnClickListener(this)
        videoImgView.setOnClickListener(this)
        cameraImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
        postButton.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveDiary.observe(this, Observer { diary ->
            var resources = ArrayList<Media>().apply {
                addAll(diary.imagesUrl.map { ImageMedia(it) })
                addAll(diary.videosUrl.map { VideoMedia(it) })
            }

            val lastMoreCount = mediaPreviewAdapter.moreCount
            if (resources.size > 5) {
                mediaPreviewAdapter.moreCount = resources.size - 4
                resources = resources.take(5).toMutableList() as ArrayList<Media>
            } else {
                mediaPreviewAdapter.moreCount = 0
            }
            recyclerView.layoutManager = if (resources.size == 1) {
                recyclerView.post {
                    recyclerView.layoutParams = recyclerView.layoutParams.apply {
                        height = recyclerView.width
                    }
                }
                linearLayoutManager
            } else {
                recyclerView.post {
                    recyclerView.layoutParams = recyclerView.layoutParams.apply {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
                gridLayoutManager
            }
            val moreCountDiff = mediaPreviewAdapter.moreCount != lastMoreCount
            val shouldScrollToBottom = mediaPreviewAdapter.itemCount < resources.size
            mediaPreviewAdapter.submitList(resources) {
                checkEnablePostButton()
                if (shouldScrollToBottom) {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }
                if (moreCountDiff) {
                    mediaPreviewAdapter.notifyItemRangeChanged(0, mediaPreviewAdapter.itemCount, arrayListOf(Media.PAYLOAD_DISPLAY_MORE))
                }
            }
        })
    }

    private fun checkEnablePostButton() {
        postButton.isEnabled = !((editText.text == null || editText.text!!.toString() == "") && viewModel.liveDiary.value!!.imagesUrl.isEmpty() && viewModel.liveDiary.value!!.videosUrl.isEmpty())
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imageImgView -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_IMAGE_REQUEST, ExternalIntentManager.CHOOSER_TYPE_IMAGE, true)
            }
            R.id.videoImgView -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_VIDEO_REQUEST, ExternalIntentManager.CHOOSER_TYPE_VIDEO, true)
            }
            R.id.cameraImgView -> {
                addCameraFragment()
            }
            R.id.backImgView -> {
                onBackPressed()
            }
            R.id.postButton -> {
                Toast.makeText(this, "not implemented", Toast.LENGTH_SHORT).show()
//                viewModel.createPost()
            }
            R.id.rootItemView -> {
                val position = recyclerView.getChildAdapterPosition(view)
                val resource = mediaPreviewAdapter.currentList[position]

                viewModel.liveDiary.value = viewModel.liveDiary.value!!.apply {
                    imagesUrl = imagesUrl.toMutableList().apply { remove(resource.uri) }
                    videosUrl = videosUrl.toMutableList().apply { remove(resource.uri) }
                }
            }
        }
    }

    override fun onBackPressedCustomized(): Boolean {
        if (!super.onBackPressedCustomized()) {
            alertDialog.show(supportFragmentManager,
                    title = getString(R.string.label_discard_post),
                    description = getString(R.string.description_discard_post),
                    button1Text = getString(R.string.label_keep),
                    button2Text = getString(R.string.label_discard),
                    button2Action = {
                        finish()
                    }
            )
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, intent: Intent?) {
        utils.assertNotNull(intent, TAG, "CHOOSE_IMAGES_REQUEST.intent") { intentNotNull ->
            val localUris = utils.getLocalUris(intentNotNull)

            viewModel.liveDiary.value = viewModel.liveDiary.value!!.apply {
                when (requestCode) {
                    Constants.CHOOSE_IMAGE_REQUEST -> {
                        imagesUrl = imagesUrl.toMutableList().apply { addAll(localUris) }
                    }
                    else -> {
                        videosUrl = videosUrl.toMutableList().apply { addAll(localUris) }
                    }
                }
            }
        }
    }

    override fun onFragmentResult(fragmentType: Int, result: Any?) {
        when (fragmentType) {
            FRAGMENT_EDIT_MEDIA -> {
                processingDialog.dismiss()
                removeCameraFragment()
                removeEditMediaFragment()

                viewModel.liveDiary.value = viewModel.liveDiary.value!!.apply {
                    when (result) {
                        is ImageMedia -> imagesUrl = imagesUrl.toMutableList().apply { add(result.uri) }
                        is VideoMedia -> videosUrl = videosUrl.toMutableList().apply { add(result.uri) }
                    }
                }
            }
            FRAGMENT_CAMERA -> {
                removeCameraFragment()
            }
        }
    }
}