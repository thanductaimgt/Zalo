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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.airbnb.lottie.model.LottieCompositionCache
import kotlinx.android.synthetic.main.activity_room.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.factories.chat_activity.DaggerRoomActivityComponent
import vng.zalo.tdtai.zalo.zalo.factories.chat_activity.RoomActivityModule
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel
import vng.zalo.tdtai.zalo.zalo.adapters.RoomActivityAdapter
import vng.zalo.tdtai.zalo.zalo.views.fragments.EmojiFragment
import javax.inject.Inject
import kotlin.math.max

class RoomActivity : AppCompatActivity(), View.OnClickListener {
    @Inject
    lateinit var viewModel: RoomActivityViewModel
    private lateinit var adapter: RoomActivityAdapter
    private var emojiFragment: EmojiFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerRoomActivityComponent.builder()
                .roomActivityModule(RoomActivityModule(this))
                .build().inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        initView()

        viewModel.liveMessages.observe(this, Observer { messageList ->
            adapter.messages = messageList
            adapter.notifyDataSetChanged()
            this.scrollRecyclerViewToLastPosition()
//            adapter.submitList(messageList) { this.scrollRecyclerViewToLastPosition() }
            Log.d(TAG, "onCreate.onChanged liveData")
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
                        sendMessage()
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

        adapter = RoomActivityAdapter()
        with(recyclerView) {
            adapter = this@RoomActivity.adapter
            layoutManager = LinearLayoutManager(this@RoomActivity)
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
            R.id.sendMsgImgView -> sendMessage()
            R.id.emojiImgView -> {
                Utils.hideKeyboardFrom(this, View(this))
                addOrRemoveEmojiFragment()
            }
        }
    }

    private fun sendMessage() {
        val messageContent = msgEditText.text.toString()
        viewModel.addNewMessageToFirestore(messageContent)

        msgEditText.setText("")
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

    private fun scrollRecyclerViewToLastPosition() {
        recyclerView.scrollToPosition(max(0, adapter.itemCount - 1))
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.removeListeners()
    }

    companion object{
        private val TAG = RoomActivity::class.java.simpleName
    }
}