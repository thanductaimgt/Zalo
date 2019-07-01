package vng.zalo.tdtai.zalo.zalo.views.home.fragments.chat_fragment.room_activity

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
import kotlinx.android.synthetic.main.activity_room.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.DaggerRoomActivityComponent
import vng.zalo.tdtai.zalo.zalo.dependency_factories.chat_activity.RoomActivityModule
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.utils.MessageDiffCallback
import vng.zalo.tdtai.zalo.zalo.viewmodels.RoomActivityViewModel
import javax.inject.Inject
import kotlin.math.max

class RoomActivity : AppCompatActivity(), View.OnClickListener {
    @Inject lateinit var viewModel: RoomActivityViewModel
    private lateinit var adapter: RoomActivityAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerRoomActivityComponent.builder()
                .roomActivityModule(RoomActivityModule(this))
                .build().inject(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room)

        initView()

        adapter = RoomActivityAdapter(MessageDiffCallback())
        with(recyclerView){
            adapter = this@RoomActivity.adapter
            layoutManager = LinearLayoutManager(this@RoomActivity)
        }

        viewModel.liveMessages.observe(this, Observer{ messageList ->
            adapter.submitList(messageList) { this.scrollRecyclerViewToLastPosition() }
            Log.d(TAG, "onChanged liveData")
        })
    }

    private fun initView(){
        toolbar.title = intent.getStringExtra(Constants.ROOM_NAME)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true) ?: Log.e(TAG, "actionBar is null")

        msgTextInputEditText.addTextChangedListener(InputTextListener())
        msgTextInputEditText.setOnEditorActionListener { _, actionId, _ ->
            when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    onClickSendMessage()
                    true
                }
                else -> false
            }
        }

        sendMsgImgButton.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_room_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sendMsgImgButton -> onClickSendMessage()
        }
    }

    private fun onClickSendMessage() {
        val messageContent = msgTextInputEditText.text.toString()
        viewModel.addMessageToFirestore(messageContent)

        msgTextInputEditText.setText("")
    }

    internal inner class InputTextListener : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable) {
            if (s.isEmpty()) {
                sendMsgImgButton.visibility = View.GONE
                uploadFileImgButton.visibility = View.VISIBLE
                voiceImgButton.visibility = View.VISIBLE
                pictureImgButton.visibility = View.VISIBLE
            } else {
                sendMsgImgButton.visibility = View.VISIBLE
                uploadFileImgButton.visibility = View.GONE
                voiceImgButton.visibility = View.GONE
                pictureImgButton.visibility = View.INVISIBLE
            }
        }
    }

    private fun scrollRecyclerViewToLastPosition() {
        recyclerView.scrollToPosition(max(0, adapter.itemCount - 1))
    }

    companion object {
        private val TAG = RoomActivity::class.java.simpleName
    }
}