package vng.zalo.tdtai.zalo.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import vng.zalo.tdtai.zalo.models.UserInfo
import vng.zalo.tdtai.zalo.utils.Constants


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val isLogin = prefs.getBoolean(UserInfo.FIELD_IS_LOGIN, false)
        startActivity(Intent(this, if(isLogin)HomeActivity::class.java else IntroActivity::class.java))

        finish()
    }
}
