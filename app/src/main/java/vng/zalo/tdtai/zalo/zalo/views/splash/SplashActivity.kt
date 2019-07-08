package vng.zalo.tdtai.zalo.zalo.views.splash

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle

import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import vng.zalo.tdtai.zalo.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.zalo.models.UserInfo

import vng.zalo.tdtai.zalo.zalo.views.intro.IntroActivity
import vng.zalo.tdtai.zalo.zalo.utils.Constants
import vng.zalo.tdtai.zalo.zalo.views.home.HomeActivity


class SplashActivity : AppCompatActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isLogin = prefs.getBoolean("isLogin", false)
        if (isLogin) {
            setAppUserInfoFromSharePreferences()
            startActivity(Intent(this, HomeActivity::class.java))
        } else {
            startActivity(Intent(this, IntroActivity::class.java))
        }
        finish()
    }

    private fun setAppUserInfoFromSharePreferences() {
        prefs.apply {
            ZaloApplication.currentUser = UserInfo(
                    phone = getString("phone", null),
                    avatar = getString("avatar", null),
                    birthDate = Timestamp(getLong("birthDateSecs", 0), getInt("birthDateNanoSecs", 0)),
                    isMale = getBoolean("isMale", true),
                    joinDate = Timestamp(getLong("joinDateSecs", 0), getInt("joinDateNanoSecs", 0))
            )
        }
    }
}
