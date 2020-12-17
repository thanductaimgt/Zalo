package com.mgt.zalo.ui.intro

import android.content.Intent
import android.view.View
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity
import com.mgt.zalo.ui.login.LoginActivity
import com.mgt.zalo.ui.sign_up.SignUpActivity
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : BaseActivity(){
    override fun onBindViews() {
        requestFullScreen()
        setContentView(R.layout.activity_intro)

        loginButton.setOnClickListener(this)
        registerButton.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.loginButton -> startActivity(Intent(this, LoginActivity::class.java))
            R.id.registerButton -> startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
