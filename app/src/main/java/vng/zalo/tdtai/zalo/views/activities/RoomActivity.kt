package vng.zalo.tdtai.zalo.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.bottom_sheet_message_actions.view.*
import kotlinx.android.synthetic.main.bottom_sheet_upload_picture.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.adapters.MessageActionAdapter
import vng.zalo.tdtai.zalo.adapters.RoomActivityAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.utils.*
import vng.zalo.tdtai.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.views.fragments.EmojiFragment
import vng.zalo.tdtai.zalo.views.fragments.ZoomImageDialog


class RoomActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private lateinit var viewModel: RoomActivityViewModel
    private lateinit var adapter: RoomActivityAdapter
    private var emojiFragment: EmojiFragment? = null
    private var isMessageJustSentByMe = false
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private val zoomImageDialog = ZoomImageDialog(supportFragmentManager)
    private var imageLocalUriString: String? = null
    private lateinit var chooseImagesView: View
    private lateinit var messageActionsView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

        // remove all room's chat notifications when enter
        NotificationManagerCompat.from(this).cancel(intent.getStringExtra(Constants.ROOM_ID).hashCode())

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(intent = intent)).get(RoomActivityViewModel::class.java)
        viewModel.liveMessages.observe(this, Observer { messages ->
            val typings = viewModel.getTypingMessages(viewModel.liveTypingMembersPhone.value!!)
            val messagesWithTypings = ArrayList<Message>().apply {
                addAll(typings)
                addAll(messages)
            }

            submitMessages(messagesWithTypings)
        })
        viewModel.liveTypingMembersPhone.observe(this, Observer { phones ->
            val typings = viewModel.getTypingMessages(phones)
            val messages = viewModel.liveMessages.value!!
            val messagesWithTypings = ArrayList<Message>().apply {
                addAll(typings)
                addAll(messages)
            }

            submitMessages(messagesWithTypings)
        })
    }

    override fun onResume() {
        super.onResume()
        // stop receiving chat notifications of this room when in active state
        ZaloApplication.currentRoomId = intent.getStringExtra(Constants.ROOM_ID)
    }

    override fun onPause() {
        super.onPause()
        // start receiving chat notifications of this room when in inactive state,
        ZaloApplication.currentRoomId = null
        // remove from typing list if inactive
        viewModel.removeCurUserFromCurRoomTypingMembers()
    }

    private fun initView() {
        setContentView(R.layout.activity_room)

        roomNameTextView.text = intent.getStringExtra(Constants.ROOM_NAME)
        onlineStateTextView.text = String.format(getString(R.string.description_online_at), 7, getString(R.string.label_hour))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        msgEditText.apply {
            addTextChangedListener(InputTextListener())
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_SEND -> {
                        if (msgEditText.text != null && msgEditText.text.toString() != "") {
                            val content = msgEditText.text.toString()
                            sendMessages(ArrayList<String>().apply { add(content) }, Message.TYPE_TEXT)
                            msgEditText.setText("")
                        }
                        true
                    }
                    else -> false
                }
            }
            if (intent.getBooleanExtra(Constants.SHOW_KEYBOARD, false)) {
                requestFocus()
            }
        }

        backImgView.setOnClickListener(this)
        voiceCallImgView.setOnClickListener(this)
        videoCallImgView.setOnClickListener(this)
        moreImgView.setOnClickListener(this)
        msgEditText.setOnClickListener(this)
        sendMsgImgView.setOnClickListener(this)
        emojiImgView.setOnClickListener(this)
        uploadFileImgView.setOnClickListener(this)
        uploadPictureImgView.setOnClickListener(this)

        adapter = RoomActivityAdapter(this, MessageDiffCallback())
        with(recyclerView) {
            adapter = this@RoomActivity.adapter
            layoutManager = LinearLayoutManager(this@RoomActivity).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            addOnScrollListener(ScrollListener())
            setItemViewCacheSize(50)
        }

        msgEditText.setOnFocusChangeListener { _, b ->
            if (b) {
                frameLayout.visibility = View.GONE
                Utils.showKeyboard(this, msgEditText)
            } else {
                Utils.hideKeyboard(this, rootView)
            }
        }

        initBottomSheet()
        initChooseImagesView()
        initMessageActionsView()
//        // observe when keyboard is on or off
//        rootView.viewTreeObserver.addOnGlobalLayoutListener {
//            val heightDiff = rootView.rootView.height - rootView.height
//            if (heightDiff > Utils.dpToPx(this@RoomActivity, 200)) { // if more than 200 dp, it's probably a keyboard...
////                keyboardSize = Utils.pxToDp(this@RoomActivity, heightDiff.toFloat())
////                Log.d(TAG, "addOnGlobalLayoutListener.keyboardSize: $keyboardSize")
//                frameLayout.visibility = View.GONE
//            }
//        }
    }

    private fun initBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)

        // Fix BottomSheetDialog not showing after getting hidden when the user drags it down
        bottomSheetDialog.setOnShowListener { dialogInterface ->
            val bottomSheetDialog = dialogInterface as BottomSheetDialog
            val bottomSheet = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)
            BottomSheetBehavior.from(bottomSheet).apply {
                skipCollapsed = true
            }
        }
    }

    private fun initChooseImagesView() {
        chooseImagesView = layoutInflater.inflate(R.layout.bottom_sheet_upload_picture, null)
        chooseImagesView.titleTextView.text = getString(R.string.label_send_image)
        chooseImagesView.choosePicTextView.setOnClickListener(this)
        chooseImagesView.takePicTextView.setOnClickListener(this)
    }

    private fun initMessageActionsView() {
        messageActionsView = layoutInflater.inflate(R.layout.bottom_sheet_message_actions, null)
        messageActionsView.cancelImgView.setOnClickListener(this)

        val actions = ArrayList<Pair<Int, Int>>()
        actions.apply {
            add(Pair(R.drawable.reply, R.string.label_reply))
            add(Pair(R.drawable.copy, R.string.label_copy))
            add(Pair(R.drawable.share, R.string.label_share))
            add(Pair(R.drawable.revoke, R.string.label_revoke))
            add(Pair(R.drawable.download3, R.string.label_download))
            add(Pair(R.drawable.remind, R.string.label_remind))
            add(Pair(R.drawable.detail, R.string.label_detail))
            add(Pair(R.drawable.delete, R.string.label_delete))
        }

        messageActionsView.recyclerView.apply {
            layoutManager = GridLayoutManager(this@RoomActivity, 4)
            val spanCount = 4
            val spacing = 50; // 50px
            val includeEdge = true;
            recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
            adapter = MessageActionAdapter().apply {
                this.actions = actions
                notifyDataSetChanged()
            }
        }
    }

    @Synchronized
    private fun submitMessages(messages: List<Message>) {
        val newMessages = if (adapter.currentList === messages) ArrayList<Message>().apply { addAll(messages) } else messages
        adapter.submitList(newMessages) {
            val isLastMessageCompletelyVisible = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() <= 1
            if (isLastMessageCompletelyVisible || isMessageJustSentByMe) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
                isMessageJustSentByMe = false
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_room_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_call -> true
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.voiceCallImgView -> {
                startActivity(Intent(this, CallActivity::class.java).apply {
                    putExtra(Constants.IS_CALLER, true)

                    putExtra(Constants.ROOM_ID, viewModel.room.id)
                    putExtra(Constants.ROOM_NAME, viewModel.room.name)
                    putExtra(Constants.ROOM_AVATAR, viewModel.room.avatarUrl)
                })
            }
            R.id.sendMsgImgView -> {
                if (msgEditText.text != null && msgEditText.text.toString() != "") {
                    val content = msgEditText.text.toString()
                    sendMessages(ArrayList<String>().apply { add(content) }, Message.TYPE_TEXT)
                    msgEditText.setText("")
                }
            }
            R.id.emojiImgView -> {
                Utils.hideKeyboard(this@RoomActivity, rootView)
                showOrHideEmojiFragment()
            }
            R.id.uploadPictureImgView -> {
                if (chooseImagesView.parent != null) {
                    (chooseImagesView.parent as ViewGroup).removeView(chooseImagesView)
                }
                bottomSheetDialog.setContentView(chooseImagesView)
                bottomSheetDialog.show()
            }
            R.id.choosePicTextView -> {
                Utils.dispatchChooserIntent(this, Constants.CHOOSE_IMAGES_REQUEST)
            }
            R.id.takePicTextView -> {
                Utils.dispatchTakePictureIntent(this) {
                    imageLocalUriString = it
                }
            }
            R.id.imageView -> {
                val position = recyclerView.getChildLayoutPosition(v.parent as View)
                val message = adapter.currentList[position]
                zoomImageDialog.show(message)
            }
            R.id.uploadFileImgView -> {
                Utils.dispatchChooserIntent(this, Constants.CHOOSE_FILES_REQUEST)
            }
            R.id.backImgView -> {
                finish()
            }
            R.id.rootActionView -> {
                messageActionsView.recyclerView.apply {
                    val position = getChildLayoutPosition(v)

                    when ((this.adapter as MessageActionAdapter).actions[position].first) {
                        R.drawable.delete -> {
                            Toast.makeText(this@RoomActivity, "delete", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            R.id.cancelImgView -> {
                bottomSheetDialog.dismiss()
            }
        }
    }

    override fun onLongClick(v: View?): Boolean {
        when (v!!.id) {
            R.id.rootItemView -> {
                val position = recyclerView.getChildLayoutPosition(v)
                val message = adapter.currentList[position]

                messageActionsView.apply {
                    nameTextView.text = message.senderPhone
                    descTextView.text = message.getPreviewContent(this@RoomActivity)
                    when (message.type) {
                        Message.TYPE_TEXT -> imageView.visibility = View.GONE
                        Message.TYPE_FILE -> {
                            imageView.visibility = View.VISIBLE
                            Picasso.get().load(
                                    Utils.getResIdFromFileExtension(this@RoomActivity,
                                            Utils.getFileExtensionFromFileName(message.fileName!!)))
                                    .into(imageView)
                        }
                        Message.TYPE_IMAGE -> {
                            imageView.visibility = View.VISIBLE
                            Picasso.get().load(
                                    message.content
                            ).fit().centerCrop().into(imageView)
                        }
                        Message.TYPE_STICKER -> {
                            imageView.visibility = View.VISIBLE
                            imageView.setAnimationFromUrl(message.content)
                        }
                    }

                    if (parent != null) {
                        (parent as ViewGroup).removeView(this)
                    }
                }

                bottomSheetDialog.setContentView(messageActionsView)
                bottomSheetDialog.show()
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            Utils.assertNotNull(intent, TAG, "onActivityResult.intent") { intentNotNull ->
                when (requestCode) {
                    Constants.CHOOSE_IMAGES_REQUEST -> {
                        val localUris = getLocalUris(intentNotNull)

                        sendMessages(localUris, Message.TYPE_IMAGE)
                    }
                    Constants.CHOOSE_FILES_REQUEST -> {
                        val localUris = getLocalUris(intentNotNull)

                        sendMessages(localUris, Message.TYPE_FILE)
                    }
                    Constants.TAKE_PICTURE_REQUEST -> {
                        imageLocalUriString?.let { localUri ->
                            sendMessages(ArrayList<String>().apply { add(localUri) }, Message.TYPE_IMAGE)
                        }
                    }
                }
                bottomSheetDialog.dismiss()
            }
        } else {
            Log.d(TAG, "resultCode != Activity.RESULT_OK")
        }
    }

    private fun getLocalUris(intent: Intent): List<String> {
        val localUris = ArrayList<String>()
        if (null != intent.clipData) {
            for (i in 0 until intent.clipData!!.itemCount) {
                val uri = intent.clipData!!.getItemAt(i).uri.toString()
                localUris.add(uri)
            }
        } else {
            val uri = intent.data!!.toString()
            localUris.add(uri)
        }
        return localUris
    }

    fun sendMessages(contents: List<String>, type: Int) {
        viewModel.addNewMessagesToFirestore(this, contents, type)
        isMessageJustSentByMe = true
    }

    private fun showOrHideEmojiFragment() {
        if (frameLayout.visibility == View.GONE) {
            if (emojiFragment == null) {
                emojiFragment = EmojiFragment()

                supportFragmentManager
                        .beginTransaction()
                        .add(R.id.frameLayout, emojiFragment!!)
                        .commit()
            }
            frameLayout.visibility = View.VISIBLE
        } else {
            frameLayout.visibility = View.GONE
        }
    }

    override fun onBackPressed() {
        if (frameLayout.visibility != View.GONE) {
            frameLayout.visibility = View.GONE
        } else {
            super.onBackPressed()
        }
    }

    internal inner class InputTextListener : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isEmpty()) {
                sendMsgImgView.visibility = View.GONE
                uploadFileImgView.visibility = View.VISIBLE
                uploadVoiceImgView.visibility = View.VISIBLE
                uploadPictureImgView.visibility = View.VISIBLE

                viewModel.removeCurUserFromCurRoomTypingMembers()
            } else {
                sendMsgImgView.visibility = View.VISIBLE
                uploadFileImgView.visibility = View.GONE
                uploadVoiceImgView.visibility = View.GONE
                uploadPictureImgView.visibility = View.INVISIBLE

                viewModel.addCurUserToCurRoomTypingMembers()
            }
        }
    }

    inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> resumeAllVisibleAnim()
                else -> pauseAllVisibleAnim()
            }
        }
    }

    private fun resumeAllVisibleAnim() {
        recyclerView.forEachIndexed { i, _ ->
            getMessageViewHolder(i).stickerAnimView.resumeAnimation()
        }
    }

    private fun pauseAllVisibleAnim() {
        recyclerView.forEachIndexed { i, _ ->
            getMessageViewHolder(i).stickerAnimView.pauseAnimation()
        }
    }

    private fun getMessageViewHolder(position: Int): RoomActivityAdapter.MessageViewHolder {
        return recyclerView.getChildViewHolder(recyclerView[position]) as RoomActivityAdapter.MessageViewHolder
    }

    fun isRecyclerViewIdle(): Boolean {
        return recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE
    }
}