package vng.zalo.tdtai.zalo.zalo.views.activities

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
import android.content.Context
import vng.zalo.tdtai.zalo.zalo.networks.Database
import vng.zalo.tdtai.zalo.zalo.utils.TAG


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
                loadingAnimView.visibility = View.VISIBLE
                loadingAnimView.playAnimation()

                val phone = phoneTextInputEditText.text.toString()
                val password = passTextInputEditText.text.toString()
                validateLoginInfo(phone, password)
            }
            R.id.swapButton -> {
                phoneTextInputEditText.setText(
                        when (phoneTextInputEditText.text.toString()) {
                            "0123456789" -> "0987654321"
                            "0111111111" -> "0123456789"
                            else -> "0111111111"
                        }
                )
            }
        }
    }

    private fun validateLoginInfo(phone: String, password: String) {
        Database.validateLoginInfo(phone, password) { isLoginSuccess, userInfo ->
            if (isLoginSuccess) {
                ZaloApplication.currentUser = userInfo
                addUserInfoToSharePreferences(userInfo!!)
                startActivity(Intent(this, HomeActivity::class.java))
//                        finish()
            } else {
                loadingAnimView.cancelAnimation()
                loadingAnimView.visibility = View.INVISIBLE
                Log.d(TAG, "login fail")
            }
        }
    }

    private fun addUserInfoToSharePreferences(userInfo: UserInfo) {
        val editor = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()

        editor.putString("phone", userInfo.phone)
        editor.putString("avatarUrl", userInfo.avatarUrl)

        editor.putLong("birthDateSecs", userInfo.birthDate!!.seconds)
        editor.putInt("birthDateNanoSecs", userInfo.birthDate!!.nanoseconds)

        editor.putBoolean("isMale", userInfo.isMale!!)

        editor.putLong("joinDateSecs", userInfo.joinDate!!.seconds)
        editor.putInt("joinDateNanoSecs", userInfo.joinDate!!.nanoseconds)

        editor.putBoolean("isLogin", true)

        editor.apply()
    }
}