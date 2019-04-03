package vng.zalo.tdtai.zalo.screens.login;

import androidx.appcompat.app.AppCompatActivity;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.screens.lobby.LobbyActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextInputEditText phoneTextInputEditText;
    private TextInputEditText passTextInputEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        phoneTextInputEditText = findViewById(R.id.phoneTextInputEditText);
        passTextInputEditText = findViewById(R.id.passTextInputEditText);
        ImageButton clearTextButton = findViewById(R.id.clearTextImgButton);
        Button loginButton = findViewById(R.id.loginButton);

        clearTextButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        getSupportActionBar().setTitle(R.string.log_in);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clearTextImgButton:
                phoneTextInputEditText.setText("");
                break;
            case R.id.loginButton:
                ProgressBar progressBar = findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                progressBar.setVisibility(View.GONE);
                Intent startLobbyIntent = new Intent(this, LobbyActivity.class);
                startActivity(startLobbyIntent);
                break;
        }
    }
}