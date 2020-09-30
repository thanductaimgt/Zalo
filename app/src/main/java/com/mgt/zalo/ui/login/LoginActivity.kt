package com.mgt.zalo.ui.login

import android.animation.Animator
import android.content.Intent
import android.view.View
import android.widget.Toast
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.android.synthetic.main.activity_login.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity
import com.mgt.zalo.ui.home.HomeActivity
import com.mgt.zalo.ui.intro.IntroActivity


class LoginActivity : BaseActivity() {
    override fun onBindViews() {
        setContentView(R.layout.activity_login)

        clearTextImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
        loginButton.setOnClickListener(this)
        swapButton.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
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
            R.id.backImgView->{
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        }
    }

    private fun validateLoginInfo(phone: String, password: String) {
        database.validateLoginInfo(phone, password) { user ->
            if (user!=null) {
                sharedPrefsManager.setUser(user)

                sessionManager.initUser(user)

                LottieCompositionFactory.fromRawRes(this, R.raw.success).addListener {
                    animView.apply {
                        repeatCount = 0
                        setComposition(it)
                        playAnimation()
                        addAnimatorListener(object : Animator.AnimatorListener {
                            override fun onAnimationRepeat(p0: Animator?) {
                            }

                            override fun onAnimationCancel(p0: Animator?) {
                            }

                            override fun onAnimationStart(p0: Animator?) {
                            }

                            override fun onAnimationEnd(p0: Animator?) {
                                startActivity(Intent(this@LoginActivity, HomeActivity::class.java))

                                finish()
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
}