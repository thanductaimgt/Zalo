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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_group.*
import kotlinx.android.synthetic.main.bottom_sheet_upload_picture.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.adapters.CreateGroupActivityRecyclerViewAdapter
import vng.zalo.tdtai.zalo.adapters.CreateGroupActivityViewPagerAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomItem
import vng.zalo.tdtai.zalo.models.RoomMember
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.RoomItemDiffCallback
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.viewmodels.CreateGroupActivityViewModel
import java.util.*

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewPagerAdapter: CreateGroupActivityViewPagerAdapter
    private lateinit var recyclerViewAdapter: CreateGroupActivityRecyclerViewAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var curAvatarLocalUriString: String? = null
    private var prevAvatarLocalUriString: String? = null
    lateinit var viewModel: CreateGroupActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(CreateGroupActivityViewModel::class.java)
        viewModel.liveSelectedRoomItems.observe(this, Observer {
            recyclerViewAdapter.submitList(it)

            countTextView.text = String.format(getString(R.string.description_selected_count), it.size)
            if (it.isEmpty()) {
                selectedListLayout.visibility = View.GONE
            } else {
                selectedListLayout.visibility = View.VISIBLE
            }
        })
    }

    private fun initView() {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        viewPagerAdapter = CreateGroupActivityViewPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)

        recyclerViewAdapter = CreateGroupActivityRecyclerViewAdapter(RoomItemDiffCallback())
        recyclerView.adapter = recyclerViewAdapter

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
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_upload_picture, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.titleTextView.text = getString(R.string.label_update_avatar)
        sheetView.choosePicTextView.setOnClickListener(this)
        sheetView.takePicTextView.setOnClickListener(this)
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
                val itemPosition = recyclerView.getChildLayoutPosition(view)
                val item = recyclerViewAdapter.currentList[itemPosition]
                processNewClickOnItem(item)
            }
            R.id.uploadAvatarImgView -> bottomSheetDialog.show()
            R.id.choosePicTextView -> {
                Utils.dispatchChooserIntent(this, Constants.CHOOSE_IMAGES_REQUEST)
            }
            R.id.takePicTextView -> {
                Utils.dispatchTakePictureIntent(this) {
                    prevAvatarLocalUriString = curAvatarLocalUriString
                    curAvatarLocalUriString = it
                }
            }
            R.id.createGroupImgView -> {
                createGroupIfNeeded()
            }
            R.id.backImgView->{
                Utils.hideKeyboard(this, rootView)
                finish()
            }
        }
    }

    private fun createGroupIfNeeded() {
        if (viewModel.liveSelectedRoomItems.value!!.size == 1) {
            val curRoomItem = viewModel.liveSelectedRoomItems.value!![0]

            startActivity(
                    Intent(this, RoomActivity::class.java).apply {
                        putExtra(Constants.ROOM_NAME, curRoomItem.name)
                        putExtra(Constants.ROOM_AVATAR, curRoomItem.avatarUrl)
                        putExtra(Constants.ROOM_ID, curRoomItem.roomId)
                        putExtra(Constants.ROOM_TYPE, curRoomItem.roomType)
                    }
            )
        } else {
            val groupName = if (nameTextView.text.toString() != "") {
                nameTextView.text.toString()
            } else {
                viewModel.liveSelectedRoomItems.value!!.joinToString(transform = { roomItem -> roomItem.name!! })
            }

            val curTimestamp = Timestamp.now()

            val newRoom = Room(
                    name = groupName,
                    avatarUrl = curAvatarLocalUriString.toString(),
                    createdTime = Timestamp.now(),
                    memberMap = HashMap<String, RoomMember>().apply {
                        viewModel.liveSelectedRoomItems.value!!.forEach {
                            put(it.name!!, RoomMember(
                                    avatarUrl = it.avatarUrl,
                                    joinDate = curTimestamp,
                                    phone = it.name)
                            )
                        }
                        put(ZaloApplication.curUser!!.phone!!, RoomMember(avatarUrl = ZaloApplication.curUser!!.avatarUrl, joinDate = curTimestamp, phone = ZaloApplication.curUser!!.phone))
                    },
                    type = Room.TYPE_GROUP
            )


            viewModel.createRoomInFireStore(this, newRoom) {
                startActivity(Intent(this, RoomActivity::class.java).apply {
                    putExtra(Constants.ROOM_ID, it.roomId)
                    putExtra(Constants.ROOM_NAME, it.name)
                    putExtra(Constants.ROOM_AVATAR, it.avatarUrl)
                    putExtra(Constants.ROOM_TYPE, it.roomType)
                })
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.CHOOSE_IMAGES_REQUEST -> {
                    Utils.assertNotNull(intent, TAG, "CHOOSE_IMAGES_REQUEST.intent") { intentNotNull ->
                        curAvatarLocalUriString = intentNotNull.data!!.toString()
                        Picasso.get().load(intentNotNull.data).fit()
                                .centerInside().into(uploadAvatarImgView)
                    }
                }
                Constants.TAKE_PICTURE_REQUEST -> {
                    // delete previous image when taking new one
                    Utils.deleteZaloFileAtUri(this, prevAvatarLocalUriString)
                    Picasso.get().load("file://$curAvatarLocalUriString").fit()
                            .centerInside().into(uploadAvatarImgView)
                }
            }
            bottomSheetDialog.dismiss()
        } else {
            Log.d(TAG, "resultCode != Activity.RESULT_OK")
        }
    }
}