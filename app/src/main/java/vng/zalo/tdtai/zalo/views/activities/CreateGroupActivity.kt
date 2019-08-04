package vng.zalo.tdtai.zalo.views.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_create_group.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.RoomItem
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.FrameLayout
import androidx.core.content.FileProvider
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_create_group.recyclerView
import kotlinx.android.synthetic.main.navi_bot_activity_create_group.view.*
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.adapters.CreateGroupActivityRecyclerViewAdapter
import vng.zalo.tdtai.zalo.adapters.CreateGroupActivityViewPagerAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.Room
import vng.zalo.tdtai.zalo.models.RoomMember
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.viewmodels.CreateGroupActivityViewModel
import java.io.File
import java.io.IOException
import java.util.*
import vng.zalo.tdtai.zalo.utils.TAG

class CreateGroupActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewPagerAdapter: CreateGroupActivityViewPagerAdapter
    private lateinit var recyclerViewAdapter: CreateGroupActivityRecyclerViewAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var currentPhotoUri: String? = null
    lateinit var viewModel: CreateGroupActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance()).get(CreateGroupActivityViewModel::class.java)
        viewModel.liveRoomItems.observe(this, Observer {
            recyclerViewAdapter.roomItems = it
            recyclerViewAdapter.notifyDataSetChanged()

            selectedListLayout.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
        })
    }

    private fun initView() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewPagerAdapter = CreateGroupActivityViewPagerAdapter(this, supportFragmentManager)
        viewPager.adapter = viewPagerAdapter

        tabLayout.setupWithViewPager(viewPager)

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

        // Fix BottomSheetDialog not showing after getting hidden when the user drags it down
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet).apply{
                skipCollapsed = true
            }
        }

        uploadAvatarImgView.setOnClickListener(this)
        searchView.setOnClickListener(this)
        createGroupButton.setOnClickListener(this)
    }

    fun proceedNewClickOnItem(item: RoomItem) {
        viewModel.liveRoomItems.value = viewModel.liveRoomItems.value!!.apply {
            if (contains(item)) {
                remove(item)
            } else {
                add(item)
            }
        }
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
                val sheetView = layoutInflater.inflate(R.layout.navi_bot_activity_create_group, null)
                bottomSheetDialog.setContentView(sheetView)
                sheetView.choosePicTextView.setOnClickListener(this)
                sheetView.takePicTextView.setOnClickListener(this)

                bottomSheetDialog.show()
            }
            R.id.choosePicTextView -> {
                dispatchChoosePictureIntent()
            }
            R.id.takePicTextView -> {
                dispatchTakePictureIntent()
            }
            R.id.createGroupButton -> {
                createGroupIfNeeded()
            }
        }
    }

    private fun createGroupIfNeeded() {
        if (viewModel.liveRoomItems.value!!.size == 1) {
            val curRoomItem = viewModel.liveRoomItems.value!![0]

            startActivity(
                    Intent(this, RoomActivity::class.java).apply {
                        putExtra(Constants.ROOM_NAME, curRoomItem.name)
                        putExtra(Constants.ROOM_AVATAR, curRoomItem.avatarUrl)
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
                    avatarUrl = currentPhotoUri,
                    createdTime = Timestamp.now(),
                    memberMap = HashMap<String, RoomMember>().apply {
                        viewModel.liveRoomItems.value!!.forEach {
                            put(it.name!!, RoomMember(
                                    avatarUrl = it.avatarUrl,
                                    joinDate = curTimestamp,
                                    phone = it.name)
                            )
                        }
                        put(ZaloApplication.currentUser!!.phone!!, RoomMember(avatarUrl = ZaloApplication.currentUser!!.avatarUrl, joinDate = curTimestamp, phone = ZaloApplication.currentUser!!.phone))
                    }
            )


            viewModel.createRoomInFireStore(newRoom) {
                startActivity(Intent(this, RoomActivity::class.java).apply {
                    putExtra(Constants.ROOM_ID, it.roomId)
                    putExtra(Constants.ROOM_NAME, it.name)
                    putExtra(Constants.ROOM_AVATAR, it.avatarUrl)
                })
            }
        }
    }

    private fun dispatchChoosePictureIntent() {
        val getIntent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }

        val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply { type = "image/*" }

        val chooserIntent = Intent.createChooser(getIntent, getString(R.string.label_choose_image_from)).apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickIntent))
        }

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
                    val photoUri: Uri = FileProvider.getUriForFile(
                            this,
                            "${applicationContext.packageName}.${Constants.PROVIDER_AUTHORITY}",
                            it
                    )
                    takePictureIntent.apply {
                        putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    packageManager.queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY).apply {
                        forEach {resolveInfo->
                            val packageName = resolveInfo.activityInfo.packageName
                            grantUriPermission(packageName, photoUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                    }

                    startActivityForResult(takePictureIntent, Constants.TAKE_PICTURE)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Utils.assertNotNull(data, TAG, "onActivityResult.data") { dataNotNull ->
                when (requestCode) {
                    Constants.PICK_IMAGE -> {
                        //TODO("rxjava here")
                        deleteZaloFileAtUri(currentPhotoUri)
                        currentPhotoUri = Utils.cloneFileToPictureDir(this, dataNotNull.data!!)
                        Picasso.get().load(File(currentPhotoUri!!)).fit().into(uploadAvatarImgView)
                    }
                    Constants.TAKE_PICTURE -> {
                        Picasso.get().load(File(currentPhotoUri!!)).fit().into(uploadAvatarImgView)
                    }
                }
                bottomSheetDialog.dismiss()
            }
        } else {
            Log.d(TAG, "resultCode != Activity.RESULT_OK")
        }
    }

    private fun createImageFile(): File {
        // Create an image file name
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        storageDir ?: Log.e(TAG, "storageDir is null")
        return File.createTempFile(
                "${Constants.FILE_PREFIX}${Date().time}", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            // delete previous image when taking new one
            deleteZaloFileAtUri(currentPhotoUri)
            currentPhotoUri = absolutePath
        }
    }

    private fun deleteZaloFileAtUri(uriString:String?){
        uriString?.let {
            if(Utils.parseFileName(uriString).startsWith(Constants.FILE_PREFIX)){
                File(uriString).delete()
            }
        }
    }
}