package vng.zalo.tdtai.zalo.ui.login

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieCompositionFactory
import kotlinx.android.synthetic.main.activity_login.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.managers.SharedPrefsManager
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.repo.FirebaseDatabase
import vng.zalo.tdtai.zalo.ui.home.HomeActivity
import vng.zalo.tdtai.zalo.ui.intro.IntroActivity
import javax.inject.Inject


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    @Inject
    lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initView()
    }

    private fun initView() {
        clearTextImgView.setOnClickListener(this)
        backImgView.setOnClickListener(this)
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
            R.id.backImgView->{
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        }
    }

    private fun validateLoginInfo(phone: String, password: String) {
        database.validateLoginInfo(phone, password) { user ->
            if (user!=null) {
                SharedPrefsManager.setUser(this, user)

                (application as ZaloApplication).initUser(user)

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