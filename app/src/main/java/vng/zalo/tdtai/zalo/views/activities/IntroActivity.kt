package vng.zalo.tdtai.zalo.views.activities

import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_intro.*
import vng.zalo.tdtai.zalo.R

class IntroActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        initView()
    }

    private fun initView() {
        loginButton.setOnClickListener(this)
        registerButton.setOnClickListener(this)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val outValue = TypedValue()
            theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)
            vnTextView.setBackgroundResource(outValue.resourceId)
            engTextView.setBackgroundResource(outValue.resourceId)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.loginButton -> startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
