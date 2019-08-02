package vng.zalo.tdtai.zalo.zalo.views.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.size
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_room.*
import kotlinx.android.synthetic.main.item_receive_room_activity.view.*
import kotlinx.android.synthetic.main.item_send_room_activity.view.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.zalo.adapters.RoomActivityAdapter
import vng.zalo.tdtai.zalo.zalo.factories.ViewModelFactory
import vng.zalo.tdtai.zalo.zalo.models.Sticker
import vng.zalo.tdtai.zalo.zalo.views.fragments.EmojiFragment
import kotlin.math.max
import vng.zalo.tdtai.zalo.zalo.utils.TAG

class RoomActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var viewModel: RoomActivityViewModel
    private lateinit var adapter: RoomActivityAdapter
    private var emojiFragment: EmojiFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        initView()

        viewModel = ViewModelProviders.of(this, ViewModelFactory.getInstance(intent = intent)).get(RoomActivityViewModel::class.java)
        viewModel.liveMessages.observe(this, Observer { messageList ->
            adapter.messages = messageList
            adapter.notifyDataSetChanged()
            //TODO
            this.scrollRecyclerViewToLastPosition()
//            adapter.submitList(messageList) { this.scrollRecyclerViewToLastPosition() }
        })
    }

    private fun initView() {
        toolbar.title = intent.getStringExtra(Constants.ROOM_NAME)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
                ?: Log.e(TAG, "actionBar is null")

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

        msgEditText.setOnFocusChangeListener { _, b ->
            if (b) {
                frameLayout.visibility = View.GONE
            }
        }

        sendMsgImgView.setOnClickListener(this)
        emojiImgView.setOnClickListener(this)

        adapter = RoomActivityAdapter(recyclerView)
        with(recyclerView) {
            adapter = this@RoomActivity.adapter
            layoutManager = LinearLayoutManager(this@RoomActivity)
            addOnScrollListener(ScrollListener())
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
                Utils.hideKeyboardFrom(this, View(this))
                addOrRemoveEmojiFragment()
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
                uploadFileImgButton.visibility = View.VISIBLE
                voiceImgButton.visibility = View.VISIBLE
                pictureImgButton.visibility = View.VISIBLE
            } else {
                sendMsgImgView.visibility = View.VISIBLE
                uploadFileImgButton.visibility = View.GONE
                voiceImgButton.visibility = View.GONE
                pictureImgButton.visibility = View.INVISIBLE
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

    private fun scrollRecyclerViewToLastPosition() {
        recyclerView.scrollToPosition(max(0, adapter.itemCount - 1))
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }
}