package com.mgt.zalo.ui.story

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.dialog_create_story_group.*
import kotlinx.android.synthetic.main.dialog_create_story_group.addButton
import kotlinx.android.synthetic.main.dialog_create_story_group.nameEditText
import kotlinx.android.synthetic.main.dialog_create_story_group.view.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseDialog
import com.mgt.zalo.manager.ResourceManager
import com.mgt.zalo.data_model.story.ImageStory
import com.mgt.zalo.data_model.story.Story
import com.mgt.zalo.data_model.story.VideoStory
import com.mgt.zalo.util.TAG
import com.mgt.zalo.util.Utils
import com.mgt.zalo.util.smartLoad
import javax.inject.Inject

class CreateStoryGroupDialog @Inject constructor(
        private val storyFragment: StoryFragment,
        private val resourceManager: ResourceManager,
        private val utils: Utils
) : BaseDialog() {
    private lateinit var story: Story

    override fun onClick(view: View) {
        when (view.id) {
            R.id.addButton -> {
                storyFragment.addStoryToNewGroup(story, nameEditText.text.toString())
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_create_story_group, container, false)
    }

    private lateinit var saveText:String

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onResume() {
        super.onResume()
        view!!.nameEditText.setText(saveText)
    }

    override fun onPause() {
        super.onPause()
        saveText = view!!.nameEditText.text.toString()
    }

    override fun onBindViews() {
        super.onBindViews()

        saveText = ""

        when (story) {
            is ImageStory -> {
                Picasso.get().smartLoad((story as ImageStory).imageUrl, resourceManager, avatarImgView) {
                    it.fit().centerCrop()
                }
            }
            is VideoStory -> {
                resourceManager.getVideoThumbUri((story as VideoStory).videoUrl!!) { uri ->
                    Picasso.get().smartLoad(uri, resourceManager, avatarImgView) {
                        it.fit().centerCrop()
                    }
                }
            }
        }

        nameEditText.apply {
            addTextChangedListener {
                view?.addButton?.isEnabled = nameEditText.text != null && nameEditText.text.toString() != ""
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    utils.showKeyboard(nameEditText)
                } else {
                    utils.hideKeyboard(view!!)
                }
            }
            requestFocus()
        }

        addButton.setOnClickListener(this)
    }

    fun show(fm: FragmentManager, story: Story) {
        this.story = story
        show(fm, TAG)
    }
}