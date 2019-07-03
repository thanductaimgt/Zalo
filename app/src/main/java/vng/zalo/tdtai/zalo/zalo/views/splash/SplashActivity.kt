package vng.zalo.tdtai.zalo.zalo.views.splash

import android.content.Intent
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity

import vng.zalo.tdtai.zalo.zalo.views.intro.IntroActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }
}
