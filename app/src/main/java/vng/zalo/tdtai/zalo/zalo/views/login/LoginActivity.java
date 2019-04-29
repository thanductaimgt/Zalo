package vng.zalo.tdtai.zalo.zalo.views.login;

import androidx.appcompat.app.AppCompatActivity;
import vng.zalo.tdtai.zalo.R;
import vng.zalo.tdtai.zalo.zalo.ZaloApplication;
import vng.zalo.tdtai.zalo.zalo.views.home.HomeActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

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
        ImageView clearTextButton = findViewById(R.id.clearTextImgButton);
        Button loginButton = findViewById(R.id.loginButton);

        clearTextButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        getSupportActionBar().setTitle(R.string.label_log_in);

        Button swapButton = findViewById(R.id.swapButton);
        swapButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.clearTextImgButton:
                phoneTextInputEditText.setText("");
                break;
            case R.id.loginButton:
                String currentPhone = phoneTextInputEditText.getText().toString();
                String currentPass = passTextInputEditText.getText().toString();
                if(isCredentialsValid(currentPhone, currentPass)){
                    Intent startLobbyIntent = new Intent(this, HomeActivity.class);
                    ZaloApplication.currentUserPhone = currentPhone;
                    startActivity(startLobbyIntent);
//                    finish();
                }
                break;
            case R.id.swapButton:
                phoneTextInputEditText.setText(phoneTextInputEditText.getText().toString().equals("0123456789")? "0987654321" : "0123456789");
                break;
        }
    }

    boolean isCredentialsValid(String phone, String pass){
        return true;
    }
}