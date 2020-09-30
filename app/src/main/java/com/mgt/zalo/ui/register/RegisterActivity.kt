package com.mgt.zalo.ui.register

import android.view.View
import kotlinx.android.synthetic.main.activity_register.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseActivity

class RegisterActivity : BaseActivity() {
    override fun onBindViews() {
        setContentView(R.layout.activity_register)

        continueButton.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
//            R.id.continueButton -> startActivity(Intent(this, a).apply {
//                putExtra(Constants.REGISTER_NAME, nameTextInputEditText.text.toString())
//            })
        }
    }
}
