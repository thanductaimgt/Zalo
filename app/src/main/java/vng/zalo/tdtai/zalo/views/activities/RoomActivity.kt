package vng.zalo.tdtai.zalo.views.activities

//import vng.zalo.tdtai.zalo.views.fragments.ViewImageDialog
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
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
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.bottom_sheet_message_actions.view.*
import kotlinx.android.synthetic.main.bottom_sheet_upload.view.*
import kotlinx.android.synthetic.main.dialog_view_images.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.abstracts.CallStarter
import vng.zalo.tdtai.zalo.abstracts.ExternalIntentDispatcher
import vng.zalo.tdtai.zalo.abstracts.MessageManager
import vng.zalo.tdtai.zalo.adapters.MessageActionAdapter
import vng.zalo.tdtai.zalo.adapters.RoomActivityAdapter
import vng.zalo.tdtai.zalo.adapters.ViewImagePagerAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.message.FileMessage
import vng.zalo.tdtai.zalo.models.message.ImageMessage
import vng.zalo.tdtai.zalo.models.message.Message
import vng.zalo.tdtai.zalo.models.message.StickerMessage
import vng.zalo.tdtai.zalo.models.room.Room
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.*
import vng.zalo.tdtai.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.views.fragments.EmojiFragment
import kotlin.math.max
import kotlin.math.min


class RoomActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {
    private lateinit var viewModel: RoomActivityViewModel
    private lateinit var adapter: RoomActivityAdapter
    private var emojiFragment: EmojiFragment? = null
    private var isMessageJustSentByMe = false

    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var imageLocalUriString: String? = null
    private lateinit var uploadView: View
    private lateinit var messageActionsView: View
    private var isUploadImageClick: Boolean = true

    private var isViewImageLayoutShown = false

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()

        // remove all room's chat notifications when enter
        NotificationManagerCompat.from(this).cancel(
                Utils.getNotificationIdFromRoom(
                        intent.getStringExtra(Constants.ROOM_ID)
                )
        )

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(intent = intent)).get(RoomActivityViewModel::class.java)
        viewModel.liveMessageMap.observe(this, Observer { messageMap ->
            val messages = messageMap.values.sortedByDescending { it.createdTime }
            val typingMessages = viewModel.getTypingMessages(viewModel.liveTypingPhones.value!!)
            val allMessages = ArrayList<Message>().apply {
                addAll(typingMessages)
                addAll(messages)
            }

            submitMessages(allMessages)
        })
        viewModel.liveTypingPhones.observe(this, Observer { phones ->
            val typingMessages = viewModel.getTypingMessages(phones)
            val messages = viewModel.liveMessageMap.value!!.values.sortedByDescending { it.createdTime }
            val allMessages = ArrayList<Message>().apply {
                addAll(typingMessages)
                addAll(messages)
            }

            submitMessages(allMessages)
        })
        viewModel.livePeerLastOnlineTime.observe(this, Observer { lastOnlineTime ->
            onlineStatusTextView.text = Utils.getLastOnlineTimeFormat(this, lastOnlineTime)

            if (lastOnlineTime == null && viewModel.room.type == Room.TYPE_PEER) {
                onlineStatusImgView.visibility = View.VISIBLE
            } else {
                onlineStatusImgView.visibility = View.GONE
            }
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

    override fun onStop() {
        super.onStop()
        bottomSheetDialog.dismiss()
    }

    private fun initView() {
        setContentView(R.layout.activity_room)

        roomNameTextView.text = intent.getStringExtra(Constants.ROOM_NAME)

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

        deleteRoomButton.setOnClickListener(this)

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
        initUploadView()
        initMessageActionsView()
        initViewImageLayout()
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
            val frameLayout = bottomSheetDialog.findViewById<FrameLayout>(R.id.design_bottom_sheet)!!
            BottomSheetBehavior.from(frameLayout).apply {
                skipCollapsed = true
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
            layoutManager = GridLayoutManager(this@RoomActivity, 4)
            val spanCount = 4
            val spacing = 50 // 50px
            val includeEdge = true
            recyclerView.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, includeEdge))
            adapter = MessageActionAdapter().apply {
                this.actions = actions
                notifyDataSetChanged()
            }
        }
    }

    private fun submitMessages(messages: List<Message>) {
        val isMessageNumIncrease = messages.size - adapter.currentList.size > 0

        val isLastNotTypingMessageVisible = (recyclerView.layoutManager as LinearLayoutManager).let { layoutManager ->
            val adapterPosition = layoutManager.findFirstVisibleItemPosition().let { position ->
                if (position != -1) {
                    val itemView = layoutManager.findViewByPosition(position)!!
                    recyclerView.getChildAdapterPosition(itemView)
                } else {
                    -1
                }
            }

            val lastNotTypingMessagePosition = adapter.currentList.indexOfFirst { it.type != Message.TYPE_TYPING }

            adapterPosition <= lastNotTypingMessagePosition
        }

        adapter.submitList(messages) {
            if ((isMessageNumIncrease && isLastNotTypingMessageVisible) || isMessageJustSentByMe) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
                isMessageJustSentByMe = false
            }

            //update date, time of prev message
            if (isMessageNumIncrease) {
                Handler().postDelayed({ adapter.notifyItemChanged(1, arrayListOf(Message.PAYLOAD_TIME, Message.PAYLOAD_AVATAR)) }, 200)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.voiceCallImgView -> {
                startCall()
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
                    chooserType = ExternalIntentDispatcher.CHOOSER_TYPE_IMAGE
                } else {
                    requestCode = Constants.CHOOSE_VIDEO_REQUEST
                    chooserType = ExternalIntentDispatcher.CHOOSER_TYPE_VIDEO
                }
                ExternalIntentDispatcher.dispatchChooserIntent(this, requestCode, chooserType, true)
            }
            R.id.captureTV -> {
                val requestCode: Int
                val chooserType: Int
                if (isUploadImageClick) {
                    requestCode = Constants.CAPTURE_IMAGE_REQUEST
                    chooserType = ExternalIntentDispatcher.CAPTURE_TYPE_IMAGE
                } else {
                    requestCode = Constants.CAPTURE_VIDEO_REQUEST
                    chooserType = ExternalIntentDispatcher.CAPTURE_TYPE_VIDEO
                }
                ExternalIntentDispatcher.dispatchCaptureIntent(this, requestCode, chooserType) {
                    imageLocalUriString = it
                }
            }
            R.id.imageView -> {
                val position = recyclerView.getChildAdapterPosition(v.parent as View)
                val imageMessage = adapter.currentList[position] as ImageMessage

                //get surrounding messages
                val imageMessages = viewModel.liveMessageMap.value!!.values
                        .filter { it.type == Message.TYPE_IMAGE }
                        .sortedByDescending { it.createdTime }
                        .toMutableList()
                        as ArrayList<ImageMessage>

//                initViewImageLayout()
                showViewImageLayout(imageMessage, imageMessages)
            }
            R.id.uploadFileImgView -> {
                ExternalIntentDispatcher.dispatchChooserIntent(this, Constants.CHOOSE_FILE_REQUEST, ExternalIntentDispatcher.CHOOSER_TYPE_FILE, true)
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
                            MessageManager.deleteMessageAndAttachedData(viewModel.room.id!!, lastPressedMessage!!) {
                                Toast.makeText(this@RoomActivity, "deleted", Toast.LENGTH_SHORT).show()
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
                startCall()
            }
            //view image layout
            R.id.dismissImgView -> {
                hideViewImageLayout()
            }
            R.id.photoView -> {
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

            R.id.deleteRoomButton -> {
                ZaloApplication.notificationDialog.show(
                        supportFragmentManager,
                        title = "Xóa Room ?",
                        description = "",
                        button1Text = "Hủy",
                        button2Text = "Xóa",
                        button2Action = {
                            if (viewModel.room.memberMap != null) {
                                viewModel.removeAllListeners()
                                Database.deleteRoom(viewModel.room, viewModel.room.memberMap!!.map { it.key }) {
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

    private fun startCall() {
        CallStarter.startAudioCall(this, viewModel.room.id!!, viewModel.room.name!!, viewModel.room.avatarUrl!!)
    }

    private var lastPressedMessage: Message? = null

    override fun onLongClick(v: View?): Boolean {
        when (v!!.id) {
            R.id.rootItemView -> {
                val position = recyclerView.getChildAdapterPosition(v)
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
                                            Utils.getFileExtension((message as FileMessage).fileName)))
                                    .into(imageView)
                        }
                        Message.TYPE_IMAGE -> {
                            imageView.visibility = View.VISIBLE
                            Picasso.get().load(
                                    (message as ImageMessage).url
                            ).fit().centerCrop().into(imageView)
                        }
                        Message.TYPE_STICKER -> {
                            imageView.visibility = View.VISIBLE
                            imageView.setAnimationFromUrl((message as StickerMessage).url)
                        }
                    }

                    removeBottomSheetViews()
                }
                bottomSheetDialog.setContentView(messageActionsView)
                bottomSheetDialog.show()

                lastPressedMessage = message
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                Constants.CHOOSE_IMAGE_REQUEST -> {
                    Utils.assertNotNull(intent, TAG, "CHOOSE_IMAGES_REQUEST.intent") { intentNotNull ->
                        val localUris = getLocalUris(intentNotNull)

                        sendMessages(localUris, Message.TYPE_IMAGE)
                    }
                }
                Constants.CHOOSE_VIDEO_REQUEST -> {
                    Utils.assertNotNull(intent, TAG, "CHOOSE_VIDEO_REQUEST.intent") { intentNotNull ->
                        val localUris = getLocalUris(intentNotNull)

                        sendMessages(localUris, Message.TYPE_VIDEO)
                    }
                }
                Constants.CHOOSE_FILE_REQUEST -> {
                    Utils.assertNotNull(intent, TAG, "CHOOSE_FILES_REQUEST.intent") { intentNotNull ->
                        val localUris = getLocalUris(intentNotNull)

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
        viewModel.addNewMessagesToFirestore(this, contents, type, SendMessageObserver())
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
        if (isViewImageLayoutShown) {
            hideViewImageLayout()
        } else if (frameLayout.visibility != View.GONE) {
            frameLayout.visibility = View.GONE
        } else {
            super.onBackPressed()
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

    fun getMoreImageMessages(curMessage: ImageMessage, l: Int = 10, r: Int = 10, alsoAddCurMessage: Boolean = false): ArrayList<ImageMessage> {
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
            adapter = ViewImagePagerAdapter(this@RoomActivity, supportFragmentManager)
            val adapter = adapter as ViewImagePagerAdapter

            offscreenPageLimit = 20

            addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    imageSenderNameTextView.text = viewModel.getNameFromPhone(adapter.imageMessages[position].senderPhone!!)

                    val threshold = 5
                    val isLeftReach = position < threshold
                    val isRightReach = adapter.count - position < threshold - 1

                    if (isLeftReach || isRightReach) {
                        val oldSize = adapter.imageMessages.size

                        val curMessage = adapter.imageMessages[position]

                        if (isLeftReach) {
                            val moreImageMessages = getMoreImageMessages(adapter.imageMessages.first(), r = 0)
                            adapter.imageMessages = moreImageMessages.apply { addAll(adapter.imageMessages) }
                        }

                        if (isRightReach) {
                            val moreImageMessages = getMoreImageMessages(adapter.imageMessages.last(), l = 0)
                            adapter.imageMessages.addAll(moreImageMessages)
                        }

                        if (adapter.imageMessages.size != oldSize) {
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            })
        }

        dismissImgView.setOnClickListener(this)
        downloadImageImgView.setOnClickListener(this)
        moreImgViewVID.setOnClickListener(this)
    }

    private fun showViewImageLayout(imageMessage: ImageMessage, imageMessages: ArrayList<ImageMessage>) {
        (viewPager.adapter as ViewImagePagerAdapter).apply {
            this.imageMessages = imageMessages
            notifyDataSetChanged()
        }

        val curPosition = imageMessages.indexOfFirst { it.id == imageMessage.id }
        viewPager.setCurrentItem(curPosition, false)
        imageSenderNameTextView.text = viewModel.getNameFromPhone(imageMessage.senderPhone!!)

        Utils.setFullscreen(window, true)

        zoomTitleLayout.visibility = View.GONE
        viewImageLayout.visibility = View.VISIBLE
        isViewImageLayoutShown = true
    }

    private fun hideViewImageLayout() {
        viewImageLayout.visibility = View.INVISIBLE

        Utils.setFullscreen(window, false)

        isViewImageLayoutShown = false
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

    internal inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> resumeAllVisibleAnim()
                else -> pauseAllVisibleAnim()
            }
        }

//        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//            recyclerView.focusedChild?.let { focusedChild ->
//                val position = recyclerView.getChildAdapterPosition(focusedChild)
//                val holder = recyclerView.getChildViewHolder(focusedChild) as RoomActivityAdapter.MessageViewHolder
//                holder.playerView.player = adapter.exoPlayer
//
//                if (adapter.lastTouchedVideoMessage == null || adapter.lastTouchedVideoMessage != position) {
//                    holder.mediaSource?.let {
//                        //                    adapter.exoPlayer.release()
//                        adapter.exoPlayer.prepare(it)
//                        adapter.exoPlayer.playWhenReady = true
//
//                        Log.d(TAG, "onScrolled")
//                    }
//                    adapter.lastTouchedVideoMessage = position
//                }
//            }
//        }
    }

    internal inner class SendMessageObserver : io.reactivex.Observer<Message> {
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}