package vng.zalo.tdtai.zalo.views.activities

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.android.synthetic.main.activity_login.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.models.UserInfo
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.utils.Constants


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initView()
    }

    private fun initView() {
        supportActionBar?.setTitle(R.string.label_log_in)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            clearTextImgView.setBackgroundResource(outValue.resourceId)
        }

        clearTextImgView.setOnClickListener(this)

        //solve layout render problem
        loginButton.isEnabled = true
        swapButton.isEnabled = true

        loginButton.setOnClickListener(this)
        swapButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.clearTextImgView -> phoneTextInputEditText.setText("")
            R.id.loginButton -> {
                animView.visibility = View.VISIBLE
                animView.playAnimation()

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
                setStatusToOnline()
                LottieCompositionFactory.fromRawRes(this, R.raw.success).addListener {
                    animView.apply {
                        repeatCount = 0
                        setComposition(it)
                        playAnimation()
                        addAnimatorListener(object :Animator.AnimatorListener{
                            override fun onAnimationRepeat(p0: Animator?) {
                            }

                            override fun onAnimationCancel(p0: Animator?) {
                            }

                            override fun onAnimationStart(p0: Animator?) {
                            }

                            override fun onAnimationEnd(p0: Animator?) {
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                //                        finish()
                            }
                        })
                    }
                }
            } else {
                animView.cancelAnimation()
                animView.visibility = View.GONE
                Toast.makeText(applicationContext, "Login fail !", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addUserInfoToSharePreferences(userInfo: UserInfo) {
        val editor = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()

        editor.putString(UserInfo.FIELD_PHONE, userInfo.phone)
        editor.putString(UserInfo.FIELD_AVATAR_URL, userInfo.avatarUrl)

        editor.putLong(UserInfo.FIELD_BIRTH_DATE_SECS, userInfo.birthDate!!.seconds)
        editor.putInt(UserInfo.FIELD_BIRTH_DATE_NANO_SECS, userInfo.birthDate!!.nanoseconds)

        editor.putBoolean(UserInfo.FIELD_IS_MALE, userInfo.isMale!!)

        editor.putLong(UserInfo.FIELD_JOIN_DATE_SECS, userInfo.joinDate!!.seconds)
        editor.putInt(UserInfo.FIELD_JOIN_DATE_NANO_SECS, userInfo.joinDate!!.nanoseconds)

        editor.putBoolean(UserInfo.FIELD_IS_LOGIN, true)

        editor.apply()
    }

    private fun setStatusToOnline(){
        Database.setCurrentUserOnlineState(true)
    }
}