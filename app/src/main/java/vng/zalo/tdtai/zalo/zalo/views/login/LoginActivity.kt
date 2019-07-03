package vng.zalo.tdtai.zalo.zalo.views.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.UserInfo
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.views.home.HomeActivity

class LoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initView()
    }

    private fun initView() {
        supportActionBar?.setTitle(R.string.label_log_in)

        clearTextImgButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
        swapButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.clearTextImgButton -> phoneTextInputEditText.setText("")
            R.id.loginButton -> {
                val currentPhone = phoneTextInputEditText.text.toString()
                val currentPass = passTextInputEditText.text.toString()
                validateCredentials(currentPhone, currentPass)
            }
            R.id.swapButton -> phoneTextInputEditText.setText(if (phoneTextInputEditText.text.toString() == "0123456789") "0987654321" else "0123456789")
        }
    }

    private fun validateCredentials(phone: String, pass: String) {
        ZaloApplication.firebaseInstance.collection(Constants.COLLECTION_USERS)
                .document(phone)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        ZaloApplication.currentUser = task.result!!.toObject(UserInfo::class.java)
                        ZaloApplication.currentUser!!.phone = phone
                        startActivity(Intent(this, HomeActivity::class.java))
                        //                    finish();
                    } else {
                        Log.d(TAG, "task not successful")
                    }
                }
    }

    companion object {
        val TAG = LoginActivity::class.java.simpleName
    }
}