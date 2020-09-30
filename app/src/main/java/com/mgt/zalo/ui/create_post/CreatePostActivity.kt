package com.mgt.zalo.ui.create_post

import android.content.Intent
import android.view.View
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_post.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.common.MediaPreviewAdapter
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.manager.ExternalIntentManager
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.smartLoad
import javax.inject.Inject

class CreatePostActivity : BaseActivity() {
    private val viewModel: CreatePostViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var mediaPreviewAdapter: MediaPreviewAdapter

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

        mediaGridView.adapter = mediaPreviewAdapter

        imageImgView.setOnClickListener(this)
        videoImgView.setOnClickListener(this)
        cameraImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
        postButton.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveDiary.observe(this, Observer { diary ->
            val oldMoreCount = mediaPreviewAdapter.moreCount
            val oldSize = mediaPreviewAdapter.itemCount
            mediaPreviewAdapter.submitListLimit(diary.medias) {
                checkEnablePostButton()
                if (oldSize < mediaPreviewAdapter.itemCount) {
                    scrollView.fullScroll(ScrollView.FOCUS_DOWN)
                }

                val moreCountDiff = mediaPreviewAdapter.moreCount != oldMoreCount
                if (moreCountDiff) {
                    mediaPreviewAdapter.notifyItemRangeChanged(0, mediaPreviewAdapter.itemCount, arrayListOf(Media.PAYLOAD_DISPLAY_MORE))
                }
            }
        })
    }

    private fun checkEnablePostButton() {
        postButton.isEnabled = !((editText.text == null || editText.text!!.toString() == "") && viewModel.liveDiary.value!!.medias.isEmpty())
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
                zaloFragmentManager.addCameraFragment()
            }
            R.id.backImgView -> {
                onBackPressed()
            }
            R.id.postButton -> {
                viewModel.liveDiary.value!!.apply {
                    text = editText.text.toString()
                    createdTime = System.currentTimeMillis()
                }

                viewModel.createDiary()
                Toast.makeText(this, getString(R.string.description_uploading_post), Toast.LENGTH_SHORT).show()
                finish()
            }
            R.id.rootItemView -> {
                val position = mediaGridView.getChildAdapterPosition(view)
                val media = mediaPreviewAdapter.currentList[position]

                viewModel.liveDiary.value = viewModel.liveDiary.value!!.apply {
                    medias.remove(media)
                }
            }
        }
    }

//    override fun onBackPressedCustomized() {
//        alertDialog.show(supportFragmentManager,
//                title = getString(R.string.label_discard_post),
//                description = getString(R.string.description_discard_post),
//                button1Text = getString(R.string.label_keep),
//                button2Text = getString(R.string.label_discard),
//                button2Action = {
//                    finish()
//                }
//        )
//    }

    override fun onActivityResult(requestCode: Int, intent: Intent?) {
        utils.assertNotNull(intent, TAG, "CHOOSE_IMAGES_REQUEST.intent") { intentNotNull ->
            val localUris = utils.getLocalUris(intentNotNull)

            when (requestCode) {
                Constants.CHOOSE_IMAGE_REQUEST -> {
                    viewModel.createMedias(localUris, Media.TYPE_IMAGE) {
                        viewModel.liveDiary.value = viewModel.liveDiary.value!!.apply {
                            medias.addAll(it)
                        }
                    }
                }
                Constants.CHOOSE_VIDEO_REQUEST -> {
                    viewModel.createMedias(localUris, Media.TYPE_VIDEO) {
                        viewModel.liveDiary.value = viewModel.liveDiary.value!!.apply {
                            medias.addAll(it)
                        }
                    }
                }
            }
        }
    }

    override fun onFragmentResult(fragmentType: Int, result: Any?) {
        when (fragmentType) {
            BaseView.FRAGMENT_EDIT_MEDIA -> {
                zaloFragmentManager.removeCameraFragment()
                zaloFragmentManager.removeEditMediaFragment()

                viewModel.liveDiary.value = viewModel.liveDiary.value!!.apply {
                    medias.add(result as Media)
                }
            }
            BaseView.FRAGMENT_CAMERA -> {
                zaloFragmentManager.removeCameraFragment()
            }
        }
    }
}