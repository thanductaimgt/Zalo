package vng.zalo.tdtai.zalo.ui.chat

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.bottom_sheet_message_actions.view.*
import kotlinx.android.synthetic.main.bottom_sheet_upload.view.*
import kotlinx.android.synthetic.main.dialog_view_images.*
import kotlinx.android.synthetic.main.part_sticker_message.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.abstracts.BindableViewHolder
import vng.zalo.tdtai.zalo.abstracts.KeyboardHeightObserver
import vng.zalo.tdtai.zalo.common.AlertDialog
import vng.zalo.tdtai.zalo.managers.*
import vng.zalo.tdtai.zalo.model.message.*
import vng.zalo.tdtai.zalo.model.room.Room
import vng.zalo.tdtai.zalo.model.room.RoomPeer
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.services.NotificationService
import vng.zalo.tdtai.zalo.ui.chat.emoji.EmojiFragment
import vng.zalo.tdtai.zalo.ui.chat.view_image.ViewMediaAdapter
import vng.zalo.tdtai.zalo.utils.*
import java.io.FileNotFoundException
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min


class ChatActivity : DaggerAppCompatActivity(), View.OnClickListener, View.OnLongClickListener, KeyboardHeightObserver {
    @Inject
    lateinit var sharedPrefsManager: SharedPrefsManager

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var externalIntentManager: ExternalIntentManager

    @Inject
    lateinit var messageManager: MessageManager

    @Inject
    lateinit var callService: CallService

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var alertDialog: AlertDialog

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: ChatViewModel by viewModels { viewModelFactory }

    @Inject
    lateinit var chatAdapter: ChatAdapter

    @Inject
    lateinit var viewMediaAdapter: ViewMediaAdapter

    @Inject
    lateinit var messageActionAdapter: MessageActionAdapter

    private var emojiFragment: EmojiFragment? = null
    private var isMessageJustSentByMe = false

    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var imageLocalUriString: String? = null
    private lateinit var uploadView: View
    private lateinit var messageActionsView: View
    private var isUploadImageClick: Boolean = true

    private var isViewImageLayoutShown = false

    private val compositeDisposable = CompositeDisposable()

    private lateinit var keyboardHeightProvider: KeyboardHeightProvider

    private var verticalScrollOffset = AtomicInteger(0)

    private var lastHeight = 0
    private var keyboardHeight = 0
        set(value) {
            if (value != field) {
                sharedPrefsManager.setKeyboardSize(value)
                frameLayout.layoutParams.height = value
                field = value
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // remove all room's chat notifications when enter
        NotificationManagerCompat.from(this).cancel(
                notificationService.getNotificationId(
                        intent.getStringExtra(Constants.ROOM_ID)!!
                )
        )

        initView()

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

    private fun initView() {
        setContentView(R.layout.activity_room)

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
        emojiImgView.setOnClickListener(this)
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

        msgEditText.setOnFocusChangeListener { _, b ->
            if (b) {
                frameLayout.visibility = View.GONE
                utils.showKeyboard(msgEditText)
            } else {
                utils.hideKeyboard(rootView)
            }
        }

        initBottomSheet()
        initUploadView()
        initMessageActionsView()
        initViewImageLayout()

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
                        chatAdapter.exoPlayer.playWhenReady = false

                        holder.itemView.stickerAnimView.pauseAnimation()
                    }
                }
            }

            setOnDismissListener {
                messageActionsView.imageView.setImageDrawable(null)

                this@ChatActivity.chatRecyclerView.forEach {
                    val holder = this@ChatActivity.chatRecyclerView.getChildViewHolder(it)
                    if (holder is ChatAdapter.MessageViewHolder) {
                        chatAdapter.exoPlayer.playWhenReady = true

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

    @SuppressLint("CheckResult")
    private fun submitMessagesAsync(messages: List<Message>) {
        Completable.fromCallable { submitMessages(messages) }
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({}, {
                    it.printStackTrace()
                })
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
                    Handler().postDelayed({
                        val index = chatAdapter.currentList.indexOfFirst { it.id == message.id }
                        if (index != -1) {
                            chatAdapter.notifyItemChanged(index, arrayListOf(Message.PAYLOAD_TIME, Message.PAYLOAD_AVATAR))
                        }
                    }, 200)
                }
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
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
            R.id.emojiImgView -> {
                utils.hideKeyboard(rootView)
                showOrHideEmojiFragment()
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
            R.id.imageView -> {
                val position = chatRecyclerView.getChildAdapterPosition(v.parent as View)
                val message = chatAdapter.currentList[position] as ResourceMessage

                //get surrounding messages
                @Suppress("UNCHECKED_CAST")
                val messages = viewModel.liveMessageMap.value!!.values
                        .filter { it.type == Message.TYPE_IMAGE }
                        .sortedByDescending { it.createdTime }
                        .toMutableList()
                        as ArrayList<ResourceMessage>

                showViewImageLayout(message, messages)
            }
            R.id.videoMessageLayout -> {
                val position = chatRecyclerView.getChildAdapterPosition(v.parent as View)
                val message = chatAdapter.currentList[position] as ResourceMessage

                //get surrounding messages
                val messages = arrayListOf(message)

                showViewImageLayout(message, messages)
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
                    val position = getChildAdapterPosition(v)

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
            //view image layout
            R.id.dismissImgView -> {
                hideViewImageLayout()
            }
            R.id.viewImagePhotoView -> {
                if (zoomTitleLayout.visibility == View.VISIBLE) {
                    zoomTitleLayout.visibility = View.GONE
                } else {
                    zoomTitleLayout.visibility = View.VISIBLE
                }
            }
            R.id.viewImagePlayerView -> {
                if (zoomTitleLayout.visibility == View.VISIBLE) {
                    zoomTitleLayout.visibility = View.GONE
                } else {
                    zoomTitleLayout.visibility = View.VISIBLE
                }
            }
            R.id.downloadImageImgView -> {
            }
            R.id.moreImgViewVID -> {
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

    private fun removeBottomSheetViews() {
        bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)?.let { frameLayout ->
            frameLayout.removeView(messageActionsView)
            frameLayout.removeView(uploadView)
        }
    }

    private fun startCallIfSupported() {
        if (viewModel.room is RoomPeer) {
            callService.startCallActivity(this, viewModel.room.id!!, viewModel.room.name!!, (viewModel.room as RoomPeer).phone!!, viewModel.room.avatarUrl!!)
        } else {
            Toast.makeText(this, "group call not supported", Toast.LENGTH_SHORT).show()
        }
    }

    private var lastPressedMessage: Message? = null

    override fun onLongClick(v: View?): Boolean {
        when (v!!.id) {
            R.id.rootItemView -> {
                val position = chatRecyclerView.getChildAdapterPosition(v)
                val message = chatAdapter.currentList[position]

                if (message.type != Message.TYPE_TYPING) {
                    showMessageActionView(message)
                }
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
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
        } else {
            Log.d(TAG, "resultCode != Activity.RESULT_OK")
        }
    }

    fun sendMessages(contents: List<String>, type: Int) {
        viewModel.addNewMessagesToFirestore(contents, type, SendMessageObserver())
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
        when {
            isViewImageLayoutShown -> {
                hideViewImageLayout()
            }
            frameLayout.visibility != View.GONE -> {
                frameLayout.visibility = View.GONE
            }
            else -> {
                super.onBackPressed()
            }
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

    private fun getViewHolder(position: Int): BindableViewHolder {
        return chatRecyclerView.getChildViewHolder(chatRecyclerView[position]) as BindableViewHolder
    }

    fun getMoreImageMessages(curMessage: ImageMessage, l: Int = 10, r: Int = 10, alsoAddCurMessage: Boolean = false): ArrayList<ImageMessage> {
        @Suppress("UNCHECKED_CAST")
        val imageMessages = viewModel.liveMessageMap.value!!.values
                .filter { it.type == Message.TYPE_IMAGE }
                .sortedByDescending { it.createdTime }
                as List<ImageMessage>
        val position = imageMessages.indexOfFirst { it.id == curMessage.id }

        val left = max(0, position - l)
        val leftImageMessages = imageMessages.subList(left, position)

        val right = min(imageMessages.size, position + 1 + r)
        val rightImageMessages = imageMessages.subList(position + 1, right)

        return ArrayList<ImageMessage>().apply {
            addAll(leftImageMessages)
            if (alsoAddCurMessage) {
                add(curMessage)
            }
            addAll(rightImageMessages)
        }
    }

    private fun initViewImageLayout() {
        viewPager.apply {
            adapter = viewMediaAdapter

            offscreenPageLimit = 20

            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    imageSenderNameTextView.text = viewModel.getNameFromPhone(viewMediaAdapter.resourceMessages[position].senderPhone!!)

                    val threshold = 5
                    val isLeftReach = position < threshold
                    val isRightReach = viewMediaAdapter.count - position < threshold - 1

                    if (viewMediaAdapter.resourceMessages[position] is ImageMessage && (isLeftReach || isRightReach)) {
                        val oldSize = viewMediaAdapter.resourceMessages.size

                        if (isLeftReach) {
                            @Suppress("UNCHECKED_CAST")
                            val moreResourceMessages = getMoreImageMessages(viewMediaAdapter.resourceMessages.first() as ImageMessage, r = 0)
                                    as ArrayList<ResourceMessage>
                            viewMediaAdapter.resourceMessages = moreResourceMessages.apply { addAll(viewMediaAdapter.resourceMessages) }
                        }

                        if (isRightReach) {
                            val moreImageMessages = getMoreImageMessages(viewMediaAdapter.resourceMessages.last() as ImageMessage, l = 0)
                            viewMediaAdapter.resourceMessages.addAll(moreImageMessages)
                        }

                        if (viewMediaAdapter.resourceMessages.size != oldSize) {
                            viewMediaAdapter.notifyDataSetChanged()
                        }
                    }
                }
            })
        }

        dismissImgView.setOnClickListener(this)
        downloadImageImgView.setOnClickListener(this)
        moreImgViewVID.setOnClickListener(this)
    }

    private fun showMessageActionView(message: Message) {
        messageActionsView.apply {
            nameTextView.text = message.senderPhone
            descTextView.text = message.getPreviewContent(this@ChatActivity)
            when (message.type) {
                Message.TYPE_TEXT -> imageView.visibility = View.GONE
                Message.TYPE_CALL -> imageView.visibility = View.GONE
                Message.TYPE_FILE -> {
                    Picasso.get().load(utils.getResIdFromFileExtension(
                                    utils.getFileExtension((message as FileMessage).fileName)))
                            .into(imageView)
                    imageView.visibility = View.VISIBLE
                }
                Message.TYPE_IMAGE -> {
                    Picasso.get()
                            .smartLoad((message as ImageMessage).url, resourceManager, imageView) {
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
                        Picasso.get().smartLoad(uri, resourceManager, imageView) {
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

    private fun showViewImageLayout(resourceMessage: ResourceMessage, resourceMessages: ArrayList<ResourceMessage>) {
        viewMediaAdapter.apply {
            this.resourceMessages = resourceMessages
            notifyDataSetChanged()
        }

        val curPosition = resourceMessages.indexOfFirst { it.id == resourceMessage.id }
        viewPager.setCurrentItem(curPosition, false)
        imageSenderNameTextView.text = viewModel.getNameFromPhone(resourceMessage.senderPhone!!)

        utils.setFullscreen(window, true)

        zoomTitleLayout.visibility = View.GONE
        viewImageLayout.visibility = View.VISIBLE

        viewMediaAdapter.exoPlayer.playWhenReady = true

        isViewImageLayoutShown = true
    }

    private fun hideViewImageLayout() {
        viewImageLayout.visibility = View.GONE
        viewMediaAdapter.exoPlayer.playWhenReady = false

        utils.setFullscreen(window, false)

        isViewImageLayoutShown = false
    }

    override fun onKeyboardHeightChanged(height: Int, orientation: Int) {
        val orientationLabel = if (orientation == Configuration.ORIENTATION_PORTRAIT) "portrait" else "landscape"
        Log.i(TAG, "onKeyboardHeightChanged in pixels: $height $orientationLabel")
        if (height > 0) {
            if (height != lastHeight)
                keyboardHeight = height - lastHeight
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

        viewMediaAdapter.exoPlayer.release()
    }
}