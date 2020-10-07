package com.mgt.zalo.ui.chat

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.forEach
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity
import com.mgt.zalo.base.BaseView
import com.mgt.zalo.base.BaseViewHolder
import com.mgt.zalo.base.KeyboardHeightObserver
import com.mgt.zalo.data_model.media.ImageMedia
import com.mgt.zalo.data_model.media.Media
import com.mgt.zalo.data_model.media.VideoMedia
import com.mgt.zalo.data_model.message.*
import com.mgt.zalo.data_model.room.Room
import com.mgt.zalo.data_model.room.RoomPeer
import com.mgt.zalo.manager.ExternalIntentManager
import com.mgt.zalo.ui.chat.emoji.EmojiFragment
import com.mgt.zalo.util.Constants
import com.mgt.zalo.util.GridSpacingItemDecoration
import com.mgt.zalo.util.KeyboardHeightProvider
import com.mgt.zalo.util.TAG
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_chat.view.*
import kotlinx.android.synthetic.main.bottom_sheet_message_actions.view.*
import kotlinx.android.synthetic.main.bottom_sheet_upload.view.*
import kotlinx.android.synthetic.main.part_message_sticker.view.*
import java.io.FileNotFoundException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.absoluteValue


class ChatActivity : BaseActivity(), KeyboardHeightObserver {
    private val viewModel: ChatViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var chatAdapter: ChatAdapter

    @Inject
    lateinit var messageActionAdapter: MessageActionAdapter

    private var emojiFragment: EmojiFragment? = null
    private var isMessageJustSentByMe = false

    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var imageLocalUriString: String? = null
    private lateinit var uploadView: View
    private lateinit var messageActionsView: View
    private var isUploadImageClick: Boolean = true

    private val compositeDisposable = CompositeDisposable()

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

    private var verticalScrollOffset = AtomicInteger(0)

    private var lastHeight = 0
    private var keyboardHeight = 0
        set(value) {
            if (value != field) {
                sharedPrefsManager.setKeyboardSize(value)
                frameLayout.layoutParams = frameLayout.layoutParams.apply {
                    height = value
                }
                field = value
            }
        }

    override fun onBindViews() {
        setStatusBarMode(false)
        requestFullScreen()
        val rootView = layoutInflater.inflate(R.layout.activity_chat, null)
        makeRoomForStatusBar(this, rootView.toolbar, BaseView.MAKE_ROOM_TYPE_MARGIN)
        setContentView(rootView)

        roomNameTextView.text = viewModel.room.getDisplayName(resourceManager)

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
        reactImgView.setOnClickListener(this)
        uploadFileImgView.setOnClickListener(this)
        uploadImageImgView.setOnClickListener(this)
        uploadVideoImgView.setOnClickListener(this)

        //test
        deleteRoomButton.setOnClickListener(this)

        chatRecyclerView.apply {
            adapter = this@ChatActivity.chatAdapter
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                reverseLayout = true
                stackFromEnd = true
            }
            addOnScrollListener(ScrollListener())
            setItemViewCacheSize(50)
        }

//        msgEditText.setOnFocusChangeListener { _, b ->
//            if (b) {
//                frameLayout.visibility = View.GONE
//                utils.showKeyboard(msgEditText)
//            } else {
//                utils.hideKeyboard(rootView)
//            }
//        }

        initBottomSheet()
        initUploadView()
        initMessageActionsView()

        keyboardHeight = sharedPrefsManager.getKeyboardSize()

        //make recycler view stay at same offset when keyboard on, off

        chatRecyclerView.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            val y = oldBottom - bottom
            if (y.absoluteValue > 0) {
                chatRecyclerView.post {
                    if (y > 0 || verticalScrollOffset.get().absoluteValue >= y.absoluteValue) {
                        chatRecyclerView.scrollBy(0, y)
                    } else {
                        chatRecyclerView.scrollBy(0, verticalScrollOffset.get())
                    }
                }
            }
        }
    }

    private fun initBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this).apply {
            // Fix BottomSheetDialog not showing after getting hidden when the user drags it down
            setOnShowListener { dialogInterface ->
                val bottomSheetDialog = dialogInterface as BottomSheetDialog
                val frameLayout = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
                BottomSheetBehavior.from(frameLayout).apply {
                    skipCollapsed = true
                }

                this@ChatActivity.chatRecyclerView.forEach {
                    val holder = this@ChatActivity.chatRecyclerView.getChildViewHolder(it)
                    if (holder is ChatAdapter.MessageViewHolder) {
                        holder.itemView.stickerAnimView.pauseAnimation()
                    }
                }
            }

            setOnDismissListener {
                messageActionsView.imageView.setImageDrawable(null)

                this@ChatActivity.chatRecyclerView.forEach {
                    val holder = this@ChatActivity.chatRecyclerView.getChildViewHolder(it)
                    if (holder is ChatAdapter.MessageViewHolder) {
                        playbackManager.resume()

                        holder.itemView.stickerAnimView.resumeAnimation()
                    }
                }
            }
        }
    }

    private fun initUploadView() {
        uploadView = layoutInflater.inflate(R.layout.bottom_sheet_upload, null)
        uploadView.chooseFromGalleryTV.setOnClickListener(this)
        uploadView.captureTV.setOnClickListener(this)
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
            layoutManager = GridLayoutManager(this@ChatActivity, 4)
            val spanCount = 4
            val spacing = 50 // 50px
            val includeEdge = true
            addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
            adapter = messageActionAdapter
            messageActionAdapter.actions = actions
            messageActionAdapter.notifyDataSetChanged()
        }
    }

    override fun onViewsBound() {
        // remove all room's chat notifications when enter
        NotificationManagerCompat.from(this).cancel(
                notificationService.getNotificationId(
                        intent.getStringExtra(Constants.ROOM_ID)!!
                )
        )

        val messagesObserver = MessagesObserver()

        viewModel.liveMessageMap.observe(this, messagesObserver)

        viewModel.liveTypingMembers.observe(this, messagesObserver)

        viewModel.liveSeenPhones.observe(this, messagesObserver)

        viewModel.livePeerLastOnlineTime.observe(this, Observer { lastOnlineTime ->
            onlineStatusTextView.text = utils.getLastOnlineTimeFormat(lastOnlineTime)

            if (lastOnlineTime == null && viewModel.room.type == Room.TYPE_PEER) {
                onlineStatusImgView.visibility = View.VISIBLE
            } else {
                onlineStatusImgView.visibility = View.GONE
            }
        })

        viewModel.liveIsLoading.observe(this, Observer { isLoading ->
            if (!isLoading)
                loadingAnimView.visibility = View.GONE
        })

        keyboardHeightProvider = KeyboardHeightProvider(this)

        // make sure to start the keyboard height provider after the onResume
        // of this activity. This is because a popup window must be initialised
        // and attached to the activity root view.
        rootView.post {
            keyboardHeightProvider.start()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        // stop receiving chat notifications of this room when in active state
        sessionManager.currentRoomId = intent.getStringExtra(Constants.ROOM_ID)

        keyboardHeightProvider.setKeyboardHeightObserver(this)
    }

    override fun onPause() {
        super.onPause()
        // start receiving chat notifications of this room when in inactive state,
        sessionManager.currentRoomId = null
        // remove from typing list if inactive
        viewModel.removeCurUserFromCurRoomTypingMembers()

        keyboardHeightProvider.setKeyboardHeightObserver(null)
    }

    override fun onStop() {
        super.onStop()
        bottomSheetDialog.dismiss()
    }

    @SuppressLint("CheckResult")
    private fun submitMessagesAsync(messages: List<Message>) {
        submitMessages(messages)
//        Completable.fromCallable { submitMessages(messages) }
//                .subscribeOn(Schedulers.computation())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe({}, {
//                    it.printStackTrace()
//                })
    }

    private fun submitMessages(messages: List<Message>) {
        val isMessageNumIncrease = messages.size - chatAdapter.currentList.size > 0

        val isLastRealMessageVisible = (chatRecyclerView.layoutManager as LinearLayoutManager).let { layoutManager ->
            val adapterPosition = layoutManager.findFirstVisibleItemPosition().let { position ->
                if (position != -1) {
                    val itemView = layoutManager.findViewByPosition(position)!!
                    chatRecyclerView.getChildAdapterPosition(itemView)
                } else {
                    -1
                }
            }

            val lastRealMessagePosition = chatAdapter.currentList.indexOfFirst { it.type != Message.TYPE_TYPING && it.type != Message.TYPE_SEEN }

            adapterPosition <= lastRealMessagePosition
        }

        chatAdapter.submitList(messages) {
            if ((isMessageNumIncrease && isLastRealMessageVisible) || isMessageJustSentByMe) {
                (chatRecyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
                isMessageJustSentByMe = false
            }

            //update date, time of prev message
            if (isMessageNumIncrease) {
                val indices = ArrayList<Int>()
                chatAdapter.currentList.forEachIndexed { index, message ->
                    if (message.type != Message.TYPE_SEEN && message.type != Message.TYPE_TYPING) {
                        if (indices.size >= 2) {
                            return@forEachIndexed
                        } else {
                            indices.add(index)
                        }
                    }
                }
                if (indices.size == 2) {
                    val message = chatAdapter.currentList[indices[1]]
                    val index = chatAdapter.currentList.indexOfFirst { it.id == message.id }

                    if (index >= 0 && index < chatRecyclerView.childCount) {
                        chatRecyclerView[index].post {
                            chatAdapter.notifyItemChanged(index, arrayListOf(Message.PAYLOAD_TIME, Message.PAYLOAD_AVATAR))
                        }
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
//            R.id.msgEditText -> showEmojiFragment()
            R.id.voiceCallImgView -> {
                startCallIfSupported()
            }
            R.id.sendMsgImgView -> {
                if (msgEditText.text != null && msgEditText.text.toString() != "") {
                    val content = msgEditText.text.toString()
                    sendMessages(ArrayList<String>().apply { add(content) }, Message.TYPE_TEXT)
                    msgEditText.setText("")
                }
            }
            R.id.reactImgView -> {
                if (isKeyboardOn) {
                    utils.hideKeyboard(rootView)
                }

                if (isEmojiFragmentOn) {
                    hideEmojiFragment()
                } else {
                    showEmojiFragment()
                }
                isEmojiFragmentOn = !isEmojiFragmentOn
            }
            R.id.uploadImageImgView -> {
                isUploadImageClick = true
                removeBottomSheetViews()
                uploadView.titleTextView.text = getString(R.string.label_upload_image)
                bottomSheetDialog.setContentView(uploadView)
                bottomSheetDialog.show()
            }
            R.id.uploadVideoImgView -> {
                isUploadImageClick = false
                removeBottomSheetViews()
                uploadView.titleTextView.text = getString(R.string.label_upload_video)
                bottomSheetDialog.setContentView(uploadView)
                bottomSheetDialog.show()
            }
            R.id.chooseFromGalleryTV -> {
                val requestCode: Int
                val chooserType: Int
                if (isUploadImageClick) {
                    requestCode = Constants.CHOOSE_IMAGE_REQUEST
                    chooserType = ExternalIntentManager.CHOOSER_TYPE_IMAGE
                } else {
                    requestCode = Constants.CHOOSE_VIDEO_REQUEST
                    chooserType = ExternalIntentManager.CHOOSER_TYPE_VIDEO
                }
                externalIntentManager.dispatchChooserIntent(this, requestCode, chooserType, true)
            }
            R.id.captureTV -> {
                val requestCode: Int
                val chooserType: Int
                if (isUploadImageClick) {
                    requestCode = Constants.CAPTURE_IMAGE_REQUEST
                    chooserType = ExternalIntentManager.CAPTURE_TYPE_IMAGE
                } else {
                    requestCode = Constants.CAPTURE_VIDEO_REQUEST
                    chooserType = ExternalIntentManager.CAPTURE_TYPE_VIDEO
                }
                externalIntentManager.dispatchCaptureIntent(this, requestCode, chooserType) {
                    imageLocalUriString = it
                }
            }
            R.id.imageView, R.id.videoMessageLayout -> {
                val position = chatRecyclerView.getChildAdapterPosition(view.parent as View)
                val message = chatAdapter.currentList[position] as MediaMessage

                val media = if (message is ImageMessage) ImageMedia(message.url, message.ratio) else VideoMedia(message.url, message.ratio)
                val medias = arrayListOf<Media>().apply {
                    addAll(getSurroundMediaMessages(message).map {
                        if (it is ImageMessage) {
                            ImageMedia(it.url, it.ratio)
                        } else {
                            VideoMedia(it.url, it.ratio)
                        }
                    })
                }
                zaloFragmentManager.addMediaFragment(media, medias)
            }
            R.id.uploadFileImgView -> {
                externalIntentManager.dispatchChooserIntent(this, Constants.CHOOSE_FILE_REQUEST, ExternalIntentManager.CHOOSER_TYPE_FILE, true)
            }
            R.id.backImgView -> {
                msgEditText.clearFocus()
                finish()
            }
            R.id.rootActionView -> {
                messageActionsView.recyclerView.apply {
                    val position = getChildAdapterPosition(view)

                    when ((this.adapter as MessageActionAdapter).actions[position].first) {
                        R.drawable.delete -> {
                            messageManager.deleteMessageAndAttachedData(viewModel.room.id!!, lastPressedMessage!!) {
                                Toast.makeText(this@ChatActivity, "deleted", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                bottomSheetDialog.dismiss()
            }
            R.id.cancelImgView -> {
                bottomSheetDialog.dismiss()
            }
            R.id.callbackTV -> {
                startCallIfSupported()
            }

            //test
            R.id.deleteRoomButton -> {
                alertDialog.show(
                        supportFragmentManager,
                        title = "Xóa Room ?",
                        description = "",
                        button1Text = "Hủy",
                        button2Text = "Xóa",
                        button2Action = {
                            if (viewModel.room.memberMap != null) {
                                viewModel.removeAllListeners()
                                database.deleteRoom(viewModel.room, viewModel.room.memberMap!!.map { it.key }) {
                                    Toast.makeText(this, "room deleted", Toast.LENGTH_SHORT).show()
                                    finish()
                                }
                            } else {
                                Toast.makeText(this, "member map not loaded", Toast.LENGTH_SHORT).show()
                            }
                        }
                )
            }
        }
    }

    private var lastPressedMessage: Message? = null

    override fun onLongClick(view: View): Boolean {
        when (view.id) {
            R.id.rootItemView -> {
                val position = chatRecyclerView.getChildAdapterPosition(view)
                val message = chatAdapter.currentList[position]

                if (message.type != Message.TYPE_TYPING) {
                    showMessageActionView(message)
                }
            }
        }
        return true
    }

    private fun removeBottomSheetViews() {
        bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.let { frameLayout ->
            frameLayout.removeView(messageActionsView)
            frameLayout.removeView(uploadView)
        }
    }

    private fun startCallIfSupported() {
        if (viewModel.room is RoomPeer) {
            callService.startCallActivity(this, viewModel.room.id!!, viewModel.room.name!!, (viewModel.room as RoomPeer).peerId!!, viewModel.room.avatarUrl!!)
        } else {
            Toast.makeText(this, "group call not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, intent: Intent?) {
        when (requestCode) {
            Constants.CHOOSE_IMAGE_REQUEST -> {
                utils.assertNotNull(intent, TAG, "CHOOSE_IMAGES_REQUEST.intent") { intentNotNull ->
                    val localUris = utils.getLocalUris(intentNotNull)

                    sendMessages(localUris, Message.TYPE_IMAGE)
                }
            }
            Constants.CHOOSE_VIDEO_REQUEST -> {
                utils.assertNotNull(intent, TAG, "CHOOSE_VIDEO_REQUEST.intent") { intentNotNull ->
                    val localUris = utils.getLocalUris(intentNotNull)

                    sendMessages(localUris, Message.TYPE_VIDEO)
                }
            }
            Constants.CHOOSE_FILE_REQUEST -> {
                utils.assertNotNull(intent, TAG, "CHOOSE_FILES_REQUEST.intent") { intentNotNull ->
                    val localUris = utils.getLocalUris(intentNotNull)

                    sendMessages(localUris, Message.TYPE_FILE)
                }
            }
            Constants.CAPTURE_IMAGE_REQUEST -> {
                imageLocalUriString?.let { localUri ->
                    sendMessages(ArrayList<String>().apply { add(localUri) }, Message.TYPE_IMAGE)
                }
            }
            Constants.CAPTURE_VIDEO_REQUEST -> {
                imageLocalUriString?.let { localUri ->
                    sendMessages(ArrayList<String>().apply { add(localUri) }, Message.TYPE_VIDEO)
                }
            }
        }

        bottomSheetDialog.dismiss()
    }

    fun sendMessages(contents: List<String>, type: Int) {
        viewModel.addNewMessagesToFirestore(contents, type, SendMessageObserver())
        isMessageJustSentByMe = true
    }

    private fun showEmojiFragment() {
        if (emojiFragment == null) {
            emojiFragment = zaloFragmentManager.addFragment(EmojiFragment(), false, frameLayout.id) as EmojiFragment
        }
        showEmojiLayout()
    }

    private fun showEmojiLayout() {
        frameLayout.visibility = View.VISIBLE
    }

    private fun hideEmojiFragment() {
        frameLayout.visibility = View.GONE
    }

    override fun onBackPressedCustomized() {
        if (isEmojiFragmentOn) {
            hideEmojiFragment()
        } else {
            super.onBackPressedCustomized()
        }
    }

    private fun pauseOrResumeAllVisibleAnim(doResume: Boolean) {
        chatRecyclerView.forEachIndexed { i, _ ->
            val holder = getViewHolder(i)
            if (holder is ChatAdapter.MessageViewHolder) {
                if (doResume) {
                    holder.itemView.stickerAnimView.resumeAnimation()
                } else {
                    holder.itemView.stickerAnimView.pauseAnimation()
                }
            }
        }
    }

    private fun getViewHolder(position: Int): BaseViewHolder {
        return chatRecyclerView.getChildViewHolder(chatRecyclerView[position]) as BaseViewHolder
    }

    private fun showMessageActionView(message: Message) {
        messageActionsView.apply {
            nameTextView.text = message.senderId
            descTextView.text = message.getPreviewContent(this@ChatActivity)
            when (message.type) {
                Message.TYPE_TEXT -> imageView.visibility = View.GONE
                Message.TYPE_CALL -> imageView.visibility = View.GONE
                Message.TYPE_FILE -> {
                    imageLoader.load(utils.getResIdFromFileExtension(
                            utils.getFileExtension((message as FileMessage).fileName)), imageView)
                    imageView.visibility = View.VISIBLE
                }
                Message.TYPE_IMAGE -> {
                    imageLoader.load((message as ImageMessage).url, imageView) {
                                it.fit().centerCrop()
                            }

                    imageView.visibility = View.VISIBLE
                }
                Message.TYPE_STICKER -> {
                    imageView.setAnimationFromUrl((message as StickerMessage).url)
                    imageView.visibility = View.VISIBLE
                }
                Message.TYPE_VIDEO -> {
                    resourceManager.getVideoThumbUri((message as VideoMessage).url) { uri ->
                        imageLoader.load(uri, imageView) {
                            it.fit()
                                    .centerCrop()
                                    .error(R.drawable.load_image_fail)
                        }
                    }
                    imageView.visibility = View.VISIBLE
                }
            }

            removeBottomSheetViews()
        }
        bottomSheetDialog.setContentView(messageActionsView)
        bottomSheetDialog.show()

        lastPressedMessage = message
    }

    private fun getSurroundMediaMessages(curMessage: Message): List<MediaMessage> {
        //get surrounding messages
        @Suppress("UNCHECKED_CAST")
        return viewModel.liveMessageMap.value!!.values
                .filterIsInstance<MediaMessage>()
                .sortedByDescending { it.createdTime }
                .toMutableList() as ArrayList<MediaMessage>
    }

    private var isKeyboardOn = false
    private var isEmojiFragmentOn = false

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        val orientationLabel = if (orientation == Configuration.ORIENTATION_PORTRAIT) "portrait" else "landscape"
        Log.i(TAG, "onKeyboardHeightChanged in pixels: $height $orientationLabel")
        if (height > 0) {
            if (height != lastHeight) {
                keyboardHeight = height - lastHeight
            }
            if (!isKeyboardOn) {
                isKeyboardOn = true
                isEmojiFragmentOn = false
                showEmojiLayout()
            }
        } else {
            if (isKeyboardOn) {
                isKeyboardOn = false
                if (!isEmojiFragmentOn) {
                    hideEmojiFragment()
                }
            }
        }
        lastHeight = height
    }

    //

    private inner class InputTextListener : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isEmpty()) {
                sendMsgImgView.visibility = View.GONE
                uploadFileImgView.visibility = View.VISIBLE
                uploadVoiceImgView.visibility = View.VISIBLE
                uploadImageImgView.visibility = View.VISIBLE
                uploadVideoImgView.visibility = View.VISIBLE

                viewModel.removeCurUserFromCurRoomTypingMembers()
            } else {
                sendMsgImgView.visibility = View.VISIBLE
                uploadFileImgView.visibility = View.GONE
                uploadVoiceImgView.visibility = View.GONE
                uploadImageImgView.visibility = View.GONE
                uploadVideoImgView.visibility = View.GONE

                viewModel.addCurUserToCurRoomTypingMembers()
            }
        }
    }

    private inner class ScrollListener : RecyclerView.OnScrollListener() {
        private var state = AtomicInteger(RecyclerView.SCROLL_STATE_IDLE)

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            state.compareAndSet(RecyclerView.SCROLL_STATE_IDLE, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    if (!state.compareAndSet(RecyclerView.SCROLL_STATE_SETTLING, newState)) {
                        state.compareAndSet(RecyclerView.SCROLL_STATE_DRAGGING, newState)
                    }

                    pauseOrResumeAllVisibleAnim(true)
                }
                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    state.compareAndSet(RecyclerView.SCROLL_STATE_IDLE, newState)

                    pauseOrResumeAllVisibleAnim(false)
                }
                RecyclerView.SCROLL_STATE_SETTLING -> {
                    state.compareAndSet(RecyclerView.SCROLL_STATE_DRAGGING, newState)
                }
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (state.get() != RecyclerView.SCROLL_STATE_IDLE) {
                verticalScrollOffset.getAndAdd(dy)
            }

            if (!chatRecyclerView.canScrollVertically(-1) && viewModel.liveIsLoading.value!!) {
                loadingAnimView.visibility = View.VISIBLE
            }

            val lastVisiblePosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            if (viewModel.shouldLoadMoreMessages() && lastVisiblePosition > chatAdapter.currentList.size - 8) {
                viewModel.loadMoreMessages(chatAdapter.currentList.lastOrNull())
            }
        }
    }

    private inner class SendMessageObserver : io.reactivex.Observer<Message> {
        override fun onComplete() {
            Log.d(TAG, "onComplete")
        }

        override fun onSubscribe(d: Disposable) {
            compositeDisposable.add(d)
        }

        override fun onNext(t: Message) {
            viewModel.addOrUpdateMessage(t)
        }

        override fun onError(e: Throwable) {
            e.printStackTrace()
            val message = getString(if (e is FileNotFoundException) {
                R.string.description_no_connection
            } else {
                R.string.label_error_occurred
            })
            Toast.makeText(this@ChatActivity, message, Toast.LENGTH_SHORT).show()
        }
    }

    private inner class MessagesObserver : Observer<Any> {
        override fun onChanged(t: Any?) {
            val messages = viewModel.getCurRealMessages()
            val seenMessage = viewModel.getCurSeenMessage()
            val typingMessages = viewModel.getCurTypingMessages()

            val allMessages = ArrayList<Message>().apply {
                if (seenMessage.seenMembers.isEmpty()) {
                    add(seenMessage)
                    addAll(typingMessages)
                } else {
                    addAll(typingMessages)
                    add(seenMessage)
                }
                addAll(messages)
            }

            submitMessagesAsync(allMessages)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()

        keyboardHeightProvider.close()
    }
}