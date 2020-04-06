package vng.zalo.tdtai.zalo.ui.intro

import android.content.Intent
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_intro.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.base.BaseActivity
import vng.zalo.tdtai.zalo.ui.login.LoginActivity

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
        }
    }
}
