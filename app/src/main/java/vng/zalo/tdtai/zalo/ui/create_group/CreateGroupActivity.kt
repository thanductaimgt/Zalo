package vng.zalo.tdtai.zalo.ui.create_group

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.bottom_sheet_upload.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.common.ChooseRoomItemAdapter
import vng.zalo.tdtai.zalo.common.ProcessingDialog
import vng.zalo.tdtai.zalo.managers.ExternalIntentManager
import vng.zalo.tdtai.zalo.managers.ResourceManager
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.model.RoomMember
import vng.zalo.tdtai.zalo.model.room.RoomGroup
import vng.zalo.tdtai.zalo.model.room.RoomItem
import vng.zalo.tdtai.zalo.model.room.RoomItemPeer
import vng.zalo.tdtai.zalo.ui.chat.ChatActivity
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.utils.loadCompat
import java.util.*
import javax.inject.Inject

class CreateGroupActivity : DaggerAppCompatActivity(), View.OnClickListener {
    @Inject lateinit var externalIntentManager: ExternalIntentManager
    @Inject lateinit var utils: Utils
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var resourceManager: ResourceManager

    @Inject lateinit var processingDialog: ProcessingDialog

    @Inject lateinit var viewPagerAdapter: CreateGroupViewPagerAdapter
    @Inject lateinit var selectedRecyclerViewAdapter: ChooseRoomItemAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: CreateGroupViewModel by viewModels { viewModelFactory }

    private var isAvatarSet = false
    private var takenImageUrl:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initView()

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

    private fun initView() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)

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
        searchView.setOnClickListener(this)
        createGroupImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
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
            R.id.searchView -> {
                searchView.isIconified = false
            }
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
                utils.hideKeyboard(this, rootView)
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
                        putExtra(Constants.ROOM_PHONE, curRoomItem.phone)
                        putExtra(Constants.ROOM_AVATAR, curRoomItem.avatarUrl)
                        putExtra(Constants.ROOM_ID, curRoomItem.roomId)
                    }
            )
        } else {
            processingDialog.show(supportFragmentManager)

            val groupName = if (nameTextView.text.toString() != "") {
                nameTextView.text.toString()
            } else {
                viewModel.liveSelectedRoomItems.value!!.joinToString(transform = { roomItem -> (roomItem as RoomItemPeer).phone!! })
            }

            val curTimestamp = System.currentTimeMillis()

            val newRoom = RoomGroup(
                    name = groupName,
                    avatarUrl = viewModel.liveAvatarLocalUri.value,
                    createdTime = curTimestamp,
                    memberMap = HashMap<String, RoomMember>().apply {
                        viewModel.liveSelectedRoomItems.value!!.forEach {
                            it as RoomItemPeer
                            put(it.phone!!, RoomMember(
                                    avatarUrl = it.avatarUrl,
                                    joinDate = curTimestamp,
                                    phone = it.phone,
                                    name = it.name
                            ))
                        }

                        put(sessionManager.curUser!!.phone!!, RoomMember(
                                avatarUrl = sessionManager.curUser!!.avatarUrl,
                                joinDate = curTimestamp,
                                phone = sessionManager.curUser!!.phone,
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
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
        } else {
            Log.d(TAG, "resultCode != Activity.RESULT_OK")
        }
    }

    private fun updateRoomAvatar(localUri: String?) {
        Picasso.get().loadCompat(localUri, resourceManager).fit()
                .centerCrop().into(uploadAvatarImgView)

        if (!isAvatarSet) {
            uploadAvatarImgView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            isAvatarSet = true
        }
    }
}