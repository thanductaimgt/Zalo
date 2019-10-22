package vng.zalo.tdtai.zalo.views.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.forEachIndexed
import androidx.core.view.get
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_room.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.adapters.RoomActivityAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.Message
import vng.zalo.tdtai.zalo.models.Sticker
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.MessageDiffCallback
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.views.fragments.EmojiFragment


class RoomActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: RoomActivityViewModel
    private lateinit var adapter: RoomActivityAdapter
    private var emojiFragment: EmojiFragment? = null
    private var isMessageJustSentByMe = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        initView()

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
        setSupportActionBar(toolbar.apply {
            title = intent.getStringExtra(Constants.ROOM_NAME)
        })

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            emojiImgView.setBackgroundResource(outValue.resourceId)
            uploadFileImgView.setBackgroundResource(outValue.resourceId)
            voiceImgView.setBackgroundResource(outValue.resourceId)
            pictureImgView.setBackgroundResource(outValue.resourceId)
            sendMsgImgView.setBackgroundResource(outValue.resourceId)
        }

        msgEditText.apply {
            addTextChangedListener(InputTextListener())
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_SEND -> {
                        sendTextMessage()
                        true
                    }
                    else -> false
                }
            }
            if (intent.getBooleanExtra(Constants.SHOW_KEYBOARD, false)) {
                requestFocus()
            }
        }

        msgEditText.setOnClickListener(this)
        sendMsgImgView.setOnClickListener(this)
        emojiImgView.setOnClickListener(this)
        uploadFileImgView.setOnClickListener(this)

        adapter = RoomActivityAdapter(this, MessageDiffCallback())
        with(recyclerView) {
            adapter = this@RoomActivity.adapter
            layoutManager = LinearLayoutManager(this@RoomActivity).apply { reverseLayout = true }
            addOnScrollListener(ScrollListener())
            setItemViewCacheSize(50)
        }

        msgEditText.setOnFocusChangeListener { _, b ->
            if (b) {
                frameLayout.visibility = View.GONE
                Utils.showKeyboard(this, msgEditText)
            } else {
                Utils.hideKeyboard(this@RoomActivity, rootView)
            }
        }
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

    @Synchronized
    private fun submitMessages(messages: List<Message>) {
        val newMessages = if (adapter.currentList === messages) ArrayList<Message>().apply { addAll(messages) } else messages
        adapter.submitList(newMessages) {
            val isLastMessageCompletelyVisible = (recyclerView.layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition() <= 1
            if (isLastMessageCompletelyVisible || isMessageJustSentByMe) {
                recyclerView.scrollToPosition(0)
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
            R.id.action_settings -> true
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sendMsgImgView -> sendTextMessage()
            R.id.emojiImgView -> {
                Utils.hideKeyboard(this@RoomActivity, rootView)
                showOrHideEmojiFragment()
            }
            R.id.uploadFileImgView -> {
//                val sheetView = layoutInflater.inflate(R.layout.navi_bot_activity_create_group, null)
//                bottomSheetDialog.setContentView(sheetView)
//                sheetView.choosePicTextView.setOnClickListener(this)
//                sheetView.takePicTextView.setOnClickListener(this)
//
//                bottomSheetDialog.show()
            }
        }
    }

    private fun sendTextMessage() {
        val content = msgEditText.text.toString()
        isMessageJustSentByMe = true
        viewModel.addNewMessageToFirestore(content, Message.TYPE_TEXT)

        msgEditText.setText("")
    }

    fun sendStickerMessage(sticker: Sticker) {
        viewModel.addNewMessageToFirestore(sticker.url!!, Message.TYPE_STICKER)
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
                voiceImgView.visibility = View.VISIBLE
                pictureImgView.visibility = View.VISIBLE

                viewModel.removeCurUserFromCurRoomTypingMembers()
            } else {
                sendMsgImgView.visibility = View.VISIBLE
                uploadFileImgView.visibility = View.GONE
                voiceImgView.visibility = View.GONE
                pictureImgView.visibility = View.INVISIBLE

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

    private fun getMessageViewHolder(position:Int): RoomActivityAdapter.MessageViewHolder{
        return recyclerView.getChildViewHolder(recyclerView[position]) as RoomActivityAdapter.MessageViewHolder
    }

    fun isRecyclerViewIdle():Boolean{
        return recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE
    }
}