package vng.zalo.tdtai.zalo.zalo.views.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_register.*
import vng.zalo.tdtai.zalo.R

class RegisterActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initView()
    }

    private fun initView() {
        continueButton.setOnClickListener(this)
    }

    override fun onClick(p0: View?) {
        when (p0!!.id) {
//            R.id.continueButton -> startActivity(Intent(this, a).apply {
//                putExtra(Constants.REGISTER_NAME, nameTextInputEditText.text.toString())
//            })
        }
    }
}
