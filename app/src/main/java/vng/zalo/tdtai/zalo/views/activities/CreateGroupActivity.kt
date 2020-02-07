package vng.zalo.tdtai.zalo.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.bottom_sheet_upload.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.ExternalIntentDispatcher
import vng.zalo.tdtai.zalo.adapters.ChoosenListRecyclerViewAdapter
import vng.zalo.tdtai.zalo.adapters.CreateGroupActivityViewPagerAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.RoomMember
import vng.zalo.tdtai.zalo.models.room.RoomGroup
import vng.zalo.tdtai.zalo.models.room.RoomItem
import vng.zalo.tdtai.zalo.models.room.RoomItemPeer
import vng.zalo.tdtai.zalo.utils.*
import vng.zalo.tdtai.zalo.viewmodels.CreateGroupActivityViewModel
import java.util.*

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewPagerAdapter: CreateGroupActivityViewPagerAdapter
    private lateinit var selectedRecyclerViewAdapter: ChoosenListRecyclerViewAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog
    lateinit var viewModel: CreateGroupActivityViewModel
    private var isAvatarSet = false

    private var takenImageUrl:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(CreateGroupActivityViewModel::class.java)

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

        viewPagerAdapter = CreateGroupActivityViewPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)

        selectedRecyclerViewAdapter = ChoosenListRecyclerViewAdapter(RoomItemDiffCallback())
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
                ExternalIntentDispatcher.dispatchChooserIntent(this, Constants.CHOOSE_IMAGE_REQUEST, ExternalIntentDispatcher.CHOOSER_TYPE_IMAGE, false)
            }
            R.id.captureTV -> {
                ExternalIntentDispatcher.dispatchCaptureIntent(this, Constants.CAPTURE_IMAGE_REQUEST, ExternalIntentDispatcher.CAPTURE_TYPE_IMAGE) {
                    takenImageUrl = it
                }
            }
            R.id.createGroupImgView -> {
                createGroupIfNeeded()
            }
            R.id.backImgView -> {
                Utils.hideKeyboard(this, rootView)
                finish()
            }
        }
    }

    private fun createGroupIfNeeded() {
        if (viewModel.liveSelectedRoomItems.value!!.size == 1) {
            val curRoomItem = viewModel.liveSelectedRoomItems.value!![0] as RoomItemPeer

            startActivity(
                    Intent(this, RoomActivity::class.java).apply {
                        putExtra(Constants.ROOM_NAME, curRoomItem.name)
                        putExtra(Constants.ROOM_PHONE, curRoomItem.phone)
                        putExtra(Constants.ROOM_AVATAR, curRoomItem.avatarUrl)
                        putExtra(Constants.ROOM_ID, curRoomItem.roomId)
                    }
            )
        } else {
            ZaloApplication.processingDialog.show(supportFragmentManager)

            val groupName = if (nameTextView.text.toString() != "") {
                nameTextView.text.toString()
            } else {
                viewModel.liveSelectedRoomItems.value!!.joinToString(transform = { roomItem -> (roomItem as RoomItemPeer).phone!! })
            }

            val curTimestamp = Timestamp.now()

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

                        put(ZaloApplication.curUser!!.phone!!, RoomMember(
                                avatarUrl = ZaloApplication.curUser!!.avatarUrl,
                                joinDate = curTimestamp,
                                phone = ZaloApplication.curUser!!.phone,
                                name = ZaloApplication.curUser!!.name))
                    }
            )

            viewModel.createRoomInFireStore(this, newRoom) {
                ZaloApplication.processingDialog.dismiss()

                startActivity(Intent(this, RoomActivity::class.java).apply {
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
                    Utils.assertNotNull(intent, TAG, "CHOOSE_IMAGES_REQUEST.intent") { intentNotNull ->
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
        Picasso.get().loadCompat(localUri).fit()
                .centerCrop().into(uploadAvatarImgView)

        if (!isAvatarSet) {
            uploadAvatarImgView.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            isAvatarSet = true
        }
    }
}