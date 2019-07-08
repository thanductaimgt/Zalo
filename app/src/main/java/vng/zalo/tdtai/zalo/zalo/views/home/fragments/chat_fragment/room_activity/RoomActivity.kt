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
import vng.zalo.tdtai.zalo.zalo.utils.Utils
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

        viewModel.liveMessages.observe(this, Observer{ messageList ->
            adapter.messages = messageList
            adapter.notifyDataSetChanged()
            this.scrollRecyclerViewToLastPosition()
//            adapter.submitList(messageList) { this.scrollRecyclerViewToLastPosition() }
            Log.d(Utils.getTag(object {}), "onChanged liveData")
        })
    }

    private fun initView(){
        toolbar.title = intent.getStringExtra(Constants.ROOM_NAME)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) ?: Log.e(Utils.getTag(object {}), "actionBar is null")

        msgEditText.apply{
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

        sendMsgImgView.setOnClickListener(this)

        adapter = RoomActivityAdapter()
        with(recyclerView){
            adapter = this@RoomActivity.adapter
            layoutManager = LinearLayoutManager(this@RoomActivity)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_room_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
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
        }
    }

    private fun sendMessage() {
        val messageContent = msgEditText.text.toString()
        viewModel.addNewMessageToFirestore(messageContent)

        msgEditText.setText("")
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
}