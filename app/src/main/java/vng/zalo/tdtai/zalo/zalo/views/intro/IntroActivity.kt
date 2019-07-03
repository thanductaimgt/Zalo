package vng.zalo.tdtai.zalo.zalo.views.intro

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_intro.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.views.login.LoginActivity

class IntroActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        initView()
    }

    private fun initView(){
        loginButton.setOnClickListener(this)
        registerButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.loginButton -> startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
