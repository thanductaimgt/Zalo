package vng.zalo.tdtai.zalo.zalo.views.intro;

import androidx.appcompat.app.AppCompatActivity;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.views.login.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IntroActivity extends AppCompatActivity implements View.OnClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.registerButton);

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginButton:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }
}
