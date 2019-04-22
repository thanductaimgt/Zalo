package vng.zalo.tdtai.zalo.zalo.views.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.views.intro.IntroActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, IntroActivity.class));
    }
}
