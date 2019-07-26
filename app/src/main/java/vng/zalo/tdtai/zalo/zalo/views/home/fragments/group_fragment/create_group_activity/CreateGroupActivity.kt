package vng.zalo.tdtai.zalo.zalo.views.home.fragments.group_fragment.create_group_activity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_create_group.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.models.RoomItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.navi_bot_activity_create_group.view.*
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.dependency_factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.models.Room
import vng.zalo.tdtai.zalo.zalo.models.RoomMember
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import vng.zalo.tdtai.zalo.zalo.viewmodels.CreateGroupActivityViewModel
import vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity.RoomActivity
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener, TabLayout.OnTabSelectedListener {
    private lateinit var viewPagerAdapter: CreateGroupActivityViewPagerAdapter
    private lateinit var recyclerViewAdapter: CreateGroupActivityRecyclerViewAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var currentPhotoPath: String? = null
    lateinit var viewModel: CreateGroupActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initView()

        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance()).get(CreateGroupActivityViewModel::class.java)
        viewModel.liveRoomItems.observe(this, Observer {
            Log.d(Utils.getTag("CreateGroupActivity:onCreate"), it.toString())
            recyclerViewAdapter.roomItems = it
            recyclerViewAdapter.notifyDataSetChanged()

            if (it.isEmpty()) {
                selectedListLayout.visibility = View.GONE
            } else {
                selectedListLayout.visibility = View.VISIBLE
            }
        })
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewPagerAdapter = CreateGroupActivityViewPagerAdapter(supportFragmentManager)
        viewPager.apply {
            adapter = viewPagerAdapter
            addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        }

        tabLayout.addOnTabSelectedListener(this)

        recyclerViewAdapter = CreateGroupActivityRecyclerViewAdapter(this)
        recyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = LinearLayoutManager(this@CreateGroupActivity, LinearLayoutManager.HORIZONTAL, false)
        }

        nameTextView.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_DONE -> {
                    false
                }
                else -> true
            }
        }

        bottomSheetDialog = BottomSheetDialog(this)
        val sheetView = this.layoutInflater.inflate(R.layout.navi_bot_activity_create_group, null)
        bottomSheetDialog.setContentView(sheetView)
        sheetView.choosePicTextView.setOnClickListener(this)
        sheetView.takePicTextView.setOnClickListener(this)

        uploadAvatarImgView.setOnClickListener(this)
        searchView.setOnClickListener(this)
        createGroupButton.setOnClickListener(this)
    }

    fun proceedNewClickOnItem(item: RoomItem) {
        viewModel.liveRoomItems.value!!.apply {
            if (contains(item)) {
                remove(item)
            } else {
                add(item)
            }
            viewModel.liveRoomItems.value = viewModel.liveRoomItems.value
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab) {
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        viewPager.currentItem = tab.position
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.searchView -> {
                searchView.isIconified = false
                searchViewDescTV.visibility = View.GONE
            }
            R.id.itemUserIconRootLayout -> {
                val itemPosition = recyclerView.getChildLayoutPosition(view)
                val item = recyclerViewAdapter.roomItems[itemPosition]
                proceedNewClickOnItem(item)
            }
            R.id.uploadAvatarImgView -> {
                Log.d(Utils.getTag(object {}), "uploadAvatarImgView clicked")

                bottomSheetDialog.show()
            }
            R.id.choosePicTextView -> {
                dispatchChoosePictureIntent()
            }
            R.id.takePicTextView -> {
                Log.d(Utils.getTag(object {}), "takeNewPicTextView clicked")
                dispatchTakePictureIntent()
            }
            R.id.createGroupButton -> {
                if (viewModel.liveRoomItems.value!!.size == 1) {
                    val curRoomItem = viewModel.liveRoomItems.value!![0]

                    startActivity(
                            Intent(this, RoomActivity::class.java).apply {
                                putExtra(Constants.ROOM_NAME, curRoomItem.name)
                                putExtra(Constants.ROOM_AVATAR, curRoomItem.avatar)
                                putExtra(Constants.ROOM_ID, curRoomItem.roomId)
                            }
                    )
                } else {
                    val groupName = if (nameTextView.text.toString() != "") {
                        nameTextView.text.toString()
                    } else {
                        viewModel.liveRoomItems.value!!.joinToString(transform = { roomItem -> roomItem.name!! })
                    }

                    val curTimestamp = Timestamp.now()

                    val newRoom = Room(
                            name = groupName,
                            avatar = currentPhotoPath,
                            createdTime = Timestamp.now(),
                            memberMap = HashMap<String, RoomMember>().apply {
                                viewModel.liveRoomItems.value!!.forEach {
                                    put(it.name!!, RoomMember(
                                            avatar = it.avatar,
                                            joinDate = curTimestamp,
                                            phone = it.name)
                                    )
                                }
                                put(ZaloApplication.currentUser!!.phone!!, RoomMember(avatar = ZaloApplication.currentUser!!.avatar, joinDate = curTimestamp, phone = ZaloApplication.currentUser!!.phone))
                            }
                    )
                    Log.d(Utils.getTag(object {}),newRoom.memberMap.toString())

                    viewModel.createRoomInFireStore(newRoom) {
                        startActivity(Intent(this, RoomActivity::class.java).apply {
                            putExtra(Constants.ROOM_ID, it.roomId)
                            putExtra(Constants.ROOM_NAME, it.name)
                            putExtra(Constants.ROOM_AVATAR, it.avatar)
                        })
                    }
                }
            }
        }
    }

    private fun dispatchChoosePictureIntent() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT)
        getIntent.type = "image/*"

        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickIntent.type = "image/*"

        val chooserIntent = Intent.createChooser(getIntent, getString(R.string.label_choose_image_from))
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))

        startActivityForResult(chooserIntent, Constants.PICK_IMAGE)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                            this,
                            "vng.zalo.tdtai.zalo.fileprovider",
                            it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, Constants.TAKE_PICTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                Constants.PICK_IMAGE -> Picasso.get().load(data.data).fit().into(uploadAvatarImgView)
                Constants.TAKE_PICTURE -> {
                    Log.d(Utils.getTag(object {}), currentPhotoPath)
                    Picasso.get().load(File(currentPhotoPath)).fit().into(uploadAvatarImgView)
                    Log.d(Utils.getTag(object {}), currentPhotoPath)
                }
            }
            bottomSheetDialog.hide()
        } else if (resultCode != Activity.RESULT_OK) {
            Log.d(Utils.getTag(object {}), "resultCode != Activity.RESULT_OK")
        } else {
            Log.d(Utils.getTag(object {}), "data == null")
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()).replace('/', '_').replace(' ', '_')
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        storageDir ?: Log.e(Utils.getTag(object {}), "storageDir is null")
        return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }
    }
}