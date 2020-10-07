package com.mgt.zalo.ui.create_group

import android.content.Intent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity
import com.mgt.zalo.common.ChooseRoomItemAdapter
import com.mgt.zalo.data_model.RoomMember
import com.mgt.zalo.data_model.room.RoomGroup
import com.mgt.zalo.data_model.room.RoomItem
import com.mgt.zalo.data_model.room.RoomItemPeer
import com.mgt.zalo.manager.ExternalIntentManager
import com.mgt.zalo.ui.chat.ChatActivity
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.TAG
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.bottom_sheet_upload.view.*
import java.util.*
import javax.inject.Inject


class CreateGroupActivity : BaseActivity() {
    @Inject
    lateinit var pagerAdapter: CreateGroupPagerAdapter

    @Inject
    lateinit var selectedRecyclerViewAdapter: ChooseRoomItemAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private val viewModel: CreateGroupViewModel by viewModels { viewModelFactory }

    private var isAvatarSet = false
    private var takenImageUrl: String? = null

    override fun onBindViews() {
        setContentView(R.layout.activity_create_group)

        viewPager.adapter = pagerAdapter

//        tabLayout.setupWithViewPager(viewPager)
        TabLayoutMediator(tabLayout, viewPager,
                TabConfigurationStrategy { tab: TabLayout.Tab, position: Int -> tab.text = pagerAdapter.getTabTitle(position) }
        ).attach()

        selectedRecyclerView.adapter = selectedRecyclerViewAdapter

        nameTextView.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    false
                }
                else -> true
            }
        }

        initBottomSheet()

        uploadAvatarImgView.setOnClickListener(this)
        createGroupImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
    }

    override fun onViewsBound() {
        viewModel.liveSelectedRoomItems.observe(this, Observer {
            val oldSize = selectedRecyclerViewAdapter.currentList.size
            selectedRecyclerViewAdapter.submitList(it) {
                if (it.size > oldSize) {
                    selectedRecyclerView.scrollToPosition(selectedRecyclerViewAdapter.itemCount - 1)
                }
            }

            countTextView.text = String.format(getString(R.string.description_selected_count), it.size)
            if (it.isEmpty()) {
                selectedListLayout.visibility = View.GONE
            } else {
                selectedListLayout.visibility = View.VISIBLE
            }
        })

        viewModel.liveAvatarLocalUri.observe(this, Observer {
            updateRoomAvatar(it)
        })
    }

    private fun initBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)

        // Fix BottomSheetDialog not showing after getting hidden when the user drags it down
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(bottomSheet).apply {
                skipCollapsed = true
            }
        }
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_upload, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.titleTextView.text = getString(R.string.label_upload_image)
        sheetView.chooseFromGalleryTV.setOnClickListener(this)
        sheetView.captureTV.setOnClickListener(this)
    }

    private fun processNewClickOnItem(item: RoomItem) {
        viewModel.liveSelectedRoomItems.value = viewModel.liveSelectedRoomItems.value!!.apply {
            if (contains(item)) {
                remove(item)
            } else {
                add(item)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.itemUserIconRootLayout -> {
                val itemPosition = selectedRecyclerView.getChildAdapterPosition(view)
                val item = selectedRecyclerViewAdapter.currentList[itemPosition]
                processNewClickOnItem(item)
            }
            R.id.uploadAvatarImgView -> bottomSheetDialog.show()
            R.id.chooseFromGalleryTV -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_IMAGE_REQUEST, ExternalIntentManager.CHOOSER_TYPE_IMAGE, false)
            }
            R.id.captureTV -> {
                externalIntentManager.dispatchCaptureIntent(this, Constants.CAPTURE_IMAGE_REQUEST, ExternalIntentManager.CAPTURE_TYPE_IMAGE) {
                    takenImageUrl = it
                }
            }
            R.id.createGroupImgView -> {
                createGroupIfNeeded()
            }
            R.id.backImgView -> {
                utils.hideKeyboard(rootView)
                finish()
            }
        }
    }

    private fun createGroupIfNeeded() {
        if (viewModel.liveSelectedRoomItems.value!!.size == 1) {
            val curRoomItem = viewModel.liveSelectedRoomItems.value!![0] as RoomItemPeer

            startActivity(
                    Intent(this, ChatActivity::class.java).apply {
                        putExtra(Constants.ROOM_NAME, curRoomItem.name)
                        putExtra(Constants.ROOM_PHONE, curRoomItem.peerId)
                        putExtra(Constants.ROOM_AVATAR, curRoomItem.avatarUrl)
                        putExtra(Constants.ROOM_ID, curRoomItem.roomId)
                    }
            )
        } else {
            processingDialog.show(supportFragmentManager)

            val groupName = if (nameTextView.text.toString() != "") {
                nameTextView.text.toString()
            } else {
                viewModel.liveSelectedRoomItems.value!!.joinToString(transform = { roomItem -> (roomItem as RoomItemPeer).peerId!! })
            }

            val curTimestamp = System.currentTimeMillis()

            val newRoom = RoomGroup(
                    name = groupName,
                    avatarUrl = viewModel.liveAvatarLocalUri.value,
                    createdTime = curTimestamp,
                    memberMap = HashMap<String, RoomMember>().apply {
                        viewModel.liveSelectedRoomItems.value!!.forEach {
                            it as RoomItemPeer
                            put(it.peerId!!, RoomMember(
                                    avatarUrl = it.avatarUrl,
                                    joinDate = curTimestamp,
                                    userId = it.peerId,
                                    name = it.name
                            ))
                        }

                        put(sessionManager.curUser!!.id!!, RoomMember(
                                avatarUrl = sessionManager.curUser!!.avatarUrl,
                                joinDate = curTimestamp,
                                userId = sessionManager.curUser!!.id,
                                name = sessionManager.curUser!!.name))
                    }
            )

            viewModel.createRoomInFireStore(newRoom) {
                processingDialog.dismiss()

                startActivity(Intent(this, ChatActivity::class.java).apply {
                    putExtra(Constants.ROOM_ID, it.roomId)
                    putExtra(Constants.ROOM_NAME, it.name)
                    putExtra(Constants.ROOM_AVATAR, it.avatarUrl)
                })
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, intent: Intent?) {
        when (requestCode) {
            Constants.CHOOSE_IMAGE_REQUEST -> {
                utils.assertNotNull(intent, TAG, "CHOOSE_IMAGES_REQUEST.intent") { intentNotNull ->
                    viewModel.liveAvatarLocalUri.value = intentNotNull.data!!.toString()
                }
            }
            Constants.CAPTURE_IMAGE_REQUEST -> {
                viewModel.liveAvatarLocalUri.value = takenImageUrl
            }
        }
        bottomSheetDialog.dismiss()
    }

    private fun updateRoomAvatar(localUri: String?) {
        imageLoader.load(localUri, uploadAvatarImgView) {
            it.fit()
                    .centerCrop()
        }

        if (!isAvatarSet) {
            uploadAvatarImgView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            isAvatarSet = true
        }
    }
}