package vng.zalo.tdtai.zalo.views.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.item_receive_room_activity.view.*
import kotlinx.android.synthetic.main.item_send_room_activity.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.adapters.RoomActivityAdapter
import vng.zalo.tdtai.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.models.Sticker
import vng.zalo.tdtai.zalo.utils.Constants
import vng.zalo.tdtai.zalo.utils.MessageDiffCallback
import vng.zalo.tdtai.zalo.utils.TAG
import vng.zalo.tdtai.zalo.utils.Utils
import vng.zalo.tdtai.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.views.fragments.EmojiFragment

class RoomActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: RoomActivityViewModel
    private lateinit var adapter: RoomActivityAdapter
    private var emojiFragment: EmojiFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        initView()

        viewModel = ViewModelProvider(this, ViewModelFactory.getInstance(intent = intent)).get(RoomActivityViewModel::class.java)
        viewModel.liveMessages.observe(this, Observer { messages ->
            Log.d(TAG, "are two list diff?: ${messages != adapter.currentList}")
            adapter.submitList(messages)
        })
    }

    private fun initView() {
        toolbar.title = intent.getStringExtra(Constants.ROOM_NAME)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                ?: Log.e(TAG, "actionBar is null")

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
        }

        msgEditText.setOnClickListener(this)
        sendMsgImgView.setOnClickListener(this)
        emojiImgView.setOnClickListener(this)
        uploadFileImgView.setOnClickListener(this)

        adapter = RoomActivityAdapter(MessageDiffCallback())
        with(recyclerView) {
            adapter = this@RoomActivity.adapter
            layoutManager = LinearLayoutManager(this@RoomActivity).apply { reverseLayout = true }
            addOnScrollListener(ScrollListener())
            setItemViewCacheSize(50)
        }

        msgEditText.setOnFocusChangeListener { _, b ->
            if (b) {
                msgEditText.post {
                    Utils.showKeyboard(this, msgEditText)
                }
            } else {
                Utils.hideKeyboard(this@RoomActivity, rootView)
            }
        }
//        // observe when keyboard is on or off
//        rootView.viewTreeObserver.addOnGlobalLayoutListener {
//            val heightDiff = rootView.rootView.height - rootView.height
//            if (heightDiff > Utils.dpToPx(this@RoomActivity, 200)) { // if more than 200 dp, it's probably a keyboard...
//                keyboardSize = Utils.pxToDp(this@RoomActivity, heightDiff.toFloat())
//                Log.d(TAG, "addOnGlobalLayoutListener.keyboardSize: $keyboardSize")
//            }
//        }
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
                addOrRemoveEmojiFragment()
            }
            R.id.msgEditText -> {
                frameLayout.visibility = View.GONE
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
        viewModel.addNewMessageToFirestore(content, Constants.MESSAGE_TYPE_TEXT)

        msgEditText.setText("")
    }

    fun sendStickerMessage(sticker: Sticker) {
        viewModel.addNewMessageToFirestore(sticker.url!!, Constants.MESSAGE_TYPE_STICKER)
    }

    private fun addOrRemoveEmojiFragment() {
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
            } else {
                sendMsgImgView.visibility = View.VISIBLE
                uploadFileImgView.visibility = View.GONE
                voiceImgView.visibility = View.GONE
                pictureImgView.visibility = View.INVISIBLE
            }
        }
    }

    inner class ScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> resumeAllAttachedToWinDowAnim()
                else -> pauseAllAnim()
            }
        }
    }

    private fun resumeAllAttachedToWinDowAnim() {
        for (i in 0 until recyclerView.size) {
            if (recyclerView[i].sendMsgAnimView?.isAttachedToWindow == true) {
                recyclerView[i].sendMsgAnimView.resumeAnimation()
            }
            if (recyclerView[i].recvMsgAnimView?.isAttachedToWindow == true) {
                recyclerView[i].recvMsgAnimView.resumeAnimation()
            }
        }
    }

    private fun pauseAllAnim() {
        for (i in 0 until recyclerView.size) {
            recyclerView[i].sendMsgAnimView?.pauseAnimation()
            recyclerView[i].recvMsgAnimView?.pauseAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }
}